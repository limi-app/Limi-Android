package personal.limi.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import personal.limi.theme.LimiTheme
import personal.limi.ui.screen.oss.OSSLicense

class OSSLicenseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val uniqueId = intent.getStringExtra("library")
        Log.i("11", uniqueId ?: "")
        if (uniqueId == null) {
            finish()
            return
        }

        setContent { LimiTheme { OSSLicense(uniqueId, { finish() }) } }
    }
}