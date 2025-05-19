package com.example.appsmovilestrabajopractico2parte2.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appsmovilestrabajopractico2parte2.model.CiudadCapital
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CiudadesViewModel : ViewModel() {
    private val _ciudades = MutableStateFlow<List<CiudadCapital>>(emptyList())
    val ciudades: StateFlow<List<CiudadCapital>> = _ciudades.asStateFlow()

    fun agregarCiudad(ciudad: CiudadCapital) {
        val listaActual = _ciudades.value.toMutableList()
        listaActual.add(ciudad)
        _ciudades.value = listaActual
    }

    fun buscarCiudad(nombreCiudad: String): CiudadCapital? {
        return _ciudades.value.find { it.nombreCiudad.equals(nombreCiudad, ignoreCase = true) }
    }

    fun eliminarCiudad(nombreCiudad: String): Boolean {
        val listaActual = _ciudades.value.toMutableList()
        val ciudad = listaActual.find { it.nombreCiudad.equals(nombreCiudad, ignoreCase = true) }
        return if (ciudad != null) {
            listaActual.remove(ciudad)
            _ciudades.value = listaActual
            true
        } else {
            false
        }
    }

    fun eliminarCiudadesPorPais(nombrePais: String): Int {
        val listaActual = _ciudades.value.toMutableList()
        val ciudadesAEliminar = listaActual.filter { it.nombrePais.equals(nombrePais, ignoreCase = true) }
        listaActual.removeAll(ciudadesAEliminar)
        _ciudades.value = listaActual
        return ciudadesAEliminar.size
    }

    fun modificarPoblacion(nombreCiudad: String, nuevaPoblacion: Long): Boolean {
        val listaActual = _ciudades.value.toMutableList()
        val ciudad = listaActual.find { it.nombreCiudad.equals(nombreCiudad, ignoreCase = true) }
        return if (ciudad != null) {
            ciudad.poblacion = nuevaPoblacion
            _ciudades.value = listaActual
            true
        } else {
            false
        }
    }
} 