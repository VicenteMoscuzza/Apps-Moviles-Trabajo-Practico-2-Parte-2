package com.example.appsmovilestrabajopractico2parte2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appsmovilestrabajopractico2parte2.model.CiudadCapital
import com.example.appsmovilestrabajopractico2parte2.ui.theme.AppsMovilesTrabajoPractico2Parte2Theme
import com.example.appsmovilestrabajopractico2parte2.viewmodel.CiudadesViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppsMovilesTrabajoPractico2Parte2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CiudadesApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CiudadesApp(viewModel: CiudadesViewModel = viewModel()) {
    var showDialog by remember { mutableStateOf(false) }
    var dialogType by remember { mutableStateOf<DialogType?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchResults by remember { mutableStateOf(false) }
    var searchResult by remember { mutableStateOf<CiudadCapital?>(null) }

    val ciudades by viewModel.ciudades.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Ciudades Capitales") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { dialogType = DialogType.AGREGAR; showDialog = true }) {
                    Text("Agregar Ciudad")
                }
                Button(onClick = { dialogType = DialogType.BUSCAR; showDialog = true }) {
                    Text("Buscar Ciudad")
                }
                Button(onClick = { dialogType = DialogType.ELIMINAR; showDialog = true }) {
                    Text("Eliminar Ciudad")
                }
                Button(onClick = { dialogType = DialogType.ELIMINAR_PAIS; showDialog = true }) {
                    Text("Eliminar País")
                }
                Button(onClick = { dialogType = DialogType.MODIFICAR_POBLACION; showDialog = true }) {
                    Text("Modificar Población")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de ciudades
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ciudades) { ciudad ->
                    CiudadItem(ciudad = ciudad)
                }
            }
        }

        // Diálogos
        when (dialogType) {
            DialogType.AGREGAR -> AgregarCiudadDialog(
                onDismiss = { showDialog = false },
                onConfirm = { pais, ciudad, poblacion ->
                    viewModel.agregarCiudad(CiudadCapital(pais, ciudad, poblacion.toLong()))
                    showDialog = false
                }
            )
            DialogType.BUSCAR -> BuscarCiudadDialog(
                onDismiss = { showDialog = false },
                onSearch = { nombre ->
                    searchResult = viewModel.buscarCiudad(nombre)
                    showSearchResults = true
                    showDialog = false
                }
            )
            DialogType.ELIMINAR -> EliminarCiudadDialog(
                onDismiss = { showDialog = false },
                onConfirm = { nombre ->
                    viewModel.eliminarCiudad(nombre)
                    showDialog = false
                }
            )
            DialogType.ELIMINAR_PAIS -> EliminarPaisDialog(
                onDismiss = { showDialog = false },
                onConfirm = { pais ->
                    viewModel.eliminarCiudadesPorPais(pais)
                    showDialog = false
                }
            )
            DialogType.MODIFICAR_POBLACION -> ModificarPoblacionDialog(
                onDismiss = { showDialog = false },
                onConfirm = { ciudad, poblacion ->
                    viewModel.modificarPoblacion(ciudad, poblacion.toLong())
                    showDialog = false
                }
            )
            null -> {}
        }

        // Mostrar resultados de búsqueda
        if (showSearchResults && searchResult != null) {
            AlertDialog(
                onDismissRequest = { showSearchResults = false },
                title = { Text("Resultado de la búsqueda") },
                text = {
                    Column {
                        Text("País: ${searchResult?.nombrePais}")
                        Text("Ciudad: ${searchResult?.nombreCiudad}")
                        Text("Población: ${searchResult?.poblacion}")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSearchResults = false }) {
                        Text("Cerrar")
                    }
                }
            )
        }
    }
}

@Composable
fun CiudadItem(ciudad: CiudadCapital) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = ciudad.nombreCiudad,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "País: ${ciudad.nombrePais}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Población: ${ciudad.poblacion}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

enum class DialogType {
    AGREGAR, BUSCAR, ELIMINAR, ELIMINAR_PAIS, MODIFICAR_POBLACION
}

@Composable
fun AgregarCiudadDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var pais by remember { mutableStateOf("") }
    var ciudad by remember { mutableStateOf("") }
    var poblacion by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Ciudad Capital") },
        text = {
            Column {
                OutlinedTextField(
                    value = pais,
                    onValueChange = { pais = it },
                    label = { Text("País") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = ciudad,
                    onValueChange = { ciudad = it },
                    label = { Text("Ciudad") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = poblacion,
                    onValueChange = { poblacion = it },
                    label = { Text("Población") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(pais, ciudad, poblacion) },
                enabled = pais.isNotBlank() && ciudad.isNotBlank() && poblacion.isNotBlank()
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun BuscarCiudadDialog(
    onDismiss: () -> Unit,
    onSearch: (String) -> Unit
) {
    var ciudad by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buscar Ciudad") },
        text = {
            OutlinedTextField(
                value = ciudad,
                onValueChange = { ciudad = it },
                label = { Text("Nombre de la ciudad") }
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSearch(ciudad) },
                enabled = ciudad.isNotBlank()
            ) {
                Text("Buscar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun EliminarCiudadDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var ciudad by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar Ciudad") },
        text = {
            OutlinedTextField(
                value = ciudad,
                onValueChange = { ciudad = it },
                label = { Text("Nombre de la ciudad") }
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(ciudad) },
                enabled = ciudad.isNotBlank()
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun EliminarPaisDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var pais by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar Ciudades por País") },
        text = {
            OutlinedTextField(
                value = pais,
                onValueChange = { pais = it },
                label = { Text("Nombre del país") }
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(pais) },
                enabled = pais.isNotBlank()
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ModificarPoblacionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var ciudad by remember { mutableStateOf("") }
    var poblacion by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modificar Población") },
        text = {
            Column {
                OutlinedTextField(
                    value = ciudad,
                    onValueChange = { ciudad = it },
                    label = { Text("Nombre de la ciudad") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = poblacion,
                    onValueChange = { poblacion = it },
                    label = { Text("Nueva población") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(ciudad, poblacion) },
                enabled = ciudad.isNotBlank() && poblacion.isNotBlank()
            ) {
                Text("Modificar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}