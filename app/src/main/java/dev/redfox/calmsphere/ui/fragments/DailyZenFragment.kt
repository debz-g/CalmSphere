package dev.redfox.calmsphere.ui.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import dagger.hilt.android.AndroidEntryPoint
import dev.redfox.calmsphere.adapter.DailyZenAdapter
import dev.redfox.calmsphere.databinding.FragmentDailyZenBinding
import dev.redfox.calmsphere.models.ShareDataModel
import dev.redfox.calmsphere.models.ZenDataModel
import dev.redfox.calmsphere.viewmodels.ZenViewModel

@AndroidEntryPoint
class DailyZenFragment : Fragment() {

    private var _binding: FragmentDailyZenBinding? = null
    private val binding
        get() = _binding!!
    val zenViewModel : ZenViewModel by viewModels<ZenViewModel>()
    private lateinit var dailyZenAdapter: DailyZenAdapter
    var zenData = ArrayList<ZenDataModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentDailyZenBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        zenViewModel.getZenData("20231215","2")
        initClicks()
        attachObservers()
    }


    private fun initClicks(){
        binding.btnDateBack.setOnClickListener{

        }

        binding.btnDateForward.setOnClickListener {

        }
    }

    private fun attachObservers(){
        zenViewModel.zenDataResponse.observe(viewLifecycleOwner, Observer {
            zenData = it.body() as ArrayList<ZenDataModel>

            dailyZenAdapter = DailyZenAdapter(zenData)

            binding.apply {
                recyclerView.setHasFixedSize(true)
                recyclerView.adapter = dailyZenAdapter
                recyclerView.layoutManager = LinearLayoutManager(context)

                recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)

                        val mLayoutManager =  (recyclerView.layoutManager as LinearLayoutManager)

                        val lastVisibleItemPosition =
                            mLayoutManager.findLastVisibleItemPosition()

                        endImage.visibility = if (lastVisibleItemPosition == zenData.size - 1) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                    }
                })

            }

            dailyZenAdapter.onShareClick = { shareData ->
                shareToWhatsApp(shareData)
            }

            dailyZenAdapter.onSaveClick = {
                Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
            }

            dailyZenAdapter.onReadArticleClick = { articleLink->
                openCustomTab(Uri.parse(articleLink))
            }

        })
    }

    private fun shareToWhatsApp(it: ShareDataModel){
        val target = object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {

                val shareText = "${it.text}\n\nAuthor: ${it.author}"
                val bitmapPath: String =
                    MediaStore.Images.Media.insertImage(context?.contentResolver, bitmap, "Image", null)
                val bitmapUri = Uri.parse(bitmapPath)
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "image/*"
                shareIntent.putExtra(Intent.EXTRA_STREAM, bitmapUri)
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)
                shareIntent.setPackage("com.whatsapp")
                try {
                    context?.startActivity(shareIntent)
                } catch (ex: ActivityNotFoundException) {
                    Toast.makeText(context, "Whatsapp is not installed on your device", Toast.LENGTH_SHORT).show()
                }
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


    private fun openCustomTab(url: Uri){
        val builder = CustomTabsIntent.Builder()
        builder.setShowTitle(true)
        builder.build().launchUrl(requireActivity(),url)
    }

}