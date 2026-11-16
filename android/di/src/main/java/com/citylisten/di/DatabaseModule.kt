package com.citylisten.di

import android.content.Context
import androidx.room.Room
import com.citylisten.data.local.AppDatabase
import com.citylisten.data.local.StoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "citylisten.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideStoryDao(db: AppDatabase): StoryDao = db.storyDao()
}