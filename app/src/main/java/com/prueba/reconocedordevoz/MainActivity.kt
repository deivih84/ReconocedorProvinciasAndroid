package com.prueba.reconocedordevoz

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.prueba.reconocedordevoz.ui.theme.ReconocedorDeVozTheme

import java.util.*

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel

    private val speechRecognizerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                viewModel.processSpeechResult(results)
            } else {
                // El usuario canceló o hubo un error
                viewModel.processSpeechResult(null)
            }
        }

    // Lanzador para el permiso de micrófono (sigue siendo necesario)
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                // TODO Manejar el caso de permiso denegado
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModelFactory = MainViewModelFactory(applicationContext)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)

        setContent {
            ReconocedorDeVozTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val uiState by viewModel.uiState

                    MainScreen(
                        uiState = uiState,
                        onStartListening = {
                            viewModel.startListening() // Avisa al ViewModel que empezamos
                            launchSpeechRecognizer()   // Lanza la UI de Google
                        }
                    )
                }
            }
        }
    }

    private fun launchSpeechRecognizer() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES") // Especificamos español
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Di el nombre de la ciudad...") // Mensaje en el diálogo
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            // TODO Manejar el caso de que el reconocimiento de voz no esté disponible en el dispositivo
            viewModel.processSpeechResult(null)
            e.printStackTrace()
        }
    }
}