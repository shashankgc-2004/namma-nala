package com.example.nammanala.data.repository

import android.net.Uri
import android.util.Log
import com.example.nammanala.data.model.CanalReport
import com.example.nammanala.data.model.WaterStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val reportsCollection = db.collection("reports")
    private val statusCollection = db.collection("water_status")
    private val usersCollection = db.collection("users")

    fun getCurrentUser() = auth.currentUser

    suspend fun saveUserToFirestore() {
        val user = auth.currentUser ?: return

        val userMap = hashMapOf(
            "uid" to user.uid,
            "name" to (user.displayName ?: ""),
            "email" to (user.email ?: ""),
            "photoUrl" to (user.photoUrl?.toString() ?: "")
        )

        usersCollection.document(user.uid).set(userMap).await()
    }

    suspend fun uploadReport(
        report: CanalReport,
        photoUri: Uri?
    ): Result<CanalReport> {

        return try {

            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("User not signed in"))

            val reportId = UUID.randomUUID().toString()

            var imageUrl = ""

            if (photoUri != null) {

                val imageRef = storage.reference
                    .child("reports/${currentUser.uid}/$reportId.jpg")

                imageRef.putFile(photoUri).await()

                imageUrl = imageRef.downloadUrl.await().toString()
            }

            val finalReport = report.copy(
                id = reportId,
                userId = currentUser.uid,
                photoUrl = imageUrl
            )

            reportsCollection
                .document(reportId)
                .set(finalReport)
                .await()

            Result.success(finalReport)

        } catch (e: Exception) {

            Log.e("FirebaseRepo", "Upload Error", e)

            Result.failure(e)
        }
    }

    suspend fun updateReportStatus(
        reportId: String,
        status: String
    ): Result<Unit> {

        return try {

            reportsCollection
                .document(reportId)
                .update("status", status)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    suspend fun completeRepair(
        reportId: String,
        photoUri: Uri,
        latitude: Double,
        longitude: Double
    ): Result<Unit> {

        return try {

            val userId =
                FirebaseAuth.getInstance().currentUser!!.uid

            val repairRef = storage.reference.child(
                "repair_photos/$userId/$reportId.jpg"
            )

            repairRef.putFile(photoUri).await()

            val repairPhotoUrl =
                repairRef.downloadUrl.await().toString()

            reportsCollection
                .document(reportId)
                .update(
                    mapOf(
                        "status" to "RESOLVED",
                        "repairPhotoUrl" to repairPhotoUrl,
                        "resolvedLatitude" to latitude,
                        "resolvedLongitude" to longitude,
                        "resolvedAt" to System.currentTimeMillis()
                    )
                )
                .await()

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    fun observeReports(): Flow<List<CanalReport>> = callbackFlow {

        val currentUser = auth.currentUser

        if (currentUser == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = reportsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val reports = snapshot
                    ?.toObjects(CanalReport::class.java)
                    ?: emptyList()

                trySend(reports)
            }

        awaitClose {
            listener.remove()
        }
    }

    suspend fun getWaterStatusList(): List<WaterStatus> {

        return try {

            statusCollection
                .get()
                .await()
                .toObjects(WaterStatus::class.java)

        } catch (e: Exception) {

            emptyList()
        }
    }

    fun signOut() {
        auth.signOut()
    }
}