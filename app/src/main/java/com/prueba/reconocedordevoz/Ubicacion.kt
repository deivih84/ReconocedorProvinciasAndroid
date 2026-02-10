package com.prueba.reconocedordevoz

/**
 * Representa una ubicación geográfica o ciudad con su código asociado.
 *
 * @property nombre El nombre de la ciudad o ubicación (ej. "Madrid").
 * @property codigo El código único asociado a dicha ubicación (ej. "ES-MAD").
 */
data class Ubicacion(
    val nombre: String,
    val codigo: String
)