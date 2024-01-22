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
import dev.redfox.calmsphere.databinding.CardEndIconLayoutBinding
import dev.redfox.calmsphere.databinding.ZenCardItemBinding
import dev.redfox.calmsphere.models.ShareDataModel
import dev.redfox.calmsphere.models.ZenDataModel
import dev.redfox.calmsphere.utils.RecyclerViewType


class DailyZenAdapter(
    var zenData: MutableList<ZenDataModel>
) : ListAdapter<ZenDataModel, RecyclerView.ViewHolder>(ZenDataComparator()) {

    var onShareClick: ((ShareDataModel) -> Unit)? = null
    var onSaveClick: ((ZenDataModel) -> Unit)? = null
    var onReadArticleClick: ((String) -> Unit)? = null

    companion object {
        const val NORMAL_VIEW = 1
        const val END_VIEW = 2
    }


    inner class DailyZenViewHolder(private val normalBinding: ZenCardItemBinding) :
        RecyclerView.ViewHolder(normalBinding.root) {
        fun bind(zenData: ZenDataModel) {
            if (zenData.articleUrl.isNotEmpty()) {
                normalBinding.btnReadArticle.let {
                    it.isVisible = true
                    it.setOnClickListener {
                        onReadArticleClick?.invoke(zenData.articleUrl)
                    }
                }
            } else {
                normalBinding.btnReadArticle.isVisible = false
            }
            normalBinding.tvQuote.text = zenData.themeTitle
            Picasso.get().load(zenData.dzImageUrl).into(normalBinding.ivQuote)

            normalBinding.btnShare.setOnClickListener {
                val shareDate = ShareDataModel(
                    zenData.text,
                    zenData.author,
                    zenData.dzImageUrl
                )
                onShareClick?.invoke(shareDate)
            }

            normalBinding.btnSave.setOnClickListener {
                onSaveClick?.invoke(zenData)
            }
        }
    }

    inner class EndIconViewHolder(private val endBinding: CardEndIconLayoutBinding) :
        RecyclerView.ViewHolder(endBinding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            NORMAL_VIEW -> DailyZenViewHolder(
                ZenCardItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            END_VIEW -> EndIconViewHolder(
                CardEndIconLayoutBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )

            else -> throw IllegalArgumentException("invalid item type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = zenData[position]
        when (holder) {
            is DailyZenViewHolder -> {
                holder.bind(item)
            }

            is EndIconViewHolder -> {
                //No Ops
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (position == zenData.size) {
            END_VIEW
        } else {
            NORMAL_VIEW
        }
    }

    override fun getItemCount(): Int {
        return zenData.size + 1
    }

    class ZenDataComparator : DiffUtil.ItemCallback<ZenDataModel>() {
        override fun areItemsTheSame(oldItem: ZenDataModel, newItem: ZenDataModel) =
            oldItem.uniqueId == newItem.uniqueId

        override fun areContentsTheSame(oldItem: ZenDataModel, newItem: ZenDataModel) =
            oldItem == newItem
    }
}

//class DailyZenAdapter(
//    var zenData: MutableList<ZenDataModel>
//) : ListAdapter<ZenDataModel, DailyZenAdapter.DailyZenViewHolder>(ZenDataComparator()) {
//
//    var onShareClick: ((ShareDataModel) -> Unit)? = null
//    var onSaveClick: ((ZenDataModel) -> Unit)? = null
//    var onReadArticleClick: ((String) -> Unit)? = null
//
//    class DailyZenViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
//        val quoteText: TextView = itemView.findViewById(R.id.tvQuote)
//        val quoteImage: ImageView = itemView.findViewById(R.id.ivQuote)
//        val btnShare: MaterialCardView = itemView.findViewById(R.id.btnShare)
//        val btnSave: MaterialCardView = itemView.findViewById(R.id.btnSave)
//        val btnReadArticle: MaterialCardView = itemView.findViewById(R.id.btnReadArticle)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyZenViewHolder {
//        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.zen_card_item, parent, false)
//        return DailyZenViewHolder(itemView)
//    }
//
//    override fun onBindViewHolder(holder: DailyZenViewHolder, position: Int) {
//        val zenData = zenData[position]
//
//        if (zenData.articleUrl.isNotEmpty()){
//            holder.btnReadArticle.let {
//                it.isVisible = true
//                it.setOnClickListener {
//                    onReadArticleClick?.invoke(zenData.articleUrl)
//                }
//            }
//        } else {
//            holder.btnReadArticle.isVisible = false
//        }
//        holder.quoteText.text = zenData.themeTitle
//        Picasso.get().load(zenData.dzImageUrl).into(holder.quoteImage)
//
//        holder.btnShare.setOnClickListener {
//            val shareDate = ShareDataModel(
//                zenData.text,
//                zenData.author,
//                zenData.dzImageUrl
//            )
//            onShareClick?.invoke(shareDate)
//        }
//
//        holder.btnSave.setOnClickListener {
//            onSaveClick?.invoke(zenData)
//        }
//    }
//
//    override fun getItemCount(): Int {
//        return zenData.size
//    }
//
//    class ZenDataComparator : DiffUtil.ItemCallback<ZenDataModel>() {
//        override fun areItemsTheSame(oldItem: ZenDataModel, newItem: ZenDataModel) =
//            oldItem.uniqueId == newItem.uniqueId
//
//        override fun areContentsTheSame(oldItem: ZenDataModel, newItem: ZenDataModel) =
//            oldItem == newItem
//
//    }
//}