package dev.redfox.calmsphere.viewmodels

import android.net.http.NetworkException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.redfox.calmsphere.models.ZenDataModel
import dev.redfox.calmsphere.networking.Repository
import dev.redfox.calmsphere.offline.ZenOfflineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ZenViewModel @Inject constructor(private val repository: Repository, private val zenOfflineRepository: ZenOfflineRepository): ViewModel() {

    private val _zenDataResponse = MutableLiveData<Response<List<ZenDataModel>>>()
    val zenDataResponse: LiveData<Response<List<ZenDataModel>>>
        get() = _zenDataResponse

    private val _showNoNetworkToast = MutableLiveData<String?>()
    val showNoNetworkToast: LiveData<String?>
        get() = _showNoNetworkToast

    fun getZenData(date: String, version: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.getZenData(date, version)
                _zenDataResponse.postValue(response)
            } catch (e: IOException) {
                _showNoNetworkToast.postValue(e.message)
            }
        }
    }

    val zenDataOfflineResponse = zenOfflineRepository.getZenDataOffline().asLiveData()
}