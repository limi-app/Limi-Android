package personal.limi.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.os.Build
import android.widget.Toast
import limi.shared.generated.resources.Res
import limi.shared.generated.resources.copied
import org.jetbrains.compose.resources.getString
import personal.limi.ui.screen.SettingIds
import personal.limi.utils.datastore.DataStorePreferences

/**
 * 复制文本到剪贴板，并显示一个 Toast 提示。
 *
 * @param textCopied 要复制的文本
 */
suspend fun Context.textCopyThenPost(textCopied: String) {
  val clipboardManager =
    this.applicationContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
  // When setting the clipboard text.
  clipboardManager.setPrimaryClip(ClipData.newPlainText("", textCopied))
  // Only show a toast for Android 12 and lower.
  if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
    Toast.makeText(
        this.applicationContext,
        getString(Res.string.copied),
        Toast.LENGTH_SHORT,
      )
      .show()
}

/**
 * 分享处理后的文本
 *
 * @param textShared 要分享的文本
 */
fun Context.textShare(textShared: String) {
  val sendIntent: Intent =
    Intent(Intent.ACTION_SEND).apply {
      putExtra(Intent.EXTRA_TEXT, textShared)
      type = "text/plain"
    }

  // 是否使用 Android Sharesheet 分享面板
  val withAndroidSharesheet =
    !DataStorePreferences.getBooleanSync(SettingIds.USE_INTENT_FILTER, false)

  this.startActivity(
    if (withAndroidSharesheet) Intent.createChooser(sendIntent, null) else sendIntent
  )
}
