package com.andikas.pantaubumi.di

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.andikas.pantaubumi.BuildConfig
import com.andikas.pantaubumi.data.local.PantauBumiDao
import com.andikas.pantaubumi.data.local.PantauBumiDatabase
import com.andikas.pantaubumi.data.remote.OsrmApi
import com.andikas.pantaubumi.data.remote.PantauBumiApi
import com.andikas.pantaubumi.data.repository.PantauBumiRepositoryImpl
import com.andikas.pantaubumi.domain.repository.PantauBumiRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            })
            .build()
    }

    @Provides
    @Singleton
    fun providePantauBumiApi(client: OkHttpClient): PantauBumiApi {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(PantauBumiApi::class.java)
    }

    @Provides
    @Singleton
    fun provideOsrmApi(client: OkHttpClient): OsrmApi {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        return Retrofit.Builder()
            .baseUrl("https://router.project-osrm.org/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(OsrmApi::class.java)
    }

    @Provides
    @Singleton
    fun providePantauBumiRepository(
        api: PantauBumiApi,
        osrmApi: OsrmApi,
        prefManager: com.andikas.pantaubumi.data.local.PrefManager,
        dao: PantauBumiDao
    ): PantauBumiRepository {
        return PantauBumiRepositoryImpl(api, osrmApi, prefManager, dao)
    }

    @Provides
    @Singleton
    fun provideRoomDatabase(@ApplicationContext context: Context): PantauBumiDatabase {
        return Room.databaseBuilder(
            context,
            PantauBumiDatabase::class.java,
            PantauBumiDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun providePantauBumiDao(db: PantauBumiDatabase): PantauBumiDao {
        return db.dao
    }

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @Provides
    @Singleton
    @Suppress("DEPRECATION")
    fun provideEncryptedSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        val fileName = "secure_settings"
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return try {
            EncryptedSharedPreferences.create(
                context,
                fileName,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (_: Exception) {
            // If encryption fails (AEADBadTagException), clear the preferences and try again
            context.getSharedPreferences(fileName, Context.MODE_PRIVATE).edit { clear() }
            EncryptedSharedPreferences.create(
                context,
                fileName,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }
}
