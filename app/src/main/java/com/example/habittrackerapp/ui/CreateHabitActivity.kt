package com.example.habittrackerapp.ui

import androidx.lifecycle.lifecycleScope
import com.example.habittrackerapp.data.HabitRepository
import kotlinx.coroutines.launch
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.habittrackerapp.R
import com.example.habittrackerapp.databinding.ActivityCreateHabitBinding

class CreateHabitActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateHabitBinding    // Creamos el binding
    private val habitRepository = HabitRepository()    // Declaramos el repositorio de hábitos
    private var esDiario = true    // 'esDiario' controla el modo de frecuencia seleccionado
    // Mapa de chip -> estado seleccionado (false = no seleccionado)
    private val diasSeleccionados = mutableMapOf<TextView, Boolean>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateHabitBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Inicializamos el mapa de días con sus TextViews
        inicializarChipsDias()
        // Selector de frecuencia
        binding.btnFreqDaily.setOnClickListener  { seleccionarFrecuencia(diario = true) }
        binding.btnFreqCustom.setOnClickListener { seleccionarFrecuencia(diario = false) }
        // Declaramos el evento de 'guardar'
        binding.btnSave.setOnClickListener {
            if (validarFormulario()) {
                guardarHabito()
            }
        }
        // Limpiamos error del nombre mientras el usuario escribe
        binding.edtHabitName.setOnFocusChangeListener { _, _ ->
            binding.tilHabitName.error = null
        }
    }
    // Registramos los chips de días en el mapa y asignamos su comportamiento de toggle
    private fun inicializarChipsDias() {
        val chips = listOf(
            binding.chipLun,
            binding.chipMar,
            binding.chipMie,
            binding.chipJue,
            binding.chipVie,
            binding.chipSab,
            binding.chipDom
        )
        chips.forEach { chip ->
            diasSeleccionados[chip] = false
            chip.setOnClickListener { toggleDia(chip) }
        }
    }
    // Cambiamos el estado visual y lógico de un chip de día al tocarlo
    private fun toggleDia(chip: TextView) {
        val seleccionado = diasSeleccionados[chip] ?: false
        diasSeleccionados[chip] = !seleccionado

        if (!seleccionado) {
            // Si se activa el chip (fondo terracota y texto blanco)
            chip.background    = getDrawable(R.drawable.bg_chip_day_selected)
            chip.setTextColor(getColor(R.color.on_accent))
        } else {
            // Si se desactiva el chip (fondo transparente y texto gris)
            chip.background    = getDrawable(R.drawable.bg_chip_day_unselected)
            chip.setTextColor(getColor(R.color.text_secondary))
        }
        // Ocultamos el error de días si el usuario selecciona alguno
        binding.tvDaysError.visibility = View.GONE
    }
    // Cambiamos la frecuencia seleccionada y actualizamos el estado visual de los botones
    private fun seleccionarFrecuencia(diario: Boolean) {
        esDiario = diario

        if (diario) {
            // Activamos 'Todos los días'
            aplicarEstiloChipActivo(binding.btnFreqDaily)
            aplicarEstiloChipInactivo(binding.btnFreqCustom)
            binding.layoutDays.visibility = View.GONE
        } else {
            // Activamos 'Días específicos'
            aplicarEstiloChipActivo(binding.btnFreqCustom)
            aplicarEstiloChipInactivo(binding.btnFreqDaily)
            binding.layoutDays.visibility = View.VISIBLE
        }
    }
    // Definimos el respectivo estilo para el botón de frecuencia activo
    private fun aplicarEstiloChipActivo(boton: com.google.android.material.button.MaterialButton) {
        boton.backgroundTintList = ColorStateList.valueOf(getColor(R.color.accent))
        boton.strokeColor        = ColorStateList.valueOf(getColor(R.color.accent))
        boton.setTextColor(getColor(R.color.on_accent))
    }
    // Definimos el respectivo estilo para el botón de frecuencia inactivo
    private fun aplicarEstiloChipInactivo(boton: com.google.android.material.button.MaterialButton) {
        boton.backgroundTintList = ColorStateList.valueOf(getColor(android.R.color.transparent))
        boton.strokeColor        = ColorStateList.valueOf(getColor(R.color.divider_color))
        boton.setTextColor(getColor(R.color.text_secondary))
    }
    // Validamos el formulario completo antes de guardar
    private fun validarFormulario(): Boolean {
        val nombre   = binding.edtHabitName.text.toString().trim()
        var esValido = true
        // Validamos que el nombre no esté vacío
        if (nombre.isEmpty()) {
            binding.tilHabitName.error = getString(R.string.error_habit_name_empty)
            esValido = false
        } else {
            binding.tilHabitName.error = null
        }
        // Si eligió días específicos, validamos que haya al menos uno seleccionado
        if (!esDiario) {
            val hayDiaSeleccionado = diasSeleccionados.values.any { it }
            if (!hayDiaSeleccionado) {
                binding.tvDaysError.visibility = View.VISIBLE
                esValido = false
            }
        }
        return esValido
    }
    // Declaramos 'guardarHabito' para guardar el hábito en Firestore
    private fun guardarHabito() {
        val nombre     = binding.edtHabitName.text.toString().trim()
        val frecuencia = if (esDiario) {
            getString(R.string.frequency_daily)
        } else {
            // Construimos el string de días seleccionados
            val etiquetas = mapOf(
                binding.chipLun to "Lun",
                binding.chipMar to "Mar",
                binding.chipMie to "Mié",
                binding.chipJue to "Jue",
                binding.chipVie to "Vie",
                binding.chipSab to "Sáb",
                binding.chipDom to "Dom"
            )
            diasSeleccionados
                .filter { it.value }
                .keys
                .mapNotNull { etiquetas[it] }
                .joinToString(" - ")
        }

        val diasLista = if (esDiario) {
            listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
        } else {
            val etiquetas = mapOf(
                binding.chipLun to "Lun",
                binding.chipMar to "Mar",
                binding.chipMie to "Mié",
                binding.chipJue to "Jue",
                binding.chipVie to "Vie",
                binding.chipSab to "Sáb",
                binding.chipDom to "Dom"
            )
            diasSeleccionados
                .filter { it.value }
                .keys
                .mapNotNull { etiquetas[it] }
        }

        binding.btnSave.isEnabled = false
        binding.btnSave.text      = getString(R.string.btn_save_loading)

        lifecycleScope.launch {
            val resultado = habitRepository.guardarHabito(nombre, frecuencia, diasLista)

            resultado.fold(
                onSuccess = {
                    finish()    // Hábito guardado exitosamente
                },
                onFailure = {    // Error al guardar hábito, restauramos botón
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text      = getString(R.string.btn_save_habit)
                }
            )
        }
    }
}
