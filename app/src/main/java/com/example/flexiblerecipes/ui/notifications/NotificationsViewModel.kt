package com.example.flexiblerecipes.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NotificationsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Saved and Favorited Recipes"
    }
    val text: LiveData<String> = _text
}