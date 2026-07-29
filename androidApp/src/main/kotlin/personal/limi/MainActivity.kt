package personal.limi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import personal.limi.theme.LimiTheme
import personal.limi.ui.LimiApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent { LimiTheme(content = ::LimiApp) }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    LimiApp()
}