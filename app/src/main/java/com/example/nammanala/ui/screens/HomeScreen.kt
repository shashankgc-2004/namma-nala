package com.example.nammanala.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Logout
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nammanala.ui.theme.*

@Composable
fun HomeScreen(navController: NavController) {

    val context = LocalContext.current
    val activity = context as Activity

    val gso = GoogleSignInOptions.Builder(
        GoogleSignInOptions.DEFAULT_SIGN_IN
    )
        .requestIdToken("1094050913936-fv7a12a8pgkc0tc79nc45ppmg25kjjij.apps.googleusercontent.com")
        .requestEmail()
        .build()

    val googleSignInClient: GoogleSignInClient =
        GoogleSignIn.getClient(context, gso)

    val account = remember {
        mutableStateOf(GoogleSignIn.getLastSignedInAccount(context))
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->

        try {

            val task = GoogleSignIn
                .getSignedInAccountFromIntent(result.data)

            if (task.isSuccessful) {

                val googleAccount = task.result

                val credential = GoogleAuthProvider.getCredential(
                    googleAccount.idToken,
                    null
                )

                FirebaseAuth.getInstance()
                    .signInWithCredential(credential)
                    .addOnCompleteListener { authTask ->

                        if (authTask.isSuccessful) {

                            account.value = googleAccount

                            Log.d(
                                "GoogleSignIn",
                                "Firebase login success"
                            )

                        } else {

                            Log.e(
                                "GoogleSignIn",
                                "Firebase login failed",
                                authTask.exception
                            )
                        }
                    }
            }
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Login Error", e)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(CanalGreen, CanalGreenLight)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 28.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Column {
                            Text(
                                "Namma Nala",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp
                            )

                            Text(
                                "Canal Health Monitor · Bengaluru",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 13.sp
                            )
                        }
                    }

                    Box {

                        if (account.value == null) {

                            Button(
                                onClick = {
                                    launcher.launch(
                                        googleSignInClient.signInIntent
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White
                                ),
                                shape = RoundedCornerShape(50)
                            ) {

                                Text(
                                    "Sign In",
                                    color = CanalGreen
                                )
                            }

                        } else {

                            var expanded by remember {
                                mutableStateOf(false)
                            }

                            Box {

                                AsyncImage(
                                    model = account.value?.photoUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            expanded = true
                                        }
                                )

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = {
                                        expanded = false
                                    }
                                ) {

                                    DropdownMenuItem(
                                        text = {
                                            Text("Sign Out")
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Logout,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = {

                                            FirebaseAuth.getInstance().signOut()

                                            googleSignInClient.signOut().addOnCompleteListener {

                                                account.value = null
                                                expanded = false
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Quick stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatBadge("Canals", "12", Icons.Filled.LocationOn)
                    StatBadge("Active Issues", "3", Icons.Filled.Warning)
                    StatBadge("Resolved", "28", Icons.Filled.CheckCircle)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "Quick Actions",
            modifier = Modifier.padding(horizontal = 20.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(12.dp))

        // Action cards grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionCard(
                modifier = Modifier.weight(1f),
                icon       = Icons.Filled.ReportProblem,
                title      = "Report Breach",
                subtitle   = "Log a leak or damage",
                color      = ErrorRed,
                onClick    = { navController.navigate("report") }
            )
            ActionCard(
                modifier = Modifier.weight(1f),
                icon     = Icons.Filled.Map,
                title    = "Canal Map",
                subtitle = "View all canal routes",
                color    = WaterBlue,
                onClick  = { navController.navigate("map") }
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionCard(
                modifier = Modifier.weight(1f),
                icon     = Icons.Filled.Waves,
                title    = "Water Status",
                subtitle = "Live feed & reports",
                color    = CanalTeal,
                onClick  = { navController.navigate("status") }
            )
            ActionCard(
                modifier = Modifier.weight(1f),
                icon     = Icons.Filled.Engineering,
                title    = "Silt / Encroach",
                subtitle = "Coming soon",
                color    = WarningAmber,
                onClick  = { /* TODO */ }
            )
        }

        Spacer(Modifier.height(24.dp))

        // Info banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = null,
                    tint = CanalGreen
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "Your reports help BBMP maintain healthy canals across Bengaluru.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CanalGreen
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun StatBadge(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(18.dp))
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
    }
}

@Composable
private fun ActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(subtitle, fontSize = 11.sp, color = Color.Gray)
        }
    }
}