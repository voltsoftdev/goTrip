package coom.moosik.mooo.model

import kotlinx.serialization.Serializable

@Serializable
data class Marker(
    var latitude : Double = 0.0,
    var longitude : Double = 0.0,
    var nb : Boolean = false,
    var type : String = "",
    var tema : String = "",
    var kung : String = "",
    var irm1 : String = "",
    var irm3 : String = ""
) {

}