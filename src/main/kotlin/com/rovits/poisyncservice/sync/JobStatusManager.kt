package com.rovits.poisyncservice.sync

import java.util.concurrent.ConcurrentHashMap
import java.util.UUID

object JobStatusManager {
    private val jobStatusMap = ConcurrentHashMap<String, JobStatus>()
    private val jobErrorMap = ConcurrentHashMap<String, String>()
    private val jobTimestamps = ConcurrentHashMap<String, Long>()

    // Job retention duration: 1 hour
    private const val JOB_RETENTION_MILLIS = 60 * 60 * 1000L

    fun createJob(): String {
        val jobId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        jobStatusMap[jobId] = JobStatus.IN_PROGRESS
        jobErrorMap[jobId] = ""
        jobTimestamps[jobId] = now

        // Cleanup old jobs
        cleanupOldJobs(now)

        return jobId
    }

    fun setJobStatus(jobId: String, status: JobStatus, error: String? = null) {
        jobStatusMap[jobId] = status
        jobErrorMap[jobId] = error ?: ""
        jobTimestamps[jobId] = System.currentTimeMillis()
    }

    fun getJobStatus(jobId: String): Pair<JobStatus?, String?> {
        val status = jobStatusMap[jobId]
        val error = jobErrorMap[jobId]
        return Pair(status, if (error.isNullOrEmpty()) null else error)
    }

    private fun cleanupOldJobs(currentTime: Long) {
        val jobsToRemove = jobTimestamps.entries
            .filter { (_, timestamp) ->
                currentTime - timestamp > JOB_RETENTION_MILLIS
            }
            .map { it.key }

        jobsToRemove.forEach { jobId ->
            jobStatusMap.remove(jobId)
            jobErrorMap.remove(jobId)
            jobTimestamps.remove(jobId)
        }
    }
}
