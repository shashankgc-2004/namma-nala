package com.example.nammanala.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammanala.data.model.CanalReport
import com.example.nammanala.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SubmitEvent {
    data object Success : SubmitEvent()
    data class Error(val message: String) : SubmitEvent()
}

class ReportViewModel : ViewModel() {
    private val repository = FirebaseRepository()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _submitEvent = MutableSharedFlow<SubmitEvent>()
    val submitEvent = _submitEvent.asSharedFlow()

    // Live reports for WaterStatusScreen
    val reports = repository.observeReports()

    fun submitReport(report: CanalReport, photoUri: Uri?) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.uploadReport(report, photoUri)
            _isLoading.value = false
            if (result.isSuccess) {
                _submitEvent.emit(SubmitEvent.Success)
            } else {
                _submitEvent.emit(
                    SubmitEvent.Error(result.exceptionOrNull()?.message ?: "Unknown error")
                )
            }
        }
    }

    fun resolveReport(reportId: String) {

        viewModelScope.launch {

            repository.updateReportStatus(
                reportId,
                "RESOLVED"
            )
        }
    }

    fun startRepair(reportId: String) {

        viewModelScope.launch {

            repository.updateReportStatus(
                reportId,
                "IN_PROGRESS"
            )
        }
    }

    fun completeRepair(
        reportId: String,
        photoUri: Uri,
        latitude: Double,
        longitude: Double
    ) {

        viewModelScope.launch {

            repository.completeRepair(
                reportId,
                photoUri,
                latitude,
                longitude
            )
        }
    }
}