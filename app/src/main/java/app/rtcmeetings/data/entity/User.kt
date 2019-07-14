package app.rtcmeetings.data.entity

import android.os.Parcelable
import app.rtcmeetings.data.db.dbentity.Contact
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String
) : Parcelable {

    fun toContact(): Contact {
        return Contact(this.id, this.name, this.email)
    }
}