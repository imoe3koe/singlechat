package com.singlechat

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.singlechat.ui.ChatFragment
import com.singlechat.ui.MembersFragment
import com.singlechat.ui.SettingsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var tvHeaderTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvHeaderTitle = findViewById(R.id.tvHeaderTitle)

        val navChat = findViewById<LinearLayout>(R.id.navChat)
        val navMembers = findViewById<LinearLayout>(R.id.navMembers)
        val navSettings = findViewById<LinearLayout>(R.id.navSettings)

        // Tampilkan tab Obrolan secara default
        loadFragment(ChatFragment(), "OBROLAN")

        navChat.setOnClickListener {
            loadFragment(ChatFragment(), "OBROLAN")
        }

        navMembers.setOnClickListener {
            loadFragment(MembersFragment(), "ANGGOTA")
        }

        navSettings.setOnClickListener {
            loadFragment(SettingsFragment(), "PENGATURAN")
        }
    }

    private fun loadFragment(fragment: Fragment, title: String) {
        tvHeaderTitle.text = title
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}