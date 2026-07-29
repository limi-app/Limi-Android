package personal.limi.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import personal.limi.theme.LimiTheme
import personal.limi.ui.screen.oss.OSSLicenseMenu

class OSSLicenseMenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent { LimiTheme { OSSLicenseMenu(onBack = { finish() }) } }
    }
}