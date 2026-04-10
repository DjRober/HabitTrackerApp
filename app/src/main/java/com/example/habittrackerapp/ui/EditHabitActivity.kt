package com.example.habittrackerapp.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.habittrackerapp.R
import com.example.habittrackerapp.data.CategoryRepository
import com.example.habittrackerapp.data.HabitCategory
import com.example.habittrackerapp.data.HabitRepository
import com.example.habittrackerapp.databinding.ActivityEditHabitBinding
import kotlinx.coroutines.launch

class EditHabitActivity : AppCompatActivity() {    // Declaramos el fragmento de edición de hábitos
    // Declaramos las variables de estado
    private lateinit var binding: ActivityEditHabitBinding
    private lateinit var formManager: HabitFormManager
    // Declaramos los repositorios de hábitos y categorías
    private val habitRepository    = HabitRepository()
    private val categoryRepository = CategoryRepository()
    // Declaramos las variables de estado
    private var habitoId              = ""
    private var categorias            = listOf<HabitCategory>()
    private var categoriaSeleccionada: HabitCategory? = null
    // Declaramos las constantes de Intent
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

        habitoId = intent.getStringExtra(EXTRA_HABIT_ID)      ?: ""
        val nombreActual      = intent.getStringExtra(EXTRA_HABIT_NAME)        ?: ""
        val frecuenciaActual  = intent.getStringExtra(EXTRA_HABIT_FREQUENCY)   ?: ""
        val categoriaIdActual = intent.getStringExtra(EXTRA_HABIT_CATEGORY_ID) ?: ""

        binding.edtHabitName.setText(nombreActual)

        formManager = HabitFormManager(
            context       = this,
            chipData      = listOf(
                binding.chipLun to "Lun",
                binding.chipMar to "Mar",
                binding.chipMie to "Mié",
                binding.chipJue to "Jue",
                binding.chipVie to "Vie",
                binding.chipSab to "Sáb",
                binding.chipDom to "Dom"
            ),
            btnFreqDaily  = binding.btnFreqDaily,
            btnFreqCustom = binding.btnFreqCustom,
            layoutDays    = binding.layoutDays,
            tvDaysError   = binding.tvDaysError
        )
        // Configuramos el modo de frecuencia inicial
        val esDiarioActual = frecuenciaActual == getString(R.string.frequency_daily)
        formManager.inicializarFrecuencia(esDiarioActual)

        if (!esDiarioActual) {
            formManager.preseleccionarDias(frecuenciaActual.split(" - "))
        }

        cargarCategorias(categoriaIdActual)    // Cargamos las categorías desde Firestore
        // Configuramos el botón de guardado
        binding.btnSave.setOnClickListener {
            if (validarFormulario()) guardarCambios()
        }
        // Configuramos el botón para editar el nombre de hábito
        binding.edtHabitName.setOnFocusChangeListener { _, _ ->
            binding.tilHabitName.error = null
        }
        // Configuramos el botón de retroceso
        binding.btnBack.setOnClickListener { finish() }
    }
    // Cargamos las categorías desde Firestore
    private fun cargarCategorias(categoriaIdActual: String) {
        lifecycleScope.launch {
            val resultado = categoryRepository.obtenerCategorias()
            resultado.fold(
                onSuccess = { lista ->
                    categorias = lista
                    val adapter = ArrayAdapter(
                        this@EditHabitActivity,
                        android.R.layout.simple_dropdown_item_1line,
                        lista.map { it.nombre }
                    )
                    binding.actvCategory.setAdapter(adapter)

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
    // Validamos el formulario
    private fun validarFormulario(): Boolean {
        val nombre   = binding.edtHabitName.text.toString().trim()
        var esValido = true

        if (nombre.isEmpty()) {
            binding.tilHabitName.error = getString(R.string.error_habit_name_empty)
            esValido = false
        } else {
            binding.tilHabitName.error = null
        }

        if (!formManager.esDiario && !formManager.hayDiasSeleccionados()) {
            formManager.mostrarErrorDias()
            esValido = false
        }

        return esValido
    }
    // Guardamos los cambios
    private fun guardarCambios() {
        val nombre = binding.edtHabitName.text.toString().trim()

        binding.btnSave.isEnabled = false
        binding.btnSave.text      = getString(R.string.btn_update_loading)

        lifecycleScope.launch {
            val resultado = habitRepository.actualizarHabito(
                habitoId    = habitoId,
                nombre      = nombre,
                frecuencia  = formManager.obtenerFrecuenciaString(),
                diasSemana  = formManager.obtenerDiasLista(),
                categoriaId = categoriaSeleccionada?.id ?: ""
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
