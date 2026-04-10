package com.example.habittrackerapp.ui

import android.widget.ArrayAdapter
import androidx.lifecycle.lifecycleScope
import com.example.habittrackerapp.data.CategoryRepository
import com.example.habittrackerapp.data.HabitCategory
import com.example.habittrackerapp.data.HabitRepository
import kotlinx.coroutines.launch
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.habittrackerapp.R
import com.example.habittrackerapp.databinding.ActivityCreateHabitBinding

class CreateHabitActivity : AppCompatActivity() {    // Declaramos el fragmento de creación de hábitos
    // Declaramos las variables de estado
    private lateinit var binding: ActivityCreateHabitBinding
    private lateinit var formManager: HabitFormManager
    // Declaramos los repositorios de hábitos y categorías
    private val habitRepository    = HabitRepository()
    private val categoryRepository = CategoryRepository()
    private var categorias         = listOf<HabitCategory>()
    private var categoriaSeleccionada: HabitCategory? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateHabitBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        formManager.inicializarFrecuencia(diario = true)
        cargarCategorias()
        // Configuramos el botón de guardado
        binding.btnSave.setOnClickListener {
            if (validarFormulario()) guardarHabito()
        }
        // Configuramos el botón de retroceso
        binding.edtHabitName.setOnFocusChangeListener { _, _ ->
            binding.tilHabitName.error = null
        }
    }
    // Cargamos las categorías desde Firestore
    private fun cargarCategorias() {
        lifecycleScope.launch {
            val resultado = categoryRepository.obtenerCategorias()
            resultado.fold(
                onSuccess = { lista ->
                    categorias = lista
                    val adapter = ArrayAdapter(
                        this@CreateHabitActivity,
                        android.R.layout.simple_dropdown_item_1line,
                        lista.map { it.nombre }
                    )
                    binding.actvCategory.setAdapter(adapter)
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
    // Guardamos el hábito
    private fun guardarHabito() {
        val nombre = binding.edtHabitName.text.toString().trim()

        binding.btnSave.isEnabled = false
        binding.btnSave.text      = getString(R.string.btn_save_loading)

        lifecycleScope.launch {
            val resultado = habitRepository.guardarHabito(
                nombre      = nombre,
                frecuencia  = formManager.obtenerFrecuenciaString(),
                diasSemana  = formManager.obtenerDiasLista(),
                categoriaId = categoriaSeleccionada?.id ?: ""
            )
            resultado.fold(
                onSuccess = { finish() },
                onFailure = {
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text      = getString(R.string.btn_save_habit)
                }
            )
        }
    }
}
