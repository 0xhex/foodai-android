import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Date.toFormattedString(): String {
    // Format like "Feb 26, 2024" (abbreviated month, day, year)
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(this)
}

fun Date.toShortTimeString(): String {
    // Format like "Feb 26, 2024, 3:45 PM" (abbreviated date and time)
    val formatter = SimpleDateFormat("MMM dd, yyyy, h:mm a", Locale.getDefault())
    return formatter.format(this)
} 