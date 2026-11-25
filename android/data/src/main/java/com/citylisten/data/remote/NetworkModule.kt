@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package com.citylisten.data.remote

import com.citylisten.data.remote.api.AuthApi
import com.citylisten.data.remote.api.CityListenApi
import com.citylisten.data.remote.api.NominatimApi
import com.citylisten.data.remote.interceptor.AuthInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://citylisten-api.citylisten.workers.dev/"
    private const val NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/"
    // Nominatim требует указывать идентифицирующий User-Agent.
    private const val NOMINATIM_USER_AGENT = "CityListen/1.0 (student project)"

    @Provides @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true; isLenient = true }

    // AuthApi использует отдельный Retrofit без AuthInterceptor:
    // иначе TokenProvider -> AuthApi -> Retrofit -> OkHttp -> AuthInterceptor
    // образует цикл зависимостей в Hilt.
    @Provides @Singleton
    fun provideAuthApi(json: Json): AuthApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AuthApi::class.java)
    }

    // Основной клиент (с AuthInterceptor)
    @Provides @Singleton
    fun provideOkHttp(authInterceptor: AuthInterceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor) // <-- Подключаем токен
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides @Singleton
    fun provideMainRetrofit(okHttp: OkHttpClient, json: Json): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides @Singleton
    fun provideCityListenApi(retrofit: Retrofit): CityListenApi = retrofit.create(CityListenApi::class.java)

    @Provides @Singleton
    fun provideNominatimApi(json: Json): NominatimApi {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("User-Agent", NOMINATIM_USER_AGENT)
                        .build()
                )
            }
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl(NOMINATIM_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(NominatimApi::class.java)
    }
}
