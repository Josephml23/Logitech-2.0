package com.example.logist_tech

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.logist_tech.scanner.ScannerScreen
import com.example.logist_tech.ui.theme.Logist_TechTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            Logist_TechTheme {

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) {

                    ScannerScreen()

                }
            }
        }
    }
}