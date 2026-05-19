package com.example.nammanala.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.nammanala.data.model.CanalReport
import com.example.nammanala.ui.theme.*
import com.example.nammanala.ui.viewmodel.ReportViewModel
import com.example.nammanala.ui.viewmodel.SubmitEvent
import com.example.nammanala.util.getCurrentLocation
import com.example.nammanala.util.hasLocationPermission
import kotlinx.coroutines.launch
import java.io.File

// Explicitly import FlowRow and the experimental annotation
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi

private val reportTypes = listOf("BREACH", "SILT", "ILLEGAL_LIFTING", "ENCROACHMENT", "GARBAGE")
private val severityLevels = listOf("LOW", "MEDIUM", "HIGH", "CRITICAL")

private fun severityColor(s: String) = when (s) {
    "LOW"      -> Color(0xFF388E3C)
    "MEDIUM"   -> WarningAmber
    "HIGH"     -> Color(0xFFE64A19)
    "CRITICAL" -> ErrorRed
    else       -> Color.Gray
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReportBreachScreen(
    navController: NavController,
    viewModel: ReportViewModel = viewModel()
) {
    val context     = LocalContext.current
    val scope       = rememberCoroutineScope()
    val snackHost   = remember { SnackbarHostState() }

    var photoUri      by remember { mutableStateOf<Uri?>(null) }
    var tempPhotoUri  by remember { mutableStateOf<Uri?>(null) }

    // --- Launchers ---

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) photoUri = tempPhotoUri
    }

    // Camera permission launcher
    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                tempPhotoUri?.let { cameraLauncher.launch(it) }
            } else {
                Toast.makeText(
                    context,
                    "Camera permission denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    var locationText  by remember { mutableStateOf("") }
    var lat           by remember { mutableDoubleStateOf(0.0) }
    var lng           by remember { mutableDoubleStateOf(0.0) }
    var description   by remember { mutableStateOf("") }
    var village       by remember { mutableStateOf("") }
    var selectedType  by remember { mutableStateOf("BREACH") }
    var selectedSeverity by remember { mutableStateOf("MEDIUM") }

    val isLoading by viewModel.isLoading.collectAsState()

    // Observe submit events
    LaunchedEffect(Unit) {
        viewModel.submitEvent.collect { event ->
            when (event) {
                is SubmitEvent.Success -> {
                    Toast.makeText(context, "Report submitted successfully!", Toast.LENGTH_LONG).show()
                    navController.popBackStack()
                }
                is SubmitEvent.Error -> {
                    snackHost.showSnackbar("Error: ${event.message}")
                }
            }
        }
    }

    // Location permission launcher
    val locationPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            captureLocation(context) { la, lo, text ->
                lat = la; lng = lo; locationText = text
            }
        } else {
            scope.launch { snackHost.showSnackbar("Location permission required") }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackHost) },
        topBar = {
            TopAppBar(
                title = { Text("Report an Issue", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- Report Type ---
            SectionLabel("Issue Type")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                reportTypes.forEach { type ->
                    val selected = type == selectedType
                    FilterChip(
                        selected = selected,
                        onClick  = { selectedType = type },
                        label    = { Text(type.replace("_", " "), fontSize = 12.sp) },
                        leadingIcon = if (selected) {
                            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CanalGreen,
                            selectedLabelColor     = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )
                }
            }

            // --- Severity ---
            SectionLabel("Severity")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                severityLevels.forEach { sev ->
                    val selected = sev == selectedSeverity
                    val color    = severityColor(sev)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selected) color else Color.Transparent)
                            .border(1.5.dp, color, RoundedCornerShape(20.dp))
                            .clickable { selectedSeverity = sev }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            sev,
                            fontSize = 12.sp,
                            color    = if (selected) Color.White else color,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // --- Photo ---
            SectionLabel("Photo (optional)")
            if (photoUri != null) {
                Box {
                    AsyncImage(
                        model              = photoUri,
                        contentDescription = "Captured photo",
                        modifier           = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale       = ContentScale.Crop
                    )
                    IconButton(
                        onClick  = { photoUri = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Remove", tint = Color.White)
                    }
                }
            } else {
                OutlinedButton(
                    onClick = {
                        val file = createTempImageFile(context)
                        val uri  = FileProvider.getUriForFile(
                            context, "${context.packageName}.provider", file
                        )
                        tempPhotoUri = uri
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Take Photo")
                }
            }

            // --- Location ---
            SectionLabel("GPS Location")
            OutlinedButton(
                onClick = {
                    if (hasLocationPermission(context)) {
                        captureLocation(context) { la, lo, text ->
                            lat = la; lng = lo; locationText = text
                        }
                    } else {
                        locationPermLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.MyLocation, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (locationText.isEmpty()) "Capture GPS Location" else "Re-capture Location")
            }
            if (locationText.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(10.dp)
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = CanalGreen, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(locationText, fontSize = 13.sp, color = CanalGreen)
                }
            }

            // --- Village ---
            OutlinedTextField(
                value         = village,
                onValueChange = { village = it },
                label         = { Text("Village / Area Name") },
                leadingIcon   = { Icon(Icons.Filled.Place, contentDescription = null) },
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp),
                singleLine    = true
            )

            // --- Description ---
            OutlinedTextField(
                value         = description,
                onValueChange = { description = it },
                label         = { Text("Describe the issue") },
                leadingIcon   = { Icon(Icons.Filled.Description, contentDescription = null) },
                modifier      = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape         = RoundedCornerShape(12.dp),
                maxLines      = 5
            )

            // --- Submit ---
            Button(
                onClick = {
                    if (lat == 0.0) {
                        scope.launch { snackHost.showSnackbar("Please capture GPS location first") }
                        return@Button
                    }
                    val report = CanalReport(
                        latitude    = lat,
                        longitude   = lng,
                        description = description,
                        reportType  = selectedType,
                        severity    = selectedSeverity,
                        village     = village
                    )
                    viewModel.submitReport(report, photoUri)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                enabled  = !isLoading,
                colors   = ButtonDefaults.buttonColors(containerColor = CanalGreen)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color    = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Filled.Send, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Submit Report", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text       = text,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 14.sp,
        color      = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
    )
}

private fun captureLocation(
    context: Context,
    onResult: (Double, Double, String) -> Unit
) {
    getCurrentLocation(context) { la, lo ->
        onResult(la, lo, "Lat: %.5f, Lng: %.5f".format(la, lo))
    }
}

private fun createTempImageFile(context: Context): File {
    val dir = File(context.cacheDir, "images").apply { mkdirs() }
    return File.createTempFile("report_", ".jpg", dir)
}
