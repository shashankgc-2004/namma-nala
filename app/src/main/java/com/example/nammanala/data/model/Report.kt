package com.example.nammanala.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class CanalReport(
    val id: String = "",
    val userId: String = "",
    val reportType: String = "BREACH", // BREACH, SILT, ILLEGAL_LIFTING, ENCROACHMENT, GARBAGE
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val description: String = "",
    val photoUrl: String = "",
    val village: String = "",
    val severity: String = "MEDIUM", // LOW, MEDIUM, HIGH, CRITICAL
    val status: String = "PENDING",  // PENDING, IN_PROGRESS, RESOLVED
    val repairPhotoUrl: String = "",
    val resolvedLatitude: Double = 0.0,
    val resolvedLongitude: Double = 0.0,
    val resolvedAt: Long? = null,
    @ServerTimestamp
    val timestamp: Date? = null
)

data class WaterStatus(
    val id: String = "",
    val section: String = "",
    val waterLevel: Float = 0f,
    val flowRate: Float = 0f,
    val turbidity: String = "CLEAR",  // CLEAR, SLIGHTLY_TURBID, TURBID
    val alertLevel: String = "NORMAL", // NORMAL, WARNING, CRITICAL
    val lastUpdated: Date? = null,
    val reportedBy: String = ""
)