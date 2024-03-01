package com.shaluambasta.soundrecorder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecordingAdapter(
    private val recordings: List<Recording>,
    private val onItemClick: (Recording) -> Unit,
    private val onItemLongClick: (Recording) -> Unit // Listener for item long click
) : RecyclerView.Adapter<RecordingAdapter.RecordingViewHolder>() {

    inner class RecordingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val recording = recordings[position]
                    onItemClick(recording)
                }
            }

            itemView.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val recording = recordings[position]
                    onItemLongClick(recording)
                    return@setOnLongClickListener true
                }
                return@setOnLongClickListener false
            }
        }
        // Bind your views here
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordingViewHolder {
        // Inflate your item layout here and return a ViewHolder
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recording_item, parent, false)
        return RecordingViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: RecordingViewHolder, position: Int) {
        val recording = recordings[position]
        val name = "Record ${position + 1}"
        holder.itemView.findViewById<TextView>(R.id.textView).text = name // Assuming "name" is the property holding the name of the recording in your Recording class
    }


    override fun getItemCount(): Int {
        return recordings.size
    }
}
