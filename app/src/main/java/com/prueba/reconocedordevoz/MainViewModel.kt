package com.prueba.reconocedordevoz

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Representa el estado de la interfaz de usuario para la pantalla principal.
 *
 * @property isListening Indica si la aplicación está esperando o procesando entrada de voz.
 * @property palabraReconocida La palabra final que se ha identificado (o "..." si está cargando).
 * @property codigoEncontrado El código asociado a la palabra reconocida.
 * @property errorMessage Mensaje de error para mostrar al usuario en caso de fallos.
 */
data class UiState(
    val isListening: Boolean = false,
    val palabraReconocida: String = "...",
    val codigoEncontrado: String = "...",
    val errorMessage: String? = null
)

/**
 * ViewModel encargado de la lógica de negocio de la pantalla principal.
 * Gestiona el reconocimiento de voz y la búsqueda de códigos asociados a ciudades.
 *
 * @property context Contexto de la aplicación, utilizado para instanciar el repositorio.
 */
class MainViewModel(private val context: Context) : ViewModel() {

    private val _uiState = mutableStateOf(UiState())
    /** Estado observable de la UI. */
    val uiState: State<UiState> = _uiState

    private val repository = CiudadesRepository(context)

    /**
     * Carga las equivalencias de ciudades desde el repositorio.
     * Convierte la lista en un mapa para búsqueda rápida: nombre (minúsculas) -> código.
     */
    private fun getEquivalencias(): Map<String, String> {
        return repository.cargarCiudades()
            .associate { it.nombre.lowercase() to it.codigo }
    }

    /**
     * Inicia el estado de escucha en la UI, reseteando valores previos.
     * Debe llamarse antes de lanzar el Intent de reconocimiento de voz.
     */
    fun startListening() {
        if (uiState.value.isListening) return
        _uiState.value = _uiState.value.copy(
            isListening = true,
            palabraReconocida = "...",
            codigoEncontrado = "...",
            errorMessage = null
        )
    }

    /**
     * Establece un mensaje de error explícito desde la UI o Activity (ej. permisos denegados).
     *
     * @param message El mensaje de error a mostrar.
     */
    fun setErrorMessage(message: String) {
        _uiState.value = _uiState.value.copy(
            isListening = false,
            errorMessage = message
        )
    }

    /**
     * Procesa los resultados devueltos por el reconocedor de voz de Google.
     * Busca la primera coincidencia en la base de datos de ciudades.
     *
     * @param results Lista de posibles transcripciones devueltas por el API de voz.
     */
    fun processSpeechResult(results: List<String>?) {
        if (results.isNullOrEmpty()) {
            _uiState.value = _uiState.value.copy(
                isListening = false,
                errorMessage = "No se ha reconocido ninguna palabra."
            )
            return
        }

        // El resultado de Google es una lista de posibles transcripciones, la primera es la más probable.
        val textoReconocido = results[0].lowercase()
        var palabraEncontrada: String? = null
        var codigoEncontrado: String? = null

        val equivalencias = getEquivalencias()

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
                // La primera en mayúscula para presentación
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
}

/**
 * Factory para crear instancias de [MainViewModel] con dependencias.
 */
class MainViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}