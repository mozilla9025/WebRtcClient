package app.rtcmeetings.ui.module.contacts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import app.rtcmeetings.R
import app.rtcmeetings.data.db.dbentity.Contact
import kotlinx.android.synthetic.main.item_contact.view.*


class ContactsAdapter constructor(
    private val clickListener: (Contact) -> Unit
) : RecyclerView.Adapter<ContactsAdapter.ContactVH>(), Filterable {

    private var data: List<Contact>? = null
    private var filteredData: List<Contact>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactVH {
        return ContactVH(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_contact, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return filteredData?.size ?: 0
    }

    override fun onBindViewHolder(holder: ContactVH, position: Int) {
        filteredData?.let {
            holder.bind(it[holder.adapterPosition])
        }
    }

    fun updateData(contacts: List<Contact>) {
        this.data = contacts
        this.filteredData = data
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

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    filteredData = data
                } else {
                    val filteredList = ArrayList<Contact>()
                    data?.forEach {
                        if (it.name.toLowerCase().contains(charString.toLowerCase())
                            || it.email.toLowerCase().contains(charString.toLowerCase())
                        ) {
                            filteredList.add(it)
                        }
                    }
                    filteredData = filteredList
                }

                val filterResults = FilterResults()
                filterResults.values = filteredData
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                filteredData = filterResults.values as List<Contact>

                // refresh the list with filtered data
                notifyDataSetChanged()
            }
        }
    }
}