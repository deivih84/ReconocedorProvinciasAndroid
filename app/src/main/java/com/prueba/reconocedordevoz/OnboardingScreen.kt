package com.prueba.reconocedordevoz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * Datos para cada página del tutorial.
 */
data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: androidx.compose.ui.graphics.Color
)

/**
 * Pantalla de bienvenida y tutorial para nuevos usuarios.
 * Utiliza un HorizontalPager para mostrar los pasos de forma fluida.
 *
 * @param onFinished Callback que se ejecuta cuando el usuario termina el tutorial.
 */
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pages = listOf(
        OnboardingPage(
            title = "¡Bienvenido!",
            description = "Esta app te permite identificar ciudades mediante tu voz y obtener sus códigos asociados de forma instantánea.",
            icon = Icons.Filled.Mic,
            color = MaterialTheme.colorScheme.primary
        ),
        OnboardingPage(
            title = "Gestión Personalizada",
            description = "Puedes añadir tus propias ciudades y códigos en la sección de gestión para adaptar la app a tus necesidades.",
            icon = Icons.Filled.AddLocation,
            color = MaterialTheme.colorScheme.secondary
        ),
        OnboardingPage(
            title = "Fácil de Usar",
            description = "Pulsa el botón central, di el nombre de una ciudad registrada y ¡listo! La magia de Google hará el resto.",
            icon = Icons.Filled.Search,
            color = MaterialTheme.colorScheme.tertiary
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pager con las páginas del tutorial
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { position ->
            OnboardingPageItem(page = pages[position])
        }

        // Indicadores de página (puntos inferiores)
        Row(
            Modifier
                .height(50.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pages.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botones de navegación
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón omitir
            TextButton(onClick = onFinished) {
                Text("Omitir")
            }

            // Botón Siguiente / Empezar
            Button(
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onFinished()
                    }
                },
                modifier = Modifier.width(140.dp)
            ) {
                Text(if (pagerState.currentPage == pages.size - 1) "EMPEZAR" else "SIGUIENTE")
            }
        }
    }
}

/**
 * Representa visualmente una página individual del tutorial.
 */
@Composable
fun OnboardingPageItem(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(150.dp),
            shape = CircleShape,
            color = page.color.copy(alpha = 0.1f)
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxSize(),
                tint = page.color
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp
        )
    }
}
