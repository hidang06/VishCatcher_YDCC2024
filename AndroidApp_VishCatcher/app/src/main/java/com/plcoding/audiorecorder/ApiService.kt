import com.plcoding.audiorecorder.Comments
import okhttp3.MultipartBody
import org.w3c.dom.Comment
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Part

interface ApiService {

    @Multipart
    @POST("/upload")
    fun uploadAudio(@Part audioFile: MultipartBody.Part): Call<Int>

    @GET("/")
    fun getAudio(): Call<Void>

    @GET("comments")
    fun getComments(): Call<List<Comments>>
}
