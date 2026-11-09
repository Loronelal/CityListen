package com.citylisten.data

import com.citylisten.domain.CityRepository
import javax.inject.Inject

class CityRepositoryImpl @Inject constructor() : CityRepository {
    override fun getWelcomeText(): String {
        return "Добро пожаловать в CityListen (Data-слой)!"
    }
}