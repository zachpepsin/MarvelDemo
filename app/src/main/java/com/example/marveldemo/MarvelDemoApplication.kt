package com.example.marveldemo

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MarvelDemoApplication : Application() {

    companion object {
        const val COMIC_ITEMS_PER_PAGE = 20
    }
}