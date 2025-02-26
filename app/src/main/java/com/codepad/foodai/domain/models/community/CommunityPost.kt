import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class CommunityPost(
    @SerializedName("_id") val id: String,
    @SerializedName("user") val user: CommunityUser,
    @SerializedName("image") val image: CommunityImage,
    @SerializedName("likes") val likes: List<CommunityUser>,
    @SerializedName("comments") val comments: List<CommunityComment>,
    @SerializedName("createdAt") val createdAt: Date? = null,
    @SerializedName("updatedAt") val updatedAt: Date? = null
) : Parcelable