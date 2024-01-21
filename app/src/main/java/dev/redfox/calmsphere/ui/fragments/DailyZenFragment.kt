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
import dev.redfox.calmsphere.ui.ui_elements.ShareBottomSheet
import dev.redfox.calmsphere.utils.Resource
import dev.redfox.calmsphere.viewmodels.ZenViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class DailyZenFragment : Fragment() {

    private var _binding: FragmentDailyZenBinding? = null
    private val binding
        get() = _binding!!
    val zenViewModel: ZenViewModel by viewModels<ZenViewModel>()
    private lateinit var dailyZenAdapter: DailyZenAdapter
    private lateinit var dailyZenOfflineAdapter: DailyZenAdapter
    var zenData = ArrayList<ZenDataModel>()
    var zenOfflineData = ArrayList<ZenDataModel>()
    private val calendar: Calendar = Calendar.getInstance()
    private var daysOffset: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentDailyZenBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        zenViewModel.getZenData(updateDate(0), "2")
        initClicks()
        attachObservers()
    }


    private fun initClicks() {
        binding.btnDateBack.setOnClickListener {
            zenViewModel.getZenData(updateDate(-1), "2")
        }

        binding.btnDateForward.setOnClickListener {
            binding.btnDateBack.isVisible = true
            zenViewModel.getZenData(updateDate(1), "2")
        }
    }

    private fun attachObservers() {

        zenViewModel.zenDataOfflineResponse.observe(viewLifecycleOwner, Observer { result ->
            binding.progressBar.isVisible = result is Resource.Loading && result.data.isNullOrEmpty()
//            Toast.makeText(context, result.error?.localizedMessage, Toast.LENGTH_SHORT).show()

            zenOfflineData = result.data as ArrayList<ZenDataModel>

            dailyZenOfflineAdapter = DailyZenAdapter(result.data)

            binding.apply {
                recyclerView.setHasFixedSize(true)
                recyclerView.adapter = dailyZenOfflineAdapter
                recyclerView.layoutManager = LinearLayoutManager(context)

                recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)

                        val mLayoutManager = (recyclerView.layoutManager as LinearLayoutManager)

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

            dailyZenOfflineAdapter.onShareClick = { shareData ->
                val share = ShareBottomSheet(shareData)
                share.show(parentFragmentManager, "share")
            }

            dailyZenOfflineAdapter.onSaveClick = {
                Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
            }

            dailyZenOfflineAdapter .onReadArticleClick = { articleLink ->
                openCustomTab(Uri.parse(articleLink))
            }

        })

        zenViewModel.showNoNetworkToast.observe(viewLifecycleOwner){
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }

        zenViewModel.zenDataResponse.observe(viewLifecycleOwner, Observer {
            zenData = it?.body() as ArrayList<ZenDataModel>

            dailyZenAdapter = DailyZenAdapter(zenData)

            binding.apply {
                recyclerView.setHasFixedSize(true)
                recyclerView.adapter = dailyZenAdapter
                recyclerView.layoutManager = LinearLayoutManager(context)

                recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)

                        val mLayoutManager = (recyclerView.layoutManager as LinearLayoutManager)

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
//                shareToWhatsApp(shareData)
                val share = ShareBottomSheet(shareData)
                share.show(parentFragmentManager, "share")
            }

            dailyZenAdapter.onSaveClick = {
                Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
            }

            dailyZenAdapter.onReadArticleClick = { articleLink ->
                openCustomTab(Uri.parse(articleLink))
            }

        })
    }

    private fun shareToWhatsApp(it: ShareDataModel) {
        val target = object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {

                val shareText = "${it.text}\n\nAuthor: ${it.author}"
                val bitmapPath: String =
                    MediaStore.Images.Media.insertImage(
                        context?.contentResolver,
                        bitmap,
                        "Image",
                        null
                    )
                val bitmapUri = Uri.parse(bitmapPath)
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "image/*"
                shareIntent.putExtra(Intent.EXTRA_STREAM, bitmapUri)
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)
                shareIntent.setPackage("com.whatsapp")
                try {
                    context?.startActivity(shareIntent)
                } catch (ex: ActivityNotFoundException) {
                    Toast.makeText(
                        context,
                        "Whatsapp is not installed on your device",
                        Toast.LENGTH_SHORT
                    ).show()
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


    private fun openCustomTab(url: Uri) {
        val builder = CustomTabsIntent.Builder()
        builder.setShowTitle(true)
        builder.build().launchUrl(requireActivity(), url)
    }

    private fun updateDate(days: Int): String {
        // Adjust the calendar based on the requested days
        daysOffset += days

        calendar.time = Date()
        calendar.add(Calendar.DAY_OF_YEAR, daysOffset)

        val currentDate = calendar.time
        val dateFormat = SimpleDateFormat("MMMM d", Locale.getDefault())
        val returnDateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val returnFormattedDate = returnDateFormat.format(currentDate)
        val formattedDate = dateFormat.format(currentDate)
        val weekRange = -2 downTo -6

        if (daysOffset == 0) {
            binding.tvDate.text = "Today"
        } else if (daysOffset == -1) {
            binding.tvDate.text = "Yesterday"
        } else if (weekRange.contains(daysOffset)) {
            binding.tvDate.text = formattedDate
            if (daysOffset == -6) binding.btnDateBack.isVisible = false
        } else {
            // Show a toast when going beyond 7 days
            Toast.makeText(context, "Cannot go beyond 7 days $daysOffset", Toast.LENGTH_SHORT)
                .show()

        }

        // Show/hide forward button based on the date
        binding.btnDateForward.visibility = if (daysOffset < 0) View.VISIBLE else View.GONE
        return returnFormattedDate
    }

}