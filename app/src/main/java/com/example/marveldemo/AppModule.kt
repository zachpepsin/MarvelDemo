package com.example.marveldemo

import android.content.Context
import androidx.room.Room
import com.example.marveldemo.data.ComicsRepository
import com.example.marveldemo.data.ComicsService
import com.example.marveldemo.data.database.AppDatabase
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import javax.inject.Singleton

private const val BASE_URL = "https://gateway.marvel.com/v1/public/"

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .addConverterFactory(
            MoshiConverterFactory.create(
                Moshi.Builder()
                    .build()
            )
        )
        .baseUrl(BASE_URL)
        .build()

    @Singleton
    @Provides
    fun provideComicsService(retrofit: Retrofit): ComicsService =
        retrofit.create<ComicsService>()

    @Singleton
    @Provides
    fun provideApplicationDatabase(@ApplicationContext context: Context): AppDatabase =
        Room
            .inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .build()

    @Provides
    @Singleton
    fun provideComicsRepository(appDatabase: AppDatabase, comicsService: ComicsService) =
        ComicsRepository(appDatabase, comicsService)
}