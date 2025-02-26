import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class CommunityImage(
    @SerializedName("url") val url: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("mealName") val mealName: String? = null,
    @SerializedName("calories") val calories: Int? = null,
    @SerializedName("protein") val protein: Int? = null,
    @SerializedName("carbs") val carbs: Int? = null,
    @SerializedName("fats") val fats: Int? = null,
    @SerializedName("healthScore") val healthScore: Double? = null,
    @SerializedName("ingredients") val ingredients: List<String>? = null
) : Parcelable