package com.prueba.reconocedordevoz

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainScreen(
    uiState: UiState,
    onStartListening: () -> Unit,
    onGestionarCiudades: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Header()
        ResultDisplay(
            palabra = uiState.palabraReconocida,
            codigo = uiState.codigoEncontrado
        )

        val statusMessage = if (uiState.isListening) {
            "Esperando resultado de Google..."
        } else {
            "Pulsa el botón para hablar"
        }
        StatusText(message = statusMessage)

        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        ListenButton(
            isListening = uiState.isListening,
            onClick = onStartListening
        )

        OutlinedButton(
            onClick = onGestionarCiudades,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("GESTIONAR CIUDADES", fontSize = 16.sp)
        }
    }
}

// Solo depende de un booleano, on god🌹
@Composable
fun ListenButton(isListening: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !isListening,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Text(
            text = if (isListening) "ESCUCHANDO..." else "PULSA PARA HABLAR",
            fontSize = 18.sp
        )
    }
}

@Composable
fun Header() {
    Text(
        text = "Reconocedor de Voz (Google)",
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center
    )
}

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

@Composable
fun StatusText(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}