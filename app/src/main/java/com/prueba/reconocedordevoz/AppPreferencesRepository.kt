package com.prueba.reconocedordevoz

import android.content.Context

/**
 * Repositorio para gestionar las preferencias generales de la aplicación.
 * Actualmente se encarga de rastrear si el usuario ha completado el tutorial inicial.
 *
 * @param context Contexto de la aplicación.
 */
class AppPreferencesRepository(context: Context) {
    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }

    /**
     * Verifica si el usuario ya ha visto y completado el tutorial de bienvenida.
     * @return `true` si ya lo completó, `false` de lo contrario.
     */
    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    /**
     * Marca el tutorial como completado para que no vuelva a aparecer.
     */
    fun setOnboardingCompleted() {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, true).apply()
    }
}
