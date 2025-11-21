package com.rovits.poisyncservice.sync

import java.util.concurrent.ConcurrentHashMap
import java.util.UUID

object JobStatusManager {
    private val jobStatusMap = ConcurrentHashMap<String, JobStatus>()
    private val jobErrorMap = ConcurrentHashMap<String, String>()

    fun createJob(): String {
        val jobId = UUID.randomUUID().toString()
        jobStatusMap[jobId] = JobStatus.IN_PROGRESS
        jobErrorMap[jobId] = ""
        return jobId
    }

    fun setJobStatus(jobId: String, status: JobStatus, error: String? = null) {
        jobStatusMap[jobId] = status
        jobErrorMap[jobId] = error ?: ""
    }

    fun getJobStatus(jobId: String): Pair<JobStatus?, String?> {
        val status = jobStatusMap[jobId]
        val error = jobErrorMap[jobId]
        return Pair(status, if (error.isNullOrEmpty()) null else error)
    }
}
