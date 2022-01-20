package com.example.baseapp.ui.main.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NotificationsVM : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "프레그먼트3"
    }
    val text: LiveData<String> = _text
}