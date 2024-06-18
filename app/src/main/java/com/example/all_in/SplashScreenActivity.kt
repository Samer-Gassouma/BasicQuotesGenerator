package com.example.all_in

import android.annotation.SuppressLint
import android.content.Intent
import androidx.activity.ComponentActivity
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.lifecycle.lifecycleScope
import com.example.all_in.ui.theme.All_InTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SplashScreen()
        }

        lifecycleScope.launch(Dispatchers.Main) {
            delay(2000) // Delay for 2 seconds
            startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
            finish()
        }
    }
}

