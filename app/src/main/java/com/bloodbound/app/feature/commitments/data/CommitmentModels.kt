// FILE: app/src/main/java/com/bloodbound/app/feature/commitments/data/CommitmentModels.kt
package com.bloodbound.app.feature.commitments.data

data class CommitmentDto(
    val id: Long,
    val status: String,
    val committedAt: String,
    val referenceNumber: String?,
    val requestId: Long,
    val bloodTypeNeeded: String?,
    val hospitalName: String?,
    val requesterName: String?,
    val requesterContactNumber: String?,
    val urgency: String?
)

data class CreateCommitmentBody(val requestId: Long)