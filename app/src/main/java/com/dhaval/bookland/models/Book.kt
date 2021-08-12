import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Book (

	@SerializedName("kind") val kind : String,
	@SerializedName("totalItems") val totalItems : Int,
	@SerializedName("items") val items : List<Items>
) : Serializable