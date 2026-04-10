package com.example.habittrackerapp.ui

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.habittrackerapp.R
import com.example.habittrackerapp.data.CategoryRepository
import com.example.habittrackerapp.data.HabitCategory
import com.example.habittrackerapp.data.HabitRepository
import com.example.habittrackerapp.databinding.ActivityEditHabitBinding
import kotlinx.coroutines.launch

class EditHabitActivity : AppCompatActivity() {
    // Referenciamos a la clase de 'ViewBinding' para acceder a los componentes de la interfaz sin usar 'findViewById'
    private lateinit var binding: ActivityEditHabitBinding
    // Declaramos los repositorios de categorías y hábitos
    private val habitRepository    = HabitRepository()
    private val categoryRepository = CategoryRepository()
    // Declaramos los estados globales del formulario
    private var habitoId              = ""
    private var esDiario              = true
    private var categorias            = listOf<HabitCategory>()
    private var categoriaSeleccionada : HabitCategory? = null
    private val diasSeleccionados     = mutableMapOf<TextView, Boolean>()
    companion object {
        const val EXTRA_HABIT_ID          = "habit_id"
        const val EXTRA_HABIT_NAME        = "habit_name"
        const val EXTRA_HABIT_FREQUENCY   = "habit_frequency"
        const val EXTRA_HABIT_CATEGORY_ID = "habit_category_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditHabitBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Recibimos los datos actuales del hábito
        habitoId = intent.getStringExtra(EXTRA_HABIT_ID)        ?: ""
        val nombreActual    = intent.getStringExtra(EXTRA_HABIT_NAME)        ?: ""
        val frecuenciaActual = intent.getStringExtra(EXTRA_HABIT_FREQUENCY)  ?: ""
        val categoriaIdActual = intent.getStringExtra(EXTRA_HABIT_CATEGORY_ID) ?: ""
        // Prellenamos el campo de nombre con el valor actual
        binding.edtHabitName.setText(nombreActual)
        // Determinamos si la frecuencia actual es diaria o personalizada
        val esDiarioActual = frecuenciaActual == getString(R.string.frequency_daily)
        inicializarChipsDias()
        seleccionarFrecuencia(esDiarioActual)
        // Si era frecuencia personalizada, marcamos los días que tenía
        if (!esDiarioActual) {
            val diasActuales = frecuenciaActual.split(" - ")
            val mapaChips = mapOf(
                "Lun" to binding.chipLun, "Mar" to binding.chipMar,
                "Mié" to binding.chipMie, "Jue" to binding.chipJue,
                "Vie" to binding.chipVie, "Sáb" to binding.chipSab,
                "Dom" to binding.chipDom
            )
            diasActuales.forEach { dia ->
                mapaChips[dia]?.let { chip -> toggleDia(chip) }
            }
        }
        // Cargamos las categorías y preseleccionamos la actual
        cargarCategorias(categoriaIdActual)
        // Declaramos los eventos del selector de frecuencia
        binding.btnFreqDaily.setOnClickListener  { seleccionarFrecuencia(true)  }
        binding.btnFreqCustom.setOnClickListener { seleccionarFrecuencia(false) }
        // Declaramos los eventos del botón de guardar
        binding.btnSave.setOnClickListener {
            if (validarFormulario()) {
                guardarCambios()
            }
        }
        // Limpiamos error del nombre mientras el usuario escribe
        binding.edtHabitName.setOnFocusChangeListener { _, _ ->
            binding.tilHabitName.error = null
        }
        binding.btnBack.setOnClickListener { finish() }
    }
    // Cargamos las categorías y marcamos la que tenía el hábito
    private fun cargarCategorias(categoriaIdActual: String) {
        lifecycleScope.launch {
            val resultado = categoryRepository.obtenerCategorias()
            resultado.fold(
                onSuccess = { lista ->
                    categorias = lista
                    val nombres = lista.map { it.nombre }
                    val adapter = ArrayAdapter(
                        this@EditHabitActivity,
                        android.R.layout.simple_dropdown_item_1line,
                        nombres
                    )
                    binding.actvCategory.setAdapter(adapter)
                    // Preseleccionamos la categoría actual si existe
                    val categoriaActual = lista.find { it.id == categoriaIdActual }
                    if (categoriaActual != null) {
                        binding.actvCategory.setText(categoriaActual.nombre, false)
                        categoriaSeleccionada = categoriaActual
                    }

                    binding.actvCategory.setOnItemClickListener { _, _, position, _ ->
                        categoriaSeleccionada = lista[position]
                    }
                },
                onFailure = { }
            )
        }
    }
    // Inicializamos el mapa de chips de días
    private fun inicializarChipsDias() {
        val chips = listOf(
            binding.chipLun, binding.chipMar, binding.chipMie, binding.chipJue,
            binding.chipVie, binding.chipSab, binding.chipDom
        )
        chips.forEach { chip ->
            diasSeleccionados[chip] = false
            chip.setOnClickListener { toggleDia(chip) }
        }
    }
    // Declaramos el toggle para el modo visual y lógico de un chip de día
    private fun toggleDia(chip: TextView) {
        val seleccionado = diasSeleccionados[chip] ?: false
        diasSeleccionados[chip] = !seleccionado

        if (!seleccionado) {
            chip.background = getDrawable(R.drawable.bg_chip_day_selected)
            chip.setTextColor(getColor(R.color.on_accent))
        } else {
            chip.background = getDrawable(R.drawable.bg_chip_day_unselected)
            chip.setTextColor(getColor(R.color.text_secondary))
        }
        binding.tvDaysError.visibility = View.GONE
    }
    // Actualizamos el modo de frecuencia
    private fun seleccionarFrecuencia(diario: Boolean) {
        esDiario = diario
        if (diario) {
            aplicarEstiloChipActivo(binding.btnFreqDaily)
            aplicarEstiloChipInactivo(binding.btnFreqCustom)
            binding.layoutDays.visibility = View.GONE
        } else {
            aplicarEstiloChipActivo(binding.btnFreqCustom)
            aplicarEstiloChipInactivo(binding.btnFreqDaily)
            binding.layoutDays.visibility = View.VISIBLE
        }
    }
    private fun aplicarEstiloChipActivo(boton: com.google.android.material.button.MaterialButton) {
        boton.backgroundTintList = ColorStateList.valueOf(getColor(R.color.accent))
        boton.strokeColor        = ColorStateList.valueOf(getColor(R.color.accent))
        boton.setTextColor(getColor(R.color.on_accent))
    }
    private fun aplicarEstiloChipInactivo(boton: com.google.android.material.button.MaterialButton) {
        boton.backgroundTintList = ColorStateList.valueOf(getColor(android.R.color.transparent))
        boton.strokeColor        = ColorStateList.valueOf(getColor(R.color.divider_color))
        boton.setTextColor(getColor(R.color.text_secondary))
    }
    // Validamos el formulario antes de guardar
    private fun validarFormulario(): Boolean {
        val nombre   = binding.edtHabitName.text.toString().trim()
        var esValido = true

        if (nombre.isEmpty()) {
            binding.tilHabitName.error = getString(R.string.error_habit_name_empty)
            esValido = false
        } else {
            binding.tilHabitName.error = null
        }

        if (!esDiario && !diasSeleccionados.values.any { it }) {
            binding.tvDaysError.visibility = View.VISIBLE
            esValido = false
        }

        return esValido
    }
    // Guardamos los cambios en Firestore
    private fun guardarCambios() {
        val nombre = binding.edtHabitName.text.toString().trim()

        val frecuencia = if (esDiario) {
            getString(R.string.frequency_daily)
        } else {
            val etiquetas = mapOf(
                binding.chipLun to "Lun", binding.chipMar to "Mar",
                binding.chipMie to "Mié", binding.chipJue to "Jue",
                binding.chipVie to "Vie", binding.chipSab to "Sáb",
                binding.chipDom to "Dom"
            )
            diasSeleccionados.filter { it.value }.keys
                .mapNotNull { etiquetas[it] }
                .joinToString(" - ")
        }

        val diasLista = if (esDiario) {
            listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
        } else {
            val etiquetas = mapOf(
                binding.chipLun to "Lun", binding.chipMar to "Mar",
                binding.chipMie to "Mié", binding.chipJue to "Jue",
                binding.chipVie to "Vie", binding.chipSab to "Sáb",
                binding.chipDom to "Dom"
            )
            diasSeleccionados.filter { it.value }.keys
                .mapNotNull { etiquetas[it] }
        }

        binding.btnSave.isEnabled = false
        binding.btnSave.text      = getString(R.string.btn_update_loading)

        lifecycleScope.launch {
            val resultado = habitRepository.actualizarHabito(
                habitoId      = habitoId,
                nombre        = nombre,
                frecuencia    = frecuencia,
                diasSemana    = diasLista,
                categoriaId   = categoriaSeleccionada?.id ?: ""
            )
            resultado.fold(
                onSuccess = { finish() },
                onFailure = {
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text      = getString(R.string.btn_update_habit)
                }
            )
        }
    }
}
