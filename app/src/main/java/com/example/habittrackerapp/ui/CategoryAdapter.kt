package com.example.habittrackerapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.habittrackerapp.databinding.ItemCategoryBinding

data class Category(
    val name: String,
    val iconRes: Int,
    val count: Int,
    val bgColor: Int
)

class CategoryAdapter(
    private val categories: List<Category>,
    private val onClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryVH>() {

    inner class CategoryVH(val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CategoryVH(ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = categories.size

    override fun onBindViewHolder(holder: CategoryVH, position: Int) {
        val category = categories[position]
        with(holder.binding) {
            tvCategoryName.text = category.name
            tvCategoryCount.text = "${category.count} registros"
            ivCategoryIcon.setImageResource(category.iconRes)
            ivCategoryIcon.backgroundTintList =
                android.content.res.ColorStateList.valueOf(category.bgColor)
            root.setOnClickListener { onClick(category) }
        }
    }
}