package com.example.parquecientificouncp.components

import androidx.appcompat.app.AppCompatActivity
import com.example.codi_android.R


class MyToolbar {
    fun showToolbar(activities: AppCompatActivity, title: String, upButtom: Boolean){
        activities.setSupportActionBar(activities.findViewById(R.id.toolbar))
        activities.supportActionBar?.title = title
        activities.supportActionBar?.setDisplayHomeAsUpEnabled(upButtom)

    }
}