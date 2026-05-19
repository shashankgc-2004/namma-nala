package com.example.nammanala.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.nammanala.data.repository.FirebaseRepository
import com.google.android.gms.auth.api.signin.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

@Composable
fun LoginScreen(navController: NavController) {

    val activity = navController.context as Activity

    val repository = remember {
        FirebaseRepository()
    }

    val gso = GoogleSignInOptions.Builder(
        GoogleSignInOptions.DEFAULT_SIGN_IN
    )
        .requestIdToken("YOUR_WEB_CLIENT_ID")
        .requestEmail()
        .build()

    val googleSignInClient =
        GoogleSignIn.getClient(activity, gso)

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        val task = GoogleSignIn
            .getSignedInAccountFromIntent(result.data)

        val account = task.result

        val credential = GoogleAuthProvider
            .getCredential(account.idToken, null)

        FirebaseAuth.getInstance()
            .signInWithCredential(credential)
            .addOnSuccessListener {

                navController.navigate("home") {
                    popUpTo("login") {
                        inclusive = true
                    }
                }
            }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Button(
            onClick = {

                launcher.launch(
                    googleSignInClient.signInIntent
                )
            }
        ) {

            Text("Sign in with Google")
        }
    }
}