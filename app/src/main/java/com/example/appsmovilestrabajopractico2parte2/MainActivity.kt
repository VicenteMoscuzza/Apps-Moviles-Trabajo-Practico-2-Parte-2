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
                Button(onClick = { 
                    dialogType = DialogType.AGREGAR
                    showDialog = true 
                }) {
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
                onDismiss = { 
                    showDialog = false
                    dialogType = null
                },
                onConfirm = { pais, ciudad, poblacion ->
                    try {
                        val poblacionLong = poblacion.toLong()
                        viewModel.agregarCiudad(CiudadCapital(pais, ciudad, poblacionLong))
                        showDialog = false
                        dialogType = null
                    } catch (e: NumberFormatException) {
                        // Manejar error de formato de número si es necesario
                    }
                }
            )
            DialogType.BUSCAR -> BuscarCiudadDialog(
                onDismiss = { 
                    showDialog = false
                    dialogType = null
                },
                onSearch = { nombre ->
                    searchResult = viewModel.buscarCiudad(nombre)
                    showSearchResults = true
                    showDialog = false
                    dialogType = null
                }
            )
            DialogType.ELIMINAR -> EliminarCiudadDialog(
                onDismiss = { 
                    showDialog = false
                    dialogType = null
                },
                onConfirm = { nombre ->
                    viewModel.eliminarCiudad(nombre)
                    showDialog = false
                    dialogType = null
                }
            )
            DialogType.ELIMINAR_PAIS -> EliminarPaisDialog(
                onDismiss = { 
                    showDialog = false
                    dialogType = null
                },
                onConfirm = { pais ->
                    viewModel.eliminarCiudadesPorPais(pais)
                    showDialog = false
                    dialogType = null
                }
            )
            DialogType.MODIFICAR_POBLACION -> ModificarPoblacionDialog(
                onDismiss = { 
                    showDialog = false
                    dialogType = null
                },
                onConfirm = { ciudad, poblacion ->
                    viewModel.modificarPoblacion(ciudad, poblacion.toLong())
                    showDialog = false
                    dialogType = null
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
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Ciudad Capital") },
        text = {
            Column {
                OutlinedTextField(
                    value = pais,
                    onValueChange = { 
                        pais = it
                        showError = false
                    },
                    label = { Text("País") },
                    isError = showError && pais.isBlank()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = ciudad,
                    onValueChange = { 
                        ciudad = it
                        showError = false
                    },
                    label = { Text("Ciudad") },
                    isError = showError && ciudad.isBlank()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = poblacion,
                    onValueChange = { 
                        poblacion = it
                        showError = false
                    },
                    label = { Text("Población") },
                    isError = showError && (poblacion.isBlank() || !poblacion.all { it.isDigit() })
                )
                if (showError) {
                    Text(
                        text = "Por favor complete todos los campos correctamente",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (pais.isNotBlank() && ciudad.isNotBlank() && poblacion.isNotBlank() && poblacion.all { it.isDigit() }) {
                        onConfirm(pais, ciudad, poblacion)
                    } else {
                        showError = true
                    }
                }
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
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buscar Ciudad") },
        text = {
            Column {
                OutlinedTextField(
                    value = ciudad,
                    onValueChange = { 
                        ciudad = it
                        showError = false
                    },
                    label = { Text("Nombre de la ciudad") },
                    isError = showError && ciudad.isBlank()
                )
                if (showError) {
                    Text(
                        text = "Por favor ingrese el nombre de la ciudad",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (ciudad.isNotBlank()) {
                        onSearch(ciudad)
                    } else {
                        showError = true
                    }
                }
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
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar Ciudad") },
        text = {
            Column {
                OutlinedTextField(
                    value = ciudad,
                    onValueChange = { 
                        ciudad = it
                        showError = false
                    },
                    label = { Text("Nombre de la ciudad") },
                    isError = showError && ciudad.isBlank()
                )
                if (showError) {
                    Text(
                        text = "Por favor ingrese el nombre de la ciudad",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (ciudad.isNotBlank()) {
                        onConfirm(ciudad)
                    } else {
                        showError = true
                    }
                }
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
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar Ciudades por País") },
        text = {
            Column {
                OutlinedTextField(
                    value = pais,
                    onValueChange = { 
                        pais = it
                        showError = false
                    },
                    label = { Text("Nombre del país") },
                    isError = showError && pais.isBlank()
                )
                if (showError) {
                    Text(
                        text = "Por favor ingrese el nombre del país",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (pais.isNotBlank()) {
                        onConfirm(pais)
                    } else {
                        showError = true
                    }
                }
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
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modificar Población") },
        text = {
            Column {
                OutlinedTextField(
                    value = ciudad,
                    onValueChange = { 
                        ciudad = it
                        showError = false
                    },
                    label = { Text("Nombre de la ciudad") },
                    isError = showError && ciudad.isBlank()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = poblacion,
                    onValueChange = { 
                        poblacion = it
                        showError = false
                    },
                    label = { Text("Nueva población") },
                    isError = showError && (poblacion.isBlank() || !poblacion.all { it.isDigit() })
                )
                if (showError) {
                    Text(
                        text = "Por favor complete todos los campos correctamente",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (ciudad.isNotBlank() && poblacion.isNotBlank() && poblacion.all { it.isDigit() }) {
                        onConfirm(ciudad, poblacion)
                    } else {
                        showError = true
                    }
                }
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