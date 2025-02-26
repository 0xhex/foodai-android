import com.google.gson.annotations.SerializedName

data class LikePostRequest(
    @SerializedName("userID") val userID: String
) 