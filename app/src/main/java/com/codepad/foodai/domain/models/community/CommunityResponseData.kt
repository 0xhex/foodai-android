import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class CommunityResponseData(
    @SerializedName("posts") val posts: List<CommunityPost>
) : Parcelable