package coom.moosik.mooo.model

data class SubCategory(
    val id: String,
    val name: String,
    override var isChecked : Boolean = false,
) : SelectableCategory