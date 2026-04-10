package com.example.habittrackerapp.ui

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habittrackerapp.data.Habit
import com.example.habittrackerapp.databinding.ItemHabitBinding

class HabitAdapter(
    private val habits: List<Habit>,
    private val onMoreClick: (Habit) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitVH>() {

    inner class HabitVH(val binding: ItemHabitBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        HabitVH(ItemHabitBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = habits.size

    override fun onBindViewHolder(holder: HabitVH, position: Int) {
        val habit = habits[position]
        with(holder.binding) {
            tvHabitName.text = habit.nombre
            tvFrequency.text = habit.frecuencia
            tvStreak.text    = "🔗 ${habit.racha}"
            tvPercent.text   = "✓ ${habit.porcentaje}%"
            // Mostramos el chip de categoría solo si tiene una asignada
            if (habit.categoriaNombre.isNotEmpty()) {
                tvCategoryChip.visibility = android.view.View.VISIBLE
                tvCategoryChip.text       = habit.categoriaNombre
                // Aplicamos el color de la categoría al fondo del chip
                try {
                    val color = android.graphics.Color.parseColor(habit.categoriaColor)
                    tvCategoryChip.backgroundTintList =
                        android.content.res.ColorStateList.valueOf(color)
                } catch (e: Exception) {
                    tvCategoryChip.backgroundTintList =
                        android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.parseColor("#C8614A")
                        )
                }
            } else {
                tvCategoryChip.visibility = android.view.View.GONE
            }

            rvWeekDays.layoutManager =
                LinearLayoutManager(root.context, LinearLayoutManager.HORIZONTAL, false)
            rvWeekDays.adapter = DayAdapter(habit.weekDays)

            root.setOnClickListener { onMoreClick(habit) }
        }
    }
}
