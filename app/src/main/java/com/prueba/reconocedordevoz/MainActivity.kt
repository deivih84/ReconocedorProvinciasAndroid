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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.prueba.reconocedordevoz.ui.theme.ReconocedorDeVozTheme


/**
 * Actividad principal de la aplicación.
 *
 * Se encarga de la configuración inicial, gestión de permisos, navegación entre pantallas (MainScreen y CiudadesScreen)
 * y el lanzamiento del Intent de reconocimiento de voz de Google.
 */
class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var ciudadesViewModel: CiudadesViewModel
    private lateinit var appPreferences: AppPreferencesRepository

    /**
     * Launcher para recibir el resultado del reconocimiento de voz de Google.
     * Procesa el resultado en [MainViewModel].
     */
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

    /**
     * Launcher para solicitar permiso de micrófono.
     * Muestra un mensaje de error si el permiso es denegado.
     */
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                viewModel.setErrorMessage("Permiso de micrófono necesario para el reconocimiento de voz.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appPreferences = AppPreferencesRepository(applicationContext)
        val viewModelFactory = MainViewModelFactory(applicationContext)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        val ciudadesViewModelFactory = CiudadesViewModelFactory(applicationContext)
        ciudadesViewModel = ViewModelProvider(this, ciudadesViewModelFactory)[CiudadesViewModel::class.java]

        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)

        setContent {
            ReconocedorDeVozTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showOnboarding by remember { mutableStateOf(!appPreferences.isOnboardingCompleted()) }
                    var mostrarGestionCiudades by remember { mutableStateOf(false) }
                    
                    val uiState by viewModel.uiState
                    val ciudadesUiState by ciudadesViewModel.uiState

                    when {
                        showOnboarding -> {
                            OnboardingScreen(onFinished = {
                                appPreferences.setOnboardingCompleted()
                                showOnboarding = false
                            })
                        }
                        mostrarGestionCiudades -> {
                            CiudadesScreen(
                                uiState = ciudadesUiState,
                                onAñadirCiudad = { ciudadesViewModel.mostrarDialogoAñadir() },
                                onEditarCiudad = { ciudad -> ciudadesViewModel.mostrarDialogoEditar(ciudad) },
                                onEliminarCiudad = { nombre -> ciudadesViewModel.eliminarCiudad(nombre) },
                                onVolverAtras = { mostrarGestionCiudades = false },
                                onLimpiarMensaje = { ciudadesViewModel.limpiarMensaje() }
                            )

                            // Diálogos
                            if (ciudadesUiState.mostrarDialogoAñadir) {
                                DialogoAñadirCiudad(
                                    onDismiss = { ciudadesViewModel.ocultarDialogoAñadir() },
                                    onConfirmar = { nombre, codigo ->
                                        ciudadesViewModel.añadirCiudad(nombre, codigo)
                                    }
                                )
                            }

                            if (ciudadesUiState.mostrarDialogoEditar && ciudadesUiState.ciudadAEditar != null) {
                                DialogoEditarCiudad(
                                    ciudad = ciudadesUiState.ciudadAEditar!!,
                                    onDismiss = { ciudadesViewModel.ocultarDialogoEditar() },
                                    onConfirmar = { nombreAntiguo, nombreNuevo, codigoNuevo ->
                                        ciudadesViewModel.actualizarCiudad(nombreAntiguo, nombreNuevo, codigoNuevo)
                                    }
                                )
                            }
                        }
                        else -> {
                            MainScreen(
                                uiState = uiState,
                                onStartListening = {
                                    viewModel.startListening()
                                    launchSpeechRecognizer()
                                },
                                onGestionarCiudades = { mostrarGestionCiudades = true }
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Lanza el Intent para el reconocimiento de voz de Google.
     * Maneja excepciones si el servicio no está disponible en el dispositivo.
     */
    private fun launchSpeechRecognizer() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES") // Especificamos español
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Di el nombre de la ciudad...") // Mensaje en el diálogo
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            viewModel.setErrorMessage("Error: Reconocimiento de voz no disponible.")
            e.printStackTrace()
        }
    }
}