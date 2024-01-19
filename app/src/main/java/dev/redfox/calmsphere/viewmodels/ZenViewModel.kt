package dev.redfox.calmsphere.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.redfox.calmsphere.models.ZenDataModel
import dev.redfox.calmsphere.networking.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class ZenViewModel @Inject constructor(private val repository: Repository): ViewModel() {

    private val _zenDataResponse = MutableLiveData<Response<List<ZenDataModel>>>()
    val zenDataResponse: MutableLiveData<Response<List<ZenDataModel>>> get() = _zenDataResponse

     fun getZenData(date: String, version: String){
        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.getZenData(date, version)
            _zenDataResponse.postValue(response)
        }
    }
}