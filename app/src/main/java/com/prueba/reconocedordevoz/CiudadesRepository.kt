package com.prueba.reconocedordevoz

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

/**
 * Repositorio encargado de gestionar la persistencia de los datos de las ciudades.
 * Utiliza SharedPreferences para almacenar la lista de ciudades editada por el usuario
 * y carga un JSON inicial desde los assets si es la primera vez que se ejecuta.
 *
 * @property context El contexto de la aplicación, necesario para acceder a SharedPreferences y Assets.
 */
class CiudadesRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("ciudades_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_CIUDADES = "ciudades_list"
        private const val KEY_INITIALIZED = "initialized"
    }

    /**
     * Carga la lista de ciudades almacenada.
     * Si es la primera ejecución, inicializa los datos desde el archivo 'equivalencias.json' en assets.
     *
     * @return Una lista de objetos [Ubicacion].
     */
    fun cargarCiudades(): List<Ubicacion> {
        val isInitialized = prefs.getBoolean(KEY_INITIALIZED, false)

        return if (isInitialized) {
            // Cargar desde SharedPreferences
            val json = prefs.getString(KEY_CIUDADES, null)
            if (json != null) {
                val type = object : TypeToken<List<Ubicacion>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            } else {
                emptyList()
            }
        } else {
            // Primera vez: cargar desde assets y guardar
            val ciudadesIniciales = cargarDesdeAssets()
            guardarCiudades(ciudadesIniciales)
            prefs.edit().putBoolean(KEY_INITIALIZED, true).apply()
            ciudadesIniciales
        }
    }

    /**
     * Guarda la lista de ciudades en SharedPreferences serializada como JSON.
     *
     * @param ciudades La lista de objetos [Ubicacion] a guardar.
     */
    fun guardarCiudades(ciudades: List<Ubicacion>) {
        val json = gson.toJson(ciudades)
        prefs.edit().putString(KEY_CIUDADES, json).apply()
    }

    /**
     * Carga los datos iniciales desde el archivo JSON en assets.
     *
     * @return Lista inicial de [Ubicacion] o una lista vacía en caso de error.
     */
    private fun cargarDesdeAssets(): List<Ubicacion> {
        return try {
            context.assets.open("equivalencias.json").use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val wrapperType = object : TypeToken<Map<String, List<Ubicacion>>>() {}.type
                    val wrapper: Map<String, List<Ubicacion>> = gson.fromJson(reader, wrapperType)
                    wrapper["ubicaciones"] ?: emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e("CiudadesRepo", "Error al cargar desde assets", e)
            emptyList()
        }
    }

    /**
     * Añade una nueva ciudad al repositorio.
     * No permite duplicados basados en el nombre (case-insensitive).
     *
     * @param ciudad La nueva [Ubicacion] a añadir.
     * @return `true` si se añadió correctamente, `false` si ya existía.
     */
    fun añadirCiudad(ciudad: Ubicacion): Boolean {
        val ciudades = cargarCiudades().toMutableList()
        // Verificar que no exista ya
        if (ciudades.any { it.nombre.equals(ciudad.nombre, ignoreCase = true) }) {
            return false
        }
        ciudades.add(ciudad)
        guardarCiudades(ciudades)
        return true
    }

    /**
     * Actualiza los datos de una ciudad existente.
     *
     * @param nombreAntiguo El nombre original de la ciudad a modificar.
     * @param ciudadNueva El objeto [Ubicacion] con los nuevos datos.
     * @return `true` si la ciudad existía y fue actualizada, `false` en caso contrario.
     */
    fun actualizarCiudad(nombreAntiguo: String, ciudadNueva: Ubicacion): Boolean {
        val ciudades = cargarCiudades().toMutableList()
        val index = ciudades.indexOfFirst { it.nombre.equals(nombreAntiguo, ignoreCase = true) }
        if (index == -1) return false

        ciudades[index] = ciudadNueva
        guardarCiudades(ciudades)
        return true
    }

    /**
     * Elimina una ciudad por su nombre.
     *
     * @param nombre El nombre de la ciudad a eliminar.
     * @return `true` si la ciudad fue encontrada y eliminada.
     */
    fun eliminarCiudad(nombre: String): Boolean {
        val ciudades = cargarCiudades().toMutableList()
        val removed = ciudades.removeIf { it.nombre.equals(nombre, ignoreCase = true) }
        if (removed) {
            guardarCiudades(ciudades)
        }
        return removed
    }
}

