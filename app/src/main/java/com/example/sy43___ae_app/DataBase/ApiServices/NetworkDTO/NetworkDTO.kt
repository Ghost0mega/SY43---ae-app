import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class NewsPaginationResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: ArrayList<NewsDateResult>
)

@Serializable
data class NewsDateResult(
    val id: Int,
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String,
    val news: NewsDetail
)

@Serializable
data class NewsDetail(
    val id: Int,
    val title: String,
    val summary: String,
    @SerialName("is_published") val isPublished: Boolean,
    val url: String,
    val club: ClubDTO
)

@Serializable
data class ClubDTO(
    val id: Int,
    val name: String,
    val logo: String,
    val url: String,
    val short_description: String,
    val results: ArrayList<MemberDTO>
)

@Serializable
data class MemberDTO(
    val user:String,
    val id: Int,
    val nickname: String,
    val first_name: String,
    val last_name: String
)
