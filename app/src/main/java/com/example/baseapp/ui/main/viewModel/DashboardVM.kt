package com.example.baseapp.ui.main.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DashboardVM : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "프레그먼트2"
    }
    val text: LiveData<String> = _text
}