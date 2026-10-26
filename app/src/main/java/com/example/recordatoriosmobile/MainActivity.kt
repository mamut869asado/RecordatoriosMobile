package com.example.recordatoriosmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// Alternativas posibles para la categoría.
sealed class Categoria {
    object Estudio : Categoria()
    object Personal : Categoria()
    object Finanzas : Categoria()
}

// Alternativas posibles para la prioridad.
sealed class Prioridad(val texto: String) {
    object Alta : Prioridad("Alta")
    object Media : Prioridad("Media")
    object Baja : Prioridad("Baja")
}

// Alternativas posibles para el estado de cada recordatorio.
sealed class EstadoRecordatorio(val texto: String) {
    object Pendiente : EstadoRecordatorio("Pendiente")
    object Completado : EstadoRecordatorio("Completado")
}

// Modelo inmutable. La nota es opcional: puede ser null.
data class Recordatorio(
    val id: Int,
    val titulo: String,
    val categoria: Categoria,
    val prioridad: Prioridad,
    val estado: EstadoRecordatorio,
    val nota: String? = null
)

// Estados posibles de la pantalla.
sealed class EstadoPantalla {
    data class ConDatos(val recordatorios: List<Recordatorio>) : EstadoPantalla()
    object Vacia : EstadoPantalla()
    data class Error(val mensaje: String) : EstadoPantalla()
}

// Datos de ejemplo: no se necesita base de datos en la API 1.
fun recordatoriosSimulados(): List<Recordatorio> = listOf(
    Recordatorio(1, "Estudiar Kotlin", Categoria.Estudio, Prioridad.Alta, EstadoRecordatorio.Pendiente),
    Recordatorio(2, "Entregar la API 1", Categoria.Estudio, Prioridad.Alta, EstadoRecordatorio.Pendiente, "Antes del 28/10"),
    Recordatorio(3, "Comprar alimentos", Categoria.Personal, Prioridad.Media, EstadoRecordatorio.Pendiente),
    Recordatorio(4, "Pagar internet", Categoria.Finanzas, Prioridad.Alta, EstadoRecordatorio.Completado),
    Recordatorio(5, "Ordenar apuntes", Categoria.Estudio, Prioridad.Baja, EstadoRecordatorio.Completado)
)

// Validación básica para evitar datos inválidos.
fun esValido(recordatorio: Recordatorio): Boolean =
    recordatorio.id > 0 && recordatorio.titulo.isNotBlank()

// Filtros solicitados por la consigna.
fun filtrarPorEstado(
    lista: List<Recordatorio>, estado: EstadoRecordatorio
): List<Recordatorio> = lista.filter { it.estado == estado }

fun filtrarPorPrioridad(
    lista: List<Recordatorio>, prioridad: Prioridad
): List<Recordatorio> = lista.filter { it.prioridad == prioridad }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PantallaRecordatorios()
                }
            }
        }
    }
}

@Composable
fun PantallaRecordatorios() {
    val recordatorios = recordatoriosSimulados()

    // Set: las categorías repetidas se cuentan una sola vez.
    val categoriasUnicas: Set<Categoria> = recordatorios.map { it.categoria }.toSet()

    // Map: cada categoría se relaciona con una etiqueta.
    val etiquetasPorCategoria: Map<Categoria, String> = mapOf(
        Categoria.Estudio to "Estudio",
        Categoria.Personal to "Personal",
        Categoria.Finanzas to "Finanzas"
    )

    val pendientes = filtrarPorEstado(recordatorios, EstadoRecordatorio.Pendiente)
    val prioridadAlta = filtrarPorPrioridad(recordatorios, Prioridad.Alta)

    val estadoPantalla: EstadoPantalla = when {
        recordatorios.any { !esValido(it) } -> EstadoPantalla.Error("Hay datos inválidos")
        recordatorios.isEmpty() -> EstadoPantalla.Vacia
        else -> EstadoPantalla.ConDatos(recordatorios)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Mis recordatorios", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Pendientes: ${pendientes.size} · Prioridad alta: ${prioridadAlta.size}")
        Text("Categorías diferentes: ${categoriasUnicas.size}")
        Spacer(modifier = Modifier.height(16.dp))

        when (estadoPantalla) {
            is EstadoPantalla.ConDatos -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(estadoPantalla.recordatorios, key = { it.id }) { recordatorio ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(recordatorio.titulo, fontWeight = FontWeight.Bold)
                                Text("Categoría: ${etiquetasPorCategoria[recordatorio.categoria] ?: "Otra"}")
                                Text("Prioridad: ${recordatorio.prioridad.texto}")
                                Text("Estado: ${recordatorio.estado.texto}")
                                recordatorio.nota?.let { Text("Nota: $it") }
                            }
                        }
                    }
                }
            }
            EstadoPantalla.Vacia -> Text("No hay recordatorios para mostrar")
            is EstadoPantalla.Error -> Text(estadoPantalla.mensaje)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VistaPreviaRecordatorios() {
    MaterialTheme {
        PantallaRecordatorios()
    }
}
