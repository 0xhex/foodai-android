import com.google.gson.annotations.SerializedName

data class DeleteCommentRequest(
    @SerializedName("commentID") val commentID: String,
    @SerializedName("userID") val userID: String
) 