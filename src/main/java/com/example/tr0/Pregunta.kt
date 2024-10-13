package com.example.tr0

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

@Composable
fun Pregunta(navController: NavHostController) {
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var timeElapsed by remember { mutableStateOf(0L) }
    var startTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var timerRunning by remember { mutableStateOf(true) }
    var selectedImageIndex by remember { mutableStateOf(-1) } // Índice de la imagen seleccionada
    var answerCorrect by remember { mutableStateOf(false) } // Indica si la respuesta es correcta

    val preguntasEstadisticas = remember { mutableStateListOf<PreguntaEstadistica>() }

    LaunchedEffect(Unit) {
        fetchQuestions { fetchedQuestions ->
            questions = fetchedQuestions
            isLoading = false
        }
    }

    LaunchedEffect(timerRunning) {
        while (timerRunning) {
            delay(1000)
            timeElapsed = (System.currentTimeMillis() - startTime) / 1000
        }
    }

    if (isLoading) {
        Text(text = "Cargando preguntas...")
    } else {
        if (currentQuestionIndex < questions.size && currentQuestionIndex < 10) {
            val question = questions[currentQuestionIndex]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = question.pregunta, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Image(
                            painter = rememberImagePainter(question.respostaCorrecta.imatge),
                            contentDescription = "Respuesta correcta",
                            modifier = Modifier
                                .size(100.dp)
                                .clickable {
                                    selectedImageIndex = 0 // 0 es el índice de la respuesta correcta
                                    answerCorrect = true // Marca como respuesta correcta
                                }
                                .padding(8.dp)
                                .border(
                                    width = 2.dp,
                                    color = if (selectedImageIndex == 0 && answerCorrect) Color.Green else Color.Transparent
                                ) // Resaltar si es la respuesta correcta
                        )

                        // Mostrar la primera imagen incorrecta
                        if (question.respostesIncorrectes.isNotEmpty()) {
                            Image(
                                painter = rememberImagePainter(question.respostesIncorrectes[0].imatge),
                                contentDescription = "Respuesta incorrecta",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clickable {
                                        selectedImageIndex = 1 // 1 es el índice de la primera respuesta incorrecta
                                        answerCorrect = false // Marca como respuesta incorrecta
                                    }
                                    .padding(8.dp)
                                    .border(
                                        width = 2.dp,
                                        color = if (selectedImageIndex == 1 && !answerCorrect) Color.Red else Color.Transparent
                                    )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Mostrar la segunda imagen incorrecta
                        if (question.respostesIncorrectes.size > 1) {
                            Image(
                                painter = rememberImagePainter(question.respostesIncorrectes[1].imatge),
                                contentDescription = "Respuesta incorrecta",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clickable {
                                        selectedImageIndex = 2 // 2 es el índice de la segunda respuesta incorrecta
                                        answerCorrect = false // Marca como respuesta incorrecta
                                    }
                                    .padding(8.dp)
                                    .border(
                                        width = 2.dp,
                                        color = if (selectedImageIndex == 2 && !answerCorrect) Color.Red else Color.Transparent
                                    )
                            )
                        }

                        // Mostrar la tercera imagen incorrecta
                        if (question.respostesIncorrectes.size > 2) {
                            Image(
                                painter = rememberImagePainter(question.respostesIncorrectes[2].imatge),
                                contentDescription = "Respuesta incorrecta",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clickable {
                                        selectedImageIndex = 3 // 3 es el índice de la tercera respuesta incorrecta
                                        answerCorrect = false // Marca como respuesta incorrecta
                                    }
                                    .padding(8.dp)
                                    .border(
                                        width = 2.dp,
                                        color = if (selectedImageIndex == 3 && !answerCorrect) Color.Red else Color.Transparent
                                    )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))



                Button(onClick = {
                    val tiempoRespuesta = (System.currentTimeMillis() - startTime) / 1000

                    // Añadimos las estadísticas de la pregunta actual
                    preguntasEstadisticas.add(
                        PreguntaEstadistica(
                            pregunta = question.pregunta,
                            tiempoRespuesta = tiempoRespuesta,

                        )
                    )

                    // Enviar las estadísticas al servidor
                    sendEstadisticas(preguntasEstadisticas.toList())

                    currentQuestionIndex++
                    selectedImageIndex = -1
                    answerCorrect = false

                    if (currentQuestionIndex >= questions.size || currentQuestionIndex >= 10) {
                        timerRunning = false
                        navController.navigate("final/${timeElapsed}")
                    }
                }, enabled = answerCorrect) {
                    Text(text = "Siguiente")
                }

                Text(text = "Tiempo transcurrido: $timeElapsed segundos", fontSize = 16.sp)
            }
        } else {
            timerRunning = false
            Text(text = "No hay más preguntas", fontSize = 24.sp)
        }
    }
}
data class PreguntaEstadistica(
    val pregunta: String,
    val tiempoRespuesta: Long,
)

private fun sendEstadisticas(preguntasEstadisticas: List<PreguntaEstadistica>) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val url = URL("http://dam.inspedralbes.cat:22222/guardarEstadisticas")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val userId = UUID.randomUUID().toString()

            // Crea el objeto JSON que se va a enviar al servidor
            val jsonEstadisticas = JSONObject().apply {
                put("usuarioId", userId) // Enviamos null como usuarioId, que el servidor generará
                put("preguntas", JSONArray().apply {
                    for (estadistica in preguntasEstadisticas) {
                        put(JSONObject().apply {
                            put("pregunta", estadistica.pregunta)
                            put("tiempoRespuesta", estadistica.tiempoRespuesta)
                        })
                    }
                })
            }

            // Convertimos el objeto JSON a String para enviarlo
            val jsonToSend = jsonEstadisticas.toString()
            connection.outputStream.use { os ->
                val input = jsonToSend.toByteArray()
                os.write(input, 0, input.size)
            }

            val responseCode = connection.responseCode
            if (responseCode == 200) {
                Log.d("Estadisticas", "Estadísticas enviadas correctamente")
            } else {
                Log.e("EstadisticasError", "Error al enviar estadísticas: Código $responseCode")
            }
        } catch (e: Exception) {
            Log.e("EstadisticasError", "Error en el envío: ${e.message}")
        }
    }
}




// Función para obtener preguntas del servidor
private fun fetchQuestions(onResult: (List<Question>) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        val url = URL("http://dam.inspedralbes.cat:22222/preguntas")
        try {
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()

            if (connection.responseCode == 200) {
                val inputStream = connection.inputStream.bufferedReader().use { it.readText() }
                val preguntas = parseQuestions(inputStream)

                withContext(Dispatchers.Main) {
                    onResult(preguntas)
                }
            } else {
                withContext(Dispatchers.Main) {
                    onResult(emptyList())
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onResult(emptyList())
            }
        }
    }
}
private fun parseQuestions(json: String): List<Question> {
    val jsonObject = JSONObject(json)
    val jsonArray = jsonObject.getJSONArray("preguntes")
    val questionsList = mutableListOf<Question>()

    for (i in 0 until jsonArray.length()) {
        val questionObject = jsonArray.getJSONObject(i)
        val pregunta = questionObject.getString("pregunta")
        val respostaCorrecta = questionObject.getJSONObject("resposta_correcta").getString("imatge")

        val respostesIncorrectesJson = questionObject.getJSONArray("respostes_incorrectes")
        val respostesIncorrectes = mutableListOf<RespuestaIncorrecta>()
        for (j in 0 until respostesIncorrectesJson.length()) {
            val incorrectaImatge = respostesIncorrectesJson.getJSONObject(j).getString("imatge")
            respostesIncorrectes.add(RespuestaIncorrecta(incorrectaImatge))
        }

        questionsList.add(
            Question(
                pregunta,
                RespostaCorrecta(respostaCorrecta),
                respostesIncorrectes
            )
        )
    }
    return questionsList
}

@Serializable
data class Question(
    val pregunta: String,
    val respostaCorrecta: RespostaCorrecta,
    val respostesIncorrectes: List<RespuestaIncorrecta>
)

@Serializable
data class RespostaCorrecta(val imatge: String)

@Serializable
data class RespuestaIncorrecta(val imatge: String)
