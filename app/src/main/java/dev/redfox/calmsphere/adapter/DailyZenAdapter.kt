package dev.redfox.calmsphere.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.squareup.picasso.Picasso
import dev.redfox.calmsphere.R
import dev.redfox.calmsphere.models.ShareDataModel
import dev.redfox.calmsphere.models.ZenDataModel

class DailyZenAdapter(
    var zenData: MutableList<ZenDataModel>
) : ListAdapter<ZenDataModel, DailyZenAdapter.DailyZenViewHolder>(ZenDataComparator()) {

    var onShareClick: ((ShareDataModel) -> Unit)? = null
    var onSaveClick: ((ZenDataModel) -> Unit)? = null
    var onReadArticleClick: ((String) -> Unit)? = null

    class DailyZenViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val quoteText: TextView = itemView.findViewById(R.id.tvQuote)
        val quoteImage: ImageView = itemView.findViewById(R.id.ivQuote)
        val btnShare: MaterialCardView = itemView.findViewById(R.id.btnShare)
        val btnSave: MaterialCardView = itemView.findViewById(R.id.btnSave)
        val btnReadArticle: MaterialCardView = itemView.findViewById(R.id.btnReadArticle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyZenViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.zen_card_item, parent, false)
        return DailyZenViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DailyZenViewHolder, position: Int) {
        val zenData = zenData[position]

        if (zenData.articleUrl.isNotEmpty()){
            holder.btnReadArticle.let {
                it.isVisible = true
                it.setOnClickListener {
                    onReadArticleClick?.invoke(zenData.articleUrl)
                }
            }
        } else {
            holder.btnReadArticle.isVisible = false
        }
        holder.quoteText.text = zenData.themeTitle
        Picasso.get().load(zenData.dzImageUrl).into(holder.quoteImage)

        holder.btnShare.setOnClickListener {
            val shareDate = ShareDataModel(
                zenData.text,
                zenData.author,
                zenData.dzImageUrl
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

    class ZenDataComparator : DiffUtil.ItemCallback<ZenDataModel>() {
        override fun areItemsTheSame(oldItem: ZenDataModel, newItem: ZenDataModel) =
            oldItem.uniqueId == newItem.uniqueId

        override fun areContentsTheSame(oldItem: ZenDataModel, newItem: ZenDataModel) =
            oldItem == newItem

    }
}