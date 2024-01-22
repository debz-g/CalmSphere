package dev.redfox.calmsphere.ui.ui_elements

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.DrawableRes
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
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

@AndroidEntryPoint
class ShareBottomSheet(private val shareCardDataModel: ShareDataModel) :
    BottomSheetDialogFragment() {

    companion object {
        private const val MORE_TAG = "MORE_TAG"
        private const val DOWNLOAD_TAG = "DOWNLOAD_TAG"
    }

    private lateinit var binding: ShareBottomSheetBinding
    private val availableApps: MutableList<DialogItemEntity> = ArrayList()
    private lateinit var shareIntent: Intent
    private val adapter = ShareSheetAppsAdapter { item -> onItemClicked(item) }
    private var imageBitmap: Bitmap? = null

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
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
        binding.shareRecyclerView.adapter = adapter
    }

    private fun shareData(it: ShareDataModel) {
        val target = object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {

                imageBitmap = bitmap
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

                val activities: List<ResolveInfo> =
                    requireActivity().packageManager.queryIntentActivities(shareIntent, 0)


                addExtraAppsToShare("WhatsApp", R.drawable.whatsapp, "com.whatsapp")
                addExtraAppsToShare("Instagram", R.drawable.insta, "com.instagram.android")
                addExtraAppsToShare("Facebook", R.drawable.fb, "com.facebook.katana")


                for (info in activities) {
                    availableApps.add(
                        DialogItemEntity(
                            info.loadLabel(requireActivity().packageManager).toString(),
                            info.loadIcon(requireActivity().packageManager),
                            info.activityInfo.packageName
                        )
                    )
                }

                val nightModeFlags = context!!.resources.configuration.uiMode and
                        Configuration.UI_MODE_NIGHT_MASK
                when (nightModeFlags) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        addExtraAppsToShare("Download", R.drawable.download_logo_dark, DOWNLOAD_TAG)
                        addExtraAppsToShare("More", R.drawable.more_logo_dark, MORE_TAG)
                    }

                    Configuration.UI_MODE_NIGHT_NO -> {
                        addExtraAppsToShare("Download", R.drawable.download_logo, DOWNLOAD_TAG)
                        addExtraAppsToShare("More", R.drawable.more_logo, MORE_TAG)
                    }

                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                        addExtraAppsToShare("Download", R.drawable.download_logo, DOWNLOAD_TAG)
                        addExtraAppsToShare("More", R.drawable.more_logo, MORE_TAG)
                    }
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

    fun addExtraAppsToShare(
        appDescription: String,
        @DrawableRes appLogo: Int,
        packageName: String
    ) {
        val appIcon = ResourcesCompat.getDrawable(
            resources,
            appLogo,
            null
        )
        appIcon?.let { drawable ->
            availableApps.add(
                DialogItemEntity(
                    appDescription,
                    drawable,
                    packageName
                )
            )
        }
    }

    private fun onItemClicked(itemEntity: DialogItemEntity) {
        if (itemEntity.packageName == MORE_TAG) {
            val shareIntent = Intent.createChooser(
                shareIntent,
                "Share with friends"
            )
            ContextCompat.startActivity(requireContext(), shareIntent, null)
        } else if (itemEntity.packageName == DOWNLOAD_TAG) {
            imageBitmap?.let { saveBitmapImage(it) }
        } else {
            shareIntent.setPackage(itemEntity.packageName)
            try {
                startActivity(shareIntent)
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(
                    requireContext(),
                    "App not installed. Please install and try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
        dismiss()
    }

    private fun copyToClipboard(shareText: String) {
        val clipboardManager =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", shareText)
        clipboardManager.setPrimaryClip(clipData)
        binding.btnCopy.setCardBackgroundColor(resources.getColor(R.color.copied_icon_color))
        binding.btnCopy.strokeWidth = 0
        binding.tvCopyText.text = resources.getString(R.string.copied)
        binding.tvCopyText.setTextColor(resources.getColor(R.color.white))
    }

    private fun saveBitmapImage(bitmap: Bitmap) {
        val timestamp = System.currentTimeMillis()

        //Tell the media scanner about the new file so that it is immediately available to the user.
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.DATE_ADDED, timestamp)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.DATE_TAKEN, timestamp)
            values.put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "Pictures/" + getString(R.string.app_name)
            )
            values.put(MediaStore.Images.Media.IS_PENDING, true)
            val uri = requireActivity().contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )
            if (uri != null) {
                try {
                    val outputStream = requireActivity().contentResolver.openOutputStream(uri)
                    if (outputStream != null) {
                        try {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                            outputStream.close()
                        } catch (e: Exception) {
                            Log.e("TAG", "saveBitmapImage: ", e)
                        }
                    }
                    values.put(MediaStore.Images.Media.IS_PENDING, false)
                    requireActivity().contentResolver.update(uri, values, null, null)

                    Toast.makeText(requireContext(), "Saved...", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e("TAG2", "saveBitmapImage: ", e)
                }
            }
        } else {
            val imageFileFolder = File(
                Environment.getExternalStorageDirectory()
                    .toString() + "Pictures/" + getString(R.string.app_name)
            )
            if (!imageFileFolder.exists()) {
                imageFileFolder.mkdirs()
            }
            val mImageName = "$timestamp.png"
            val imageFile = File(imageFileFolder, mImageName)
            try {
                val outputStream: OutputStream = FileOutputStream(imageFile)
                try {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.close()
                } catch (e: Exception) {
                    Log.e("TAG3", "saveBitmapImage: ", e)
                }
                values.put(MediaStore.Images.Media.DATA, imageFile.absolutePath)
                requireActivity().contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )

                Toast.makeText(requireContext(), "Saved...", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("TAG4", "saveBitmapImage: ", e)
            }
        }
    }
}