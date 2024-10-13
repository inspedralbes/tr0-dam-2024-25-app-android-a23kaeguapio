package com.example.tr0

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import java.util.UUID

@Composable
fun IniciQuiz(navController: NavHostController) {
    val sessionId = UUID.randomUUID().toString()
    Image(
        painter = painterResource(id = R.drawable.azul),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "QUIZ", fontSize = 24.sp,color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("questions") }) {
            Text(text = "Jugar")
        }
    }
}

