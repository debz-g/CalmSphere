package dev.redfox.calmsphere.ui.ui_elements

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import dagger.hilt.android.AndroidEntryPoint
import dev.redfox.calmsphere.R
import dev.redfox.calmsphere.adapter.ShareSheetAppsAdapter
import dev.redfox.calmsphere.databinding.ShareBottomSheetBinding
import dev.redfox.calmsphere.models.DialogItemEntity
import dev.redfox.calmsphere.models.ShareDataModel

@AndroidEntryPoint
class ShareBottomSheet(private val shareCardDataModel: ShareDataModel) :
    BottomSheetDialogFragment() {

    companion object {
        private const val MORE_TAG = "MORE_TAG"
    }

    private lateinit var binding: ShareBottomSheetBinding
    private val availableApps: MutableList<DialogItemEntity> = ArrayList()
    private lateinit var shareIntent: Intent
    private val adapter = ShareSheetAppsAdapter { item -> onItemClicked(item) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ShareBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val dialog = requireView().parent as View
        dialog.setBackgroundColor(Color.TRANSPARENT)
        val behavior = BottomSheetBehavior.from(dialog)
        behavior.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
            behavior.isDraggable = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.shareRecyclerView.adapter = null
    }

    override fun onPause() {
        super.onPause()
        dismiss()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Picasso.get().load(Uri.parse(shareCardDataModel.imageUrl)).into(binding.ivImageShare)
        val shareText = "${shareCardDataModel.text}\n\nAuthor: ${shareCardDataModel.author}"
        binding.tvQuote.text = "$shareText"

        binding.btnCopy.setOnClickListener {
           copyToClipboard(shareText)
        }

        binding.btnClose.setOnClickListener {
            dismiss()
        }

        shareData(shareCardDataModel)
        setupRecycler()
    }

    private fun setupRecycler() {
        binding.shareRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
        }
        binding.shareRecyclerView.adapter = adapter
    }

    private fun shareData(it: ShareDataModel) {
        val target = object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {

                val shareText = "${it.text}\n\nAuthor: ${it.author}"
                val bitmapPath: String? =
                    MediaStore.Images.Media.insertImage(
                        context?.contentResolver,
                        bitmap,
                        "ShareImage",
                        "Shared"
                    )
                val bitmapUri = Uri.parse(bitmapPath)
                shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "image/*"
                shareIntent.putExtra(Intent.EXTRA_STREAM, bitmapUri)
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)

                val activities: List<ResolveInfo> = requireActivity().packageManager.queryIntentActivities(shareIntent,0)

                val whatsAppDescription = "WhatsApp"
                val whatsAppIcon = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.whatsapp,
                    null
                )
                whatsAppIcon?.let {drawable ->
                    availableApps.add(
                        DialogItemEntity(
                            whatsAppDescription,
                            drawable,
                            "com.whatsapp"
                        )
                    )
                }

                for (info in activities) {
                    availableApps.add(
                        DialogItemEntity(
                            info.loadLabel(requireActivity().packageManager).toString(),
                            info.loadIcon(requireActivity().packageManager),
                            info.activityInfo.packageName
                        )
                    )
                }

                val moreDescription = "More"
                val moreIcon = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.container,
                    null
                )

                moreIcon?.let { drawable ->
                    availableApps.add(
                        DialogItemEntity(
                            moreDescription,
                            drawable,
                            MORE_TAG
                        )
                    )
                }
                adapter.addItems(availableApps)
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                Toast.makeText(context, "Failed to read image", Toast.LENGTH_SHORT).show()
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                Toast.makeText(context, "Loading Image! Please Wait!", Toast.LENGTH_SHORT).show()
            }
        }

        Picasso.get()
            .load(it.imageUrl)
            .into(target)
    }

    private fun onItemClicked(itemEntity: DialogItemEntity) {
        if (itemEntity.packageName == MORE_TAG) {
            val shareIntent = Intent.createChooser(
                shareIntent,
                "Share with friends"
            )
            ContextCompat.startActivity(requireContext(), shareIntent, null)
        } else {
            shareIntent.setPackage(itemEntity.packageName)
            startActivity(shareIntent)
        }
        dismiss()
    }

    private fun copyToClipboard(shareText: String) {
        val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", shareText)
        clipboardManager.setPrimaryClip(clipData)
        binding.btnCopy.setCardBackgroundColor(resources.getColor(R.color.copied_icon_color))
        binding.btnCopy.strokeWidth = 0
        binding.tvCopyText.text = resources.getString(R.string.copied)
        binding.tvCopyText.setTextColor(resources.getColor(R.color.white))
    }
}