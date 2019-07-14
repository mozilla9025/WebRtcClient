package app.rtcmeetings.data.db.dbentity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Contact(
    @PrimaryKey
    val id: Int,
    val name: String,
    val email: String
)
