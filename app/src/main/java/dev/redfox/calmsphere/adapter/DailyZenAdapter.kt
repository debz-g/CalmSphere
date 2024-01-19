package dev.redfox.calmsphere.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.squareup.picasso.Picasso
import dev.redfox.calmsphere.R
import dev.redfox.calmsphere.models.ShareDataModel
import dev.redfox.calmsphere.models.ZenDataModel

class DailyZenAdapter(
    var zenData: MutableList<ZenDataModel>
) : RecyclerView.Adapter<DailyZenAdapter.DailyZenViewHolder>() {

    var onShareClick: ((ShareDataModel) -> Unit)? = null
    var onSaveClick: ((ZenDataModel) -> Unit)? = null

    class DailyZenViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val quoteText: TextView = itemView.findViewById(R.id.tvQuote)
        val quoteImage: ImageView = itemView.findViewById(R.id.ivQuote)
        val btnShare: MaterialCardView = itemView.findViewById(R.id.btnShare)
        val btnSave: MaterialCardView = itemView.findViewById(R.id.btnSave)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyZenViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.zen_card_item, parent, false)
        return DailyZenViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DailyZenViewHolder, position: Int) {
        val zenData = zenData[position]
        holder.quoteText.text = zenData.text
        Picasso.get().load(zenData.dzImageUrl).into(holder.quoteImage)

        holder.btnShare.setOnClickListener {
            val shareDate = ShareDataModel(
                zenData.text,
                zenData.dzImageUrl,
                zenData.author
            )
            onShareClick?.invoke(shareDate)
        }

        holder.btnSave.setOnClickListener {
            onSaveClick?.invoke(zenData)
        }
    }

    override fun getItemCount(): Int {
        return zenData.size
    }
}