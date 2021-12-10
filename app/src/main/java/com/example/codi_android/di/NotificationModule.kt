package com.example.codi_android.di

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.codi_android.MapActivity
import com.example.codi_android.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object NotificationModule {
    const val NOTIFICATION_CHANNEL_ID = "tracker_notification_id"
    const val NOTIFICATION_CHANNEL_NAME = "tracker_notification"
    const val PENDING_CONTENT_REQUEST_CODE = 99
    const val ACTION_NAVIGATE_TO_MAPS_FRAGMENT = "ACTION_NAVIGATE_TO_MAPS_FRAGMENT"
    const val NOTIFICATION_ID = 3

    @ServiceScoped
    @Provides
    fun providePendingIntent(
        @ApplicationContext context: Context
    ):PendingIntent{
        return PendingIntent.getActivity(
            context,
            PENDING_CONTENT_REQUEST_CODE,
            Intent(context,MapActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    @ServiceScoped
    @Provides
    fun provideNotificationBuilder(
        @ApplicationContext context: Context,
        pendingIntent: PendingIntent
    ): NotificationCompat.Builder{
        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)

            .setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
    }

    @ServiceScoped
    @Provides
    fun provideNotificationManager(
        @ApplicationContext context:Context
    ):NotificationManager{
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

}