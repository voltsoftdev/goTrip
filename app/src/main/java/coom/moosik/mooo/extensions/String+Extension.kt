package coom.moosik.mooo.extensions

fun String.isNumeric(): Boolean {
    val regex = "-?\\d+(\\.\\d+)?".toRegex()
    return this.matches(regex)
}