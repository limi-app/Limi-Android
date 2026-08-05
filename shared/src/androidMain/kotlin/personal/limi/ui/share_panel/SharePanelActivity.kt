package personal.limi.ui.share_panel

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.IntentCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import personal.limi.theme.LimiTheme

class SharePanelActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
    val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)?.ifBlank { null }
    val sharedImageUri =
      IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
    val isLaunchedFromShare = isLaunchedFromShareIntent(intent)

    setContent {
      Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent,
      ) {
        LimiTheme {
          val viewModel: SharePanelViewModel = viewModel()
          if (!sharedText.isNullOrBlank()) {
            viewModel.initializeWithText(sharedText)
          } else if (sharedImageUri != null) {
            viewModel.initializeWithImage(this@SharePanelActivity, sharedImageUri)
          } else if (!isLaunchedFromShare) viewModel.initializeWithText(isEditingMode = true)
          SharePanel(viewModel, onActivityClose = { finish() })
        }
      }
    }
  }

  private fun isLaunchedFromShareIntent(intent: Intent): Boolean {
    // 检查 Intent Action
    if (intent.action == Intent.ACTION_SEND) return true

    // 检查是否有分享来源标识
    val source = intent.getStringExtra(EXTRA_SOURCE)
    if (source == SOURCE_SHARE) return true

    // 检查是否包含分享特有的 extras
    if (intent.hasExtra(Intent.EXTRA_TEXT) && intent.type?.startsWith("text/") == true) return true

    return false
  }

  companion object {
    const val EXTRA_SOURCE = "source"
    const val SOURCE_SHARE = "share"
  }
}
