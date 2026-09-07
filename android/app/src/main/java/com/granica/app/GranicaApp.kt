package com.granica.app

import android.app.Application
import com.granica.app.data.api.NetworkModule
import com.granica.app.data.local.GranicaDatabase
import com.granica.app.data.repository.GranicaRepository

class GranicaApp : Application() {

    lateinit var repository: GranicaRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = GranicaDatabase.getInstance(this)
        repository = GranicaRepository(NetworkModule.apiService, db.favoriteCrossingDao())
    }
}
