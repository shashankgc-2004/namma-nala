package com.example.nammanala.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.nammanala.data.model.CanalReport
import com.example.nammanala.ui.theme.*
import com.example.nammanala.ui.viewmodel.ReportViewModel
import java.text.SimpleDateFormat
import java.util.*

private fun statusColor(status: String) = when (status) {
    "PENDING" -> WarningAmber
    "IN_PROGRESS" -> WaterBlue
    "RESOLVED" -> Color(0xFF388E3C)
    else -> Color.Gray
}

private fun severityBg(severity: String) = when (severity) {
    "CRITICAL" -> ErrorRed
    "HIGH" -> Color(0xFFE64A19)
    "MEDIUM" -> WarningAmber
    "LOW" -> Color(0xFF388E3C)
    else -> Color.Gray
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterStatusScreen(
    navController: NavController,
    viewModel: ReportViewModel = viewModel()
) {

    val reports by viewModel.reports.collectAsState(initial = emptyList())

    var filterStatus by remember {
        mutableStateOf("ALL")
    }

    var selectedReport by remember {
        mutableStateOf<CanalReport?>(null)
    }

    var showRepairDialog by remember {
        mutableStateOf(false)
    }

    val filtered =
        if (filterStatus == "ALL") reports
        else reports.filter { it.status == filterStatus }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Water Status Feed",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = null
                        )
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                SummaryChip(
                    "Pending",
                    reports.count {
                        it.status == "PENDING"
                    }.toString(),
                    WarningAmber
                )

                SummaryChip(
                    "Active",
                    reports.count {
                        it.status == "IN_PROGRESS"
                    }.toString(),
                    WaterBlue
                )

                SummaryChip(
                    "Resolved",
                    reports.count {
                        it.status == "RESOLVED"
                    }.toString(),
                    Color(0xFF388E3C)
                )
            }

            ScrollableTabRow(
                selectedTabIndex = listOf(
                    "ALL",
                    "PENDING",
                    "IN_PROGRESS",
                    "RESOLVED"
                ).indexOf(filterStatus)
            ) {

                listOf(
                    "ALL",
                    "PENDING",
                    "IN_PROGRESS",
                    "RESOLVED"
                ).forEach { tab ->

                    Tab(
                        selected = filterStatus == tab,
                        onClick = {
                            filterStatus = tab
                        },
                        text = {
                            Text(tab.replace("_", " "))
                        }
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(filtered, key = { it.id }) { report ->

                    ReportCard(
                        report = report,

                        onStartRepair = {
                            viewModel.startRepair(it)
                        },

                        onCompleteRepair = {

                            selectedReport = it
                            showRepairDialog = true
                        }
                    )
                }
            }
        }

        if (showRepairDialog && selectedReport != null) {

            AlertDialog(
                onDismissRequest = {
                    showRepairDialog = false
                },

                title = {
                    Text("Complete Repair")
                },

                text = {
                    Text(
                        "After uploading repair photo and GPS location, report will move to RESOLVED tab."
                    )
                },

                confirmButton = {

                    Button(
                        onClick = {

                            // TODO:
                            // Launch camera/gallery
                            // Fetch GPS
                            // Upload repair proof

                            viewModel.resolveReport(
                                selectedReport!!.id
                            )

                            showRepairDialog = false
                        }
                    ) {
                        Text("Complete")
                    }
                },

                dismissButton = {

                    TextButton(
                        onClick = {
                            showRepairDialog = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun SummaryChip(
    label: String,
    count: String,
    color: Color
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            count,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = color
        )

        Text(
            label,
            fontSize = 11.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun ReportCard(
    report: CanalReport,
    onStartRepair: (String) -> Unit,
    onCompleteRepair: (CanalReport) -> Unit
) {

    val dateStr = report.timestamp?.let {

        SimpleDateFormat(
            "dd MMM, hh:mm a",
            Locale.getDefault()
        ).format(it)

    } ?: "Just now"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Column(
            Modifier.padding(14.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            CanalGreen.copy(alpha = 0.12f)
                        )
                        .padding(
                            horizontal = 8.dp,
                            vertical = 4.dp
                        )
                ) {

                    Text(
                        report.reportType.replace("_", " "),
                        color = CanalGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    report.status.replace("_", " "),
                    color = statusColor(report.status),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(10.dp))

            if (report.photoUrl.isNotEmpty()) {

                AsyncImage(
                    model = report.photoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.height(10.dp))
            }

            Text(
                report.description,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(10.dp))

            if (report.status == "PENDING") {

                Button(
                    onClick = {
                        onStartRepair(report.id)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WaterBlue
                    )
                ) {

                    Icon(
                        Icons.Default.Build,
                        contentDescription = null
                    )

                    Spacer(Modifier.width(6.dp))

                    Text("Start Repair")
                }
            }

            if (report.status == "IN_PROGRESS") {

                Button(
                    onClick = {
                        onCompleteRepair(report)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF388E3C)
                    )
                ) {

                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null
                    )

                    Spacer(Modifier.width(6.dp))

                    Text("Complete Repair")
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                dateStr,
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}