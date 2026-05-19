package com.example.nammanala.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.nammanala.data.model.CanalReport
import com.example.nammanala.ui.theme.*
import com.example.nammanala.ui.viewmodel.ReportViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    viewModel: ReportViewModel = viewModel()
) {
    val reports by viewModel.reports.collectAsState(initial = emptyList())
    var selectedReport by remember { mutableStateOf<CanalReport?>(null) }
    var mapType by remember { mutableStateOf(MapType.NORMAL) }

    val bangalore = LatLng(12.9716, 77.5946)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(bangalore, 12f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Canal Map", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        mapType = if (mapType == MapType.NORMAL) MapType.HYBRID else MapType.NORMAL
                    }) {
                        Icon(Icons.Filled.Layers, contentDescription = "Toggle map type", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CanalGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            GoogleMap(
                modifier            = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties          = MapProperties(mapType = mapType),
                uiSettings          = MapUiSettings(zoomControlsEnabled = true)
            ) {
                // Render each report as a map marker
                reports.forEach { report ->
                    val position = LatLng(report.latitude, report.longitude)
                    val hue = markerHue(report)
                    Marker(
                        state       = rememberMarkerState(position = position),
                        title       = report.reportType.replace("_", " "),
                        snippet     = report.description.take(60),
                        icon        = BitmapDescriptorFactory.defaultMarker(hue),
                        onClick     = {
                            selectedReport = report
                            false
                        }
                    )
                }
            }

            // Report count badge
            if (reports.isNotEmpty()) {
                Surface(
                    modifier  = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    shape     = RoundedCornerShape(20.dp),
                    color     = CanalGreen,
                    shadowElevation = 4.dp
                ) {
                    Text(
                        "${reports.size} reports",
                        color    = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Bottom sheet for selected report
            selectedReport?.let { report ->
                Surface(
                    modifier        = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape           = RoundedCornerShape(16.dp),
                    shadowElevation = 8.dp,
                    color           = Color.White
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier            = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                report.reportType.replace("_", " "),
                                fontWeight = FontWeight.Bold,
                                fontSize   = 16.sp
                            )
                            TextButton(onClick = { selectedReport = null }) {
                                Text("Close")
                            }
                        }
                        if (report.village.isNotEmpty()) {
                            Text("📍 ${report.village}", fontSize = 13.sp, color = Color.Gray)
                        }
                        if (report.description.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            Text(report.description, fontSize = 13.sp)
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusBadge(report.status)
                            SeverityBadge(report.severity)
                        }
                    }
                }
            }
        }
    }
}

private fun markerHue(report: CanalReport): Float = when {
    report.reportType == "BREACH"         -> BitmapDescriptorFactory.HUE_RED
    report.severity   == "CRITICAL"       -> BitmapDescriptorFactory.HUE_RED
    report.reportType == "ILLEGAL_LIFTING" -> BitmapDescriptorFactory.HUE_ORANGE
    report.status     == "RESOLVED"       -> BitmapDescriptorFactory.HUE_GREEN
    else                                  -> BitmapDescriptorFactory.HUE_AZURE
}

@Composable
private fun StatusBadge(status: String) {
    val color = when (status) {
        "PENDING"     -> WarningAmber
        "IN_PROGRESS" -> WaterBlue
        "RESOLVED"    -> Color(0xFF388E3C)
        else          -> Color.Gray
    }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            status.replace("_", " "),
            fontSize   = 11.sp,
            color      = color,
            fontWeight = FontWeight.SemiBold,
            modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun SeverityBadge(severity: String) {
    val color = when (severity) {
        "CRITICAL" -> ErrorRed
        "HIGH"     -> Color(0xFFE64A19)
        "MEDIUM"   -> WarningAmber
        else       -> Color(0xFF388E3C)
    }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            severity,
            fontSize   = 11.sp,
            color      = color,
            fontWeight = FontWeight.SemiBold,
            modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}