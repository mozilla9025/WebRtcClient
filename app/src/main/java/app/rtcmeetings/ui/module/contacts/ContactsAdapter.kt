package app.rtcmeetings.ui.module.contacts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.rtcmeetings.R
import app.rtcmeetings.data.db.dbentity.Contact
import kotlinx.android.synthetic.main.item_contact.view.*

class ContactsAdapter constructor(
        private val clickListener: (Contact) -> Unit
) : RecyclerView.Adapter<ContactsAdapter.ContactVH>() {

    private var data: List<Contact>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactVH {
        return ContactVH(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_contact, parent, false))
    }

    override fun getItemCount(): Int {
        return data?.size ?: 0
    }

    override fun onBindViewHolder(holder: ContactVH, position: Int) {
        data?.let {
            holder.bind(it[holder.adapterPosition])
        }
    }

    fun updateData(contacts: List<Contact>) {
        this.data = contacts
        notifyDataSetChanged()
    }

    inner class ContactVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(contact: Contact) {
            itemView.tvName.text = contact.name
            itemView.tvEmail.text = contact.email
            itemView.btnCall.setOnClickListener {
                clickListener.invoke(contact)
            }
        }
    }
}