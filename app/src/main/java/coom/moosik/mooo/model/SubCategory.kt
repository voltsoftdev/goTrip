package coom.moosik.mooo.model

data class SubCategory(
    val id: String,
    val img: String,
    val name: String,
    var isFixed: Boolean = false,
    override var isChecked : Boolean = true,
) : SelectableCategory