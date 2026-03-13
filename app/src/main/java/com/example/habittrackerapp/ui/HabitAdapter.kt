package com.example.habittrackerapp.ui

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

    inner class HabitVH(val binding: ItemHabitBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        HabitVH(ItemHabitBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = habits.size

    override fun onBindViewHolder(holder: HabitVH, position: Int) {
        val habit = habits[position]
        with(holder.binding) {
            tvHabitName.text = habit.name
            tvFrequency.text = habit.frequency
            tvStreak.text = "🔗 ${habit.streak}"
            tvPercent.text = "✓ ${habit.completionPercent}%"

            rvWeekDays.layoutManager =
                LinearLayoutManager(root.context, LinearLayoutManager.HORIZONTAL, false)
            rvWeekDays.adapter = DayAdapter(habit.weekDays)
        }
    }
}