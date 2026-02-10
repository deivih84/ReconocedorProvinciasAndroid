package com.prueba.reconocedordevoz

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Estado de la interfaz de usuario para la pantalla de gestión de ciudades.
 *
 * @property ciudades Lista actual de ciudades cargadas.
 * @property mostrarDialogoAñadir Indica si se debe mostrar el diálogo de creación.
 * @property mostrarDialogoEditar Indica si se debe mostrar el diálogo de edición.
 * @property ciudadAEditar La ciudad seleccionada para editar (si aplica).
 * @property mensaje Mensaje informativo o de error (ej. Snackbars).
 */
data class CiudadesUiState(
    val ciudades: List<Ubicacion> = emptyList(),
    val mostrarDialogoAñadir: Boolean = false,
    val mostrarDialogoEditar: Boolean = false,
    val ciudadAEditar: Ubicacion? = null,
    val mensaje: String? = null
)

/**
 * ViewModel para gestionar las operaciones CRUD de ciudades.
 *
 * @property context Contexto de la aplicación.
 */
class CiudadesViewModel(private val context: Context) : ViewModel() {

    private val repository = CiudadesRepository(context)

    private val _uiState = mutableStateOf(CiudadesUiState())
    /** Estado observable de la UI de gestión. */
    val uiState: State<CiudadesUiState> = _uiState

    init {
        cargarCiudades()
    }

    /** Recarga la lista de ciudades desde el repositorio. */
    private fun cargarCiudades() {
        val ciudades = repository.cargarCiudades()
        _uiState.value = _uiState.value.copy(ciudades = ciudades)
    }

    /** Muestra el diálogo para añadir una nueva ciudad. */
    fun mostrarDialogoAñadir() {
        _uiState.value = _uiState.value.copy(mostrarDialogoAñadir = true)
    }

    /** Oculta el diálogo para añadir una nueva ciudad. */
    fun ocultarDialogoAñadir() {
        _uiState.value = _uiState.value.copy(mostrarDialogoAñadir = false)
    }

    /**
     * Muestra el diálogo para editar una ciudad existente.
     * @param ciudad La ciudad a editar.
     */
    fun mostrarDialogoEditar(ciudad: Ubicacion) {
        _uiState.value = _uiState.value.copy(
            mostrarDialogoEditar = true,
            ciudadAEditar = ciudad
        )
    }

    /** Oculta el diálogo de edición. */
    fun ocultarDialogoEditar() {
        _uiState.value = _uiState.value.copy(
            mostrarDialogoEditar = false,
            ciudadAEditar = null
        )
    }

    /**
     * Añade una nueva ciudad con validación básica.
     * @param nombre Nombre de la ciudad.
     * @param codigo Código de la ciudad.
     */
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

    /**
     * Actualiza una ciudad existente.
     * @param nombreAntiguo Nombre original para buscar la ciudad.
     * @param nombreNuevo Nuevo nombre de la ciudad.
     * @param codigoNuevo Nuevo código de la ciudad.
     */
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

    /**
     * Elimina una ciudad.
     * @param nombre Nombre de la ciudad a eliminar.
     */
    fun eliminarCiudad(nombre: String) {
        val resultado = repository.eliminarCiudad(nombre)
        if (resultado) {
            cargarCiudades()
            _uiState.value = _uiState.value.copy(mensaje = "Ciudad eliminada correctamente")
        } else {
            _uiState.value = _uiState.value.copy(mensaje = "Error al eliminar la ciudad")
        }
    }

    /** Limpia el mensaje de estado actual (Snackbar). */
    fun limpiarMensaje() {
        _uiState.value = _uiState.value.copy(mensaje = null)
    }
}

/**
 * Factory para crear instancias de [CiudadesViewModel].
 */
class CiudadesViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CiudadesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CiudadesViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

