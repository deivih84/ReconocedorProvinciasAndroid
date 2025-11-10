package com.prueba.reconocedordevoz

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

data class CiudadesUiState(
    val ciudades: List<Ubicacion> = emptyList(),
    val mostrarDialogoAñadir: Boolean = false,
    val mostrarDialogoEditar: Boolean = false,
    val ciudadAEditar: Ubicacion? = null,
    val mensaje: String? = null
)

class CiudadesViewModel(private val context: Context) : ViewModel() {

    private val repository = CiudadesRepository(context)

    private val _uiState = mutableStateOf(CiudadesUiState())
    val uiState: State<CiudadesUiState> = _uiState

    init {
        cargarCiudades()
    }

    private fun cargarCiudades() {
        val ciudades = repository.cargarCiudades()
        _uiState.value = _uiState.value.copy(ciudades = ciudades)
    }

    fun mostrarDialogoAñadir() {
        _uiState.value = _uiState.value.copy(mostrarDialogoAñadir = true)
    }

    fun ocultarDialogoAñadir() {
        _uiState.value = _uiState.value.copy(mostrarDialogoAñadir = false)
    }

    fun mostrarDialogoEditar(ciudad: Ubicacion) {
        _uiState.value = _uiState.value.copy(
            mostrarDialogoEditar = true,
            ciudadAEditar = ciudad
        )
    }

    fun ocultarDialogoEditar() {
        _uiState.value = _uiState.value.copy(
            mostrarDialogoEditar = false,
            ciudadAEditar = null
        )
    }

    fun añadirCiudad(nombre: String, codigo: String) {
        if (nombre.isBlank() || codigo.isBlank()) {
            _uiState.value = _uiState.value.copy(mensaje = "El nombre y código no pueden estar vacíos")
            return
        }

        val ciudad = Ubicacion(nombre.trim(), codigo.trim())
        val resultado = repository.añadirCiudad(ciudad)

        if (resultado) {
            cargarCiudades()
            ocultarDialogoAñadir()
            _uiState.value = _uiState.value.copy(mensaje = "Ciudad añadida correctamente")
        } else {
            _uiState.value = _uiState.value.copy(mensaje = "Ya existe una ciudad con ese nombre")
        }
    }

    fun actualizarCiudad(nombreAntiguo: String, nombreNuevo: String, codigoNuevo: String) {
        if (nombreNuevo.isBlank() || codigoNuevo.isBlank()) {
            _uiState.value = _uiState.value.copy(mensaje = "El nombre y código no pueden estar vacíos")
            return
        }

        val ciudadNueva = Ubicacion(nombreNuevo.trim(), codigoNuevo.trim())
        val resultado = repository.actualizarCiudad(nombreAntiguo, ciudadNueva)

        if (resultado) {
            cargarCiudades()
            ocultarDialogoEditar()
            _uiState.value = _uiState.value.copy(mensaje = "Ciudad actualizada correctamente")
        } else {
            _uiState.value = _uiState.value.copy(mensaje = "Error al actualizar la ciudad")
        }
    }

    fun eliminarCiudad(nombre: String) {
        val resultado = repository.eliminarCiudad(nombre)
        if (resultado) {
            cargarCiudades()
            _uiState.value = _uiState.value.copy(mensaje = "Ciudad eliminada correctamente")
        } else {
            _uiState.value = _uiState.value.copy(mensaje = "Error al eliminar la ciudad")
        }
    }

    fun limpiarMensaje() {
        _uiState.value = _uiState.value.copy(mensaje = null)
    }
}

class CiudadesViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CiudadesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CiudadesViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

