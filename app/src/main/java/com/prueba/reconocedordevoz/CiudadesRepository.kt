package com.prueba.reconocedordevoz

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

class CiudadesRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("ciudades_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_CIUDADES = "ciudades_list"
        private const val KEY_INITIALIZED = "initialized"
    }

    // Cargar ciudades (primero de SharedPreferences, si no existe, del JSON)
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

    // Guardar ciudades en SharedPreferences
    fun guardarCiudades(ciudades: List<Ubicacion>) {
        val json = gson.toJson(ciudades)
        prefs.edit().putString(KEY_CIUDADES, json).apply()
    }

    // Cargar desde el archivo JSON de assets (solo primera vez)
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

    // Añadir una nueva ciudad
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

    // Actualizar una ciudad existente
    fun actualizarCiudad(nombreAntiguo: String, ciudadNueva: Ubicacion): Boolean {
        val ciudades = cargarCiudades().toMutableList()
        val index = ciudades.indexOfFirst { it.nombre.equals(nombreAntiguo, ignoreCase = true) }
        if (index == -1) return false

        ciudades[index] = ciudadNueva
        guardarCiudades(ciudades)
        return true
    }

    // Eliminar una ciudad
    fun eliminarCiudad(nombre: String): Boolean {
        val ciudades = cargarCiudades().toMutableList()
        val removed = ciudades.removeIf { it.nombre.equals(nombre, ignoreCase = true) }
        if (removed) {
            guardarCiudades(ciudades)
        }
        return removed
    }
}

