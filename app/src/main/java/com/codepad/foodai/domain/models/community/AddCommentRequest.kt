import com.google.gson.annotations.SerializedName

data class AddCommentRequest(
    @SerializedName("userID") val userID: String,
    @SerializedName("text") val text: String
) 