package coom.moosik.mooo.model

import android.util.Log

data class Category(
    val id: String,
    var subCategories: List<SubCategory> = emptyList()
) : SelectableCategory {

    override var isChecked: Boolean
        get() = subCategories.map { it.isChecked }.reduce { acc, b -> (acc && b) }
        set(value) {
            subCategories = subCategories.toMutableList().map {
                if (it.isFixed) {
                    it.copy(isChecked = true)
                }
                else
                {
                    it.copy(isChecked = value)
                }
            }
        }
}