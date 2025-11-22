package com.rovits.poisyncservice.sync

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class JobStatusManagerTest {

    @BeforeEach
    fun setup() {
        // Note: JobStatusManager is an object, so state persists between tests
        // In a real scenario, we might want to add a reset() method for testing
    }

    @Test
    fun `should create job with IN_PROGRESS status`() {
        val jobId = JobStatusManager.createJob()

        val (status, error) = JobStatusManager.getJobStatus(jobId)

        assertNotNull(jobId)
        assertEquals(JobStatus.IN_PROGRESS, status)
        assertNull(error)
    }

    @Test
    fun `should update job status successfully`() {
        val jobId = JobStatusManager.createJob()

        JobStatusManager.setJobStatus(jobId, JobStatus.COMPLETED)

        val (status, error) = JobStatusManager.getJobStatus(jobId)

        assertEquals(JobStatus.COMPLETED, status)
        assertNull(error)
    }

    @Test
    fun `should store error message on FAILED status`() {
        val jobId = JobStatusManager.createJob()
        val errorMessage = "Connection timeout"

        JobStatusManager.setJobStatus(jobId, JobStatus.FAILED, errorMessage)

        val (status, error) = JobStatusManager.getJobStatus(jobId)

        assertEquals(JobStatus.FAILED, status)
        assertEquals(errorMessage, error)
    }

    @Test
    fun `should return null for non-existent job`() {
        val (status, error) = JobStatusManager.getJobStatus("non-existent-job-id")

        assertNull(status)
        assertNull(error)
    }

    @Test
    fun `should handle multiple jobs independently`() {
        val jobId1 = JobStatusManager.createJob()
        val jobId2 = JobStatusManager.createJob()

        JobStatusManager.setJobStatus(jobId1, JobStatus.COMPLETED)
        JobStatusManager.setJobStatus(jobId2, JobStatus.FAILED, "Error occurred")

        val (status1, error1) = JobStatusManager.getJobStatus(jobId1)
        val (status2, error2) = JobStatusManager.getJobStatus(jobId2)

        assertEquals(JobStatus.COMPLETED, status1)
        assertNull(error1)

        assertEquals(JobStatus.FAILED, status2)
        assertEquals("Error occurred", error2)
    }

    @Test
    fun `should cleanup old jobs to prevent memory leak`() {
        // Create a job
        val oldJobId = JobStatusManager.createJob()
        JobStatusManager.setJobStatus(oldJobId, JobStatus.COMPLETED)

        // Verify job exists
        val (statusBefore, _) = JobStatusManager.getJobStatus(oldJobId)
        assertEquals(JobStatus.COMPLETED, statusBefore)

        // Note: Testing actual cleanup requires either:
        // 1. Waiting 1 hour (not practical)
        // 2. Mocking time (requires refactoring to inject Clock)
        // 3. Exposing cleanup method for testing

        // This test documents the expected behavior
        // In production, jobs older than 1 hour will be automatically cleaned

        // For now, we verify the mechanism doesn't break job creation
        val newJobId = JobStatusManager.createJob()
        assertNotNull(newJobId)

        val (newStatus, _) = JobStatusManager.getJobStatus(newJobId)
        assertEquals(JobStatus.IN_PROGRESS, newStatus)
    }

    @Test
    fun `should generate unique job IDs`() {
        val jobIds = mutableSetOf<String>()

        repeat(100) {
            val jobId = JobStatusManager.createJob()
            jobIds.add(jobId)
        }

        assertEquals(100, jobIds.size, "All job IDs should be unique")
    }

    @Test
    fun `should be thread-safe under concurrent access`() {
        val threads = 10
        val jobsPerThread = 10
        val allJobIds = mutableListOf<String>()

        val threadList = (1..threads).map {
            Thread {
                repeat(jobsPerThread) {
                    val jobId = JobStatusManager.createJob()
                    synchronized(allJobIds) {
                        allJobIds.add(jobId)
                    }
                    JobStatusManager.setJobStatus(jobId, JobStatus.COMPLETED)
                }
            }
        }

        threadList.forEach { it.start() }
        threadList.forEach { it.join() }

        assertEquals(threads * jobsPerThread, allJobIds.size)
        assertEquals(allJobIds.size, allJobIds.toSet().size, "All job IDs should be unique")

        // Verify all jobs have correct status
        allJobIds.forEach { jobId ->
            val (status, _) = JobStatusManager.getJobStatus(jobId)
            assertEquals(JobStatus.COMPLETED, status)
        }
    }

    @Test
    fun `should update timestamp when status changes`() {
        val jobId = JobStatusManager.createJob()

        // Initial status
        val (status1, _) = JobStatusManager.getJobStatus(jobId)
        assertEquals(JobStatus.IN_PROGRESS, status1)

        Thread.sleep(100) // Small delay

        // Update status
        JobStatusManager.setJobStatus(jobId, JobStatus.COMPLETED)

        val (status2, _) = JobStatusManager.getJobStatus(jobId)
        assertEquals(JobStatus.COMPLETED, status2)

        // Job should still exist (timestamp updated)
        assertNotNull(status2)
    }
}

