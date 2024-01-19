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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import dagger.hilt.android.AndroidEntryPoint
import dev.redfox.calmsphere.R
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
                this.recyclerView.setHasFixedSize(true)
                this.recyclerView.adapter = dailyZenAdapter
                this.recyclerView.layoutManager = LinearLayoutManager(context)
            }

            dailyZenAdapter.onShareClick = {
                shareToWhatsApp(it)
                Toast.makeText(context, "Shared", Toast.LENGTH_SHORT).show()
            }

            dailyZenAdapter.onSaveClick = {
                Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun shareToWhatsApp(it: ShareDataModel){
        val target = object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {

                var shareText = "${it.text},\nAuthor: ${it.author}"
                val bitmapPath: String =
                    MediaStore.Images.Media.insertImage(context?.contentResolver, bitmap, "Beer", null)
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

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
        }

        Picasso.get()
            .load(it.imageUrl)
            .into(target)

    }


}