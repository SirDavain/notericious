import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface NoteApiService {
    @GET("notes")
    suspend fun getNotes(): Response<List<Note>>

    @POST("notes")
    suspend fun saveNote(@Body note: Note): Response<Note>
}