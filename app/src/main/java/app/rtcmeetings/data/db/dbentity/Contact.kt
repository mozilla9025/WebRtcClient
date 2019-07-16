package app.rtcmeetings.data.db.dbentity

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.rtcmeetings.data.entity.User

@Entity
data class Contact(
    @PrimaryKey
    val id: Int,
    val name: String,
    val email: String
)

fun Contact.toUser(): User {
    return User(this.id, this.name, this.email)
}

