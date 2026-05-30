package org.arielgos.ambiguitykiller

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale

class MessageAdapter(context: Context, messages: MutableList<Message>) : ArrayAdapter<Message?>(context, 0, messages) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val message: Message? = getItem(position)
        var convertView = LayoutInflater.from(context).inflate(R.layout.message_item, parent, false)
        if (message?.type == MessageType.SYSTEM) {
            convertView = LayoutInflater.from(context).inflate(R.layout.system_message_item, parent, false)
        }
        val tvMessage = convertView.findViewById<TextView>(R.id.message)
        val tvDate = convertView.findViewById<TextView>(R.id.date)
        tvMessage.text = message?.message
        tvDate.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(Date.from(Instant.now()))
        return convertView
    }
}