package com.example.appfood.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appfood.R

class SearchHistoryAdapter(
    private val historyList: MutableList<String>,
    private val onItemClick: (String) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSearchQuery: TextView = itemView.findViewById(R.id.tvSearchQuery)
        val ivDelete: ImageView = itemView.findViewById(R.id.ivDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val query = historyList[position]
        holder.tvSearchQuery.text = query

        holder.itemView.setOnClickListener {
            onItemClick(query)
        }

        // Đè vào (long click) để xóa theo yêu cầu của người dùng
        holder.itemView.setOnLongClickListener {
            onDeleteClick(query)
            true
        }

        holder.ivDelete.setOnClickListener {
            onDeleteClick(query)
        }
    }

    override fun getItemCount(): Int = historyList.size

    fun updateList(newList: List<String>) {
        historyList.clear()
        historyList.addAll(newList)
        notifyDataSetChanged()
    }
}
