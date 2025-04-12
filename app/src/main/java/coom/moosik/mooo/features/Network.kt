package coom.moosik.mooo.features

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import coom.moosik.mooo.model.Marker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.io.IOException

private const val BASE_URL = "http://wwww.gajaguyo.com/"

interface ApiService {
    @GET("cj/adata5.txt")
    suspend fun getMarkers(): CommonResponse<Marker> // 응답 전체를 String으로 받음
}


@JsonPropertyOrder
@JsonIgnoreProperties(ignoreUnknown = true)
data class CommonResponse<T>(

    @SerializedName("ResponseCode")
    @JsonProperty("ResponseCode")
    val ResponseCode: Int,

    @SerializedName("mydata")
    @JsonProperty("mydata")
    val mydata: List<T>) {
}

object RetrofitClient {
    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Gson 사용
            .build()
            .create(ApiService::class.java)
    }
}

suspend fun fetchAndParseMarkers(): Result<CommonResponse<Marker>> = try {
    Result.success(RetrofitClient.instance.getMarkers())
} catch (e : Exception) {
    e.printStackTrace()

    Result.failure(e)
}

fun readMarkersFromServer() : Flow<List<Marker>?> = flow {
    emit(fetchAndParseMarkers().getOrNull()?.mydata)
}