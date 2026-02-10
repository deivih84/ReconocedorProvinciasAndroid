package com.prueba.reconocedordevoz

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Pantalla principal de la aplicación.
 * Muestra el estado actual del reconocimiento de voz y los resultados obtenidos.
 *
 * @param uiState Estado actual de la UI (palabra reconocida, código, errores, etc.).
 * @param onStartListening Acción a ejecutar cuando se pulsa el botón de escuchar.
 * @param onGestionarCiudades Acción a ejecutar para navegar a la pantalla de gestión de ciudades.
 */
@Composable
fun MainScreen(
    uiState: UiState,
    onStartListening: () -> Unit,
    onGestionarCiudades: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            SmallFloatingActionButton(
                onClick = onGestionarCiudades,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Settings, contentDescription = "Gestionar Ciudades")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Header()

            ResultDisplay(
                palabra = uiState.palabraReconocida,
                codigo = uiState.codigoEncontrado
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                StatusText(
                    message = if (uiState.isListening) "Te escucho..." else "Listo para empezar"
                )

                Spacer(modifier = Modifier.height(24.dp))

                ListenButton(
                    isListening = uiState.isListening,
                    onClick = onStartListening
                )
            }
            
            // Espacio extra inferior para equilibrar visualmente
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Botón principal para activar la escucha con animación de pulsación.
 *
 * @param isListening Indica si actualmente se está escuchando (activa la animación).
 * @param onClick Acción al pulsar.
 */
@Composable
fun ListenButton(isListening: Boolean, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(contentAlignment = Alignment.Center) {
        if (isListening) {
            // Círculo de fondo que expande para efecto de onda
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            )
        }
        
        Button(
            onClick = onClick,
            enabled = !isListening,
            shape = CircleShape,
            modifier = Modifier
                .size(160.dp)
                .scale(if (isListening) 1f else 1f), // Mantenemos el botón estable, la onda es externa
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 2.dp
            )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isListening) "OYENDO..." else "HABLAR",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

/**
 * Cabecera de la aplicación.
 */
@Composable
fun Header() {
    Text(
        text = "Reconocedor de Voz (Google)",
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center
    )
}

/**
 * Componente que muestra el resultado del reconocimiento.
 *
 * @param palabra La palabra que se ha reconocido.
 * @param codigo El código asociado encontrado.
 */
@Composable
fun ResultDisplay(palabra: String, codigo: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Palabra Reconocida:", style = MaterialTheme.typography.titleMedium)
            Text(
                text = palabra,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider( // Horizontal Divider god
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )
            Text(text = "Código Asociado:", style = MaterialTheme.typography.titleMedium)
            Text(
                text = codigo,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

/**
 * Muestra el estado textual actual.
 * @param message El mensaje a mostrar.
 */
@Composable
fun StatusText(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}