package com.prueba.reconocedordevoz

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

data class UiState(
    val isListening: Boolean = false,
    val palabraReconocida: String = "...",
    val codigoEncontrado: String = "...",
    val errorMessage: String? = null
)

class MainViewModel(private val context: Context) : ViewModel() {

    private val _uiState = mutableStateOf(UiState())
    val uiState: State<UiState> = _uiState

    private val equivalencias: Map<String, String> by lazy { cargarEquivalencias() }

    // El usuario pulsó el botón 👺👺👺
    fun startListening() {
        if (uiState.value.isListening) return
        _uiState.value = _uiState.value.copy(
            isListening = true,
            palabraReconocida = "...",
            codigoEncontrado = "..."
        )
    }

    // Esta función se llama desde la MainActivity con el resultado de Google ❤️
    fun processSpeechResult(results: List<String>?) {
        if (results.isNullOrEmpty()) {
            _uiState.value = _uiState.value.copy(
                isListening = false,
                errorMessage = "No se ha reconocido ninguna palabra."
            )
            return
        }

        // El resultado de Google es una lista de posibles transcripciones, la primera es la más probable.
        // Que listillos
        val textoReconocido = results[0].lowercase()
        var palabraEncontrada: String? = null
        var codigoEncontrado: String? = null

        Log.d("SpeechDebug", "Texto recibido de Google: '$textoReconocido'")
        Log.d("SpeechDebug", "Claves en el mapa: ${equivalencias.keys}")

        for (ciudad in equivalencias.keys) {
            if (textoReconocido.contains(ciudad)) {
                palabraEncontrada = ciudad
                codigoEncontrado = equivalencias[ciudad]
                break
            }
        }

        if (palabraEncontrada != null && codigoEncontrado != null) {
            _uiState.value = _uiState.value.copy(
                isListening = false,
                // La primera en mayúscula para evitar el error loco aquel
                palabraReconocida = palabraEncontrada.replaceFirstChar { it.titlecase() },
                codigoEncontrado = codigoEncontrado
            )
        } else {
            _uiState.value = _uiState.value.copy(
                isListening = false,
                palabraReconocida = "---",
                codigoEncontrado = "---",
                errorMessage = "Palabra no encontrada en la base de datos."
            )
        }
    }

    // La carga del JSON no cambia
    private fun cargarEquivalencias(): Map<String, String> {
        return try {
            context.assets.open("equivalencias.json").use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val wrapperType = object : TypeToken<Map<String, List<Ubicacion>>>() {}.type
                    val wrapper: Map<String, List<Ubicacion>> = Gson().fromJson(reader, wrapperType)
                    wrapper["ubicaciones"]
                        ?.associate { it.nombre.lowercase() to it.codigo }
                        ?: emptyMap()
                }
            }
        } catch (e: Exception) {
            Log.e("CargaJSON", "Error fatal al leer o parsear equivalencias.json", e)

            _uiState.value = _uiState.value.copy(
                errorMessage = "Error al cargar el archivo JSON. Revisa Logcat."
            )
            emptyMap()
        }
    }
}

// La Factory no cambia
class MainViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}