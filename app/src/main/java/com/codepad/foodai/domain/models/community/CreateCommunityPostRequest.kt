import com.google.gson.annotations.SerializedName

data class CreateCommunityPostRequest(
    @SerializedName("userID") val userID: String,
    @SerializedName("imageID") val imageID: String
) 