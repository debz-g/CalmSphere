package dev.redfox.calmsphere.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.redfox.calmsphere.databinding.AvailableAppsItemBinding
import dev.redfox.calmsphere.models.DialogItemEntity

class ShareSheetAppsAdapter(private val onClickCallback: (itemEntity: DialogItemEntity) -> Unit) :
    RecyclerView.Adapter<ShareSheetAppsAdapter.ViewHolder>() {
    private val dataSet = mutableListOf<DialogItemEntity>()

    fun addItems(items: List<DialogItemEntity>) {
        dataSet.addAll(items)
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: AvailableAppsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: DialogItemEntity, onClickCallback: (itemEntity: DialogItemEntity) -> Unit){
            binding.tvTitleItemSheet.text = data.name
            binding.imgItemSheet.background = data.drawable
            binding.root.setOnClickListener {
                onClickCallback.invoke(data)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val binding = AvailableAppsItemBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataSet[position]
        holder.bind(item){
            onClickCallback.invoke(item)
        }
    }
}