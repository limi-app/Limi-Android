package personal.limi.ui.share_panel

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import limi.shared.generated.resources.Res
import limi.shared.generated.resources.no_links_found
import limi.shared.generated.resources.no_qr_code_found
import limi.shared.generated.resources.processing_error
import org.jetbrains.compose.resources.getString
import personal.limi.data.model.LimiHistoryEntity
import personal.limi.logic.processUrl
import personal.limi.ui.screen.SettingIds
import personal.limi.utils.DatabaseHolder
import personal.limi.utils.datastore.DataStorePreferences
import personal.limi.utils.extractUrlList
import personal.limi.utils.room.LimiHistoryDao
import personal.limi.utils.scanQRCodeFromImageUri
import personal.limi.utils.textCopyThenPost
import personal.limi.utils.textShare

@SuppressLint("MutableCollectionMutableState")
class SharePanelViewModel : ViewModel() {
  var isProcessing by mutableStateOf(false)
    private set

  var processedText by mutableStateOf<String?>(null)
    private set

  var processedUrlList by mutableStateOf<MutableList<String>>(mutableListOf())
    private set

  var originalText by mutableStateOf<String?>("")
    private set

  var isEmpty by mutableStateOf(false)
    private set

  var isNotHasUrls by mutableStateOf(false)
    private set

  var isError by mutableStateOf(false)
    private set

  var isEditing by mutableStateOf(false)

  private val dao: LimiHistoryDao = DatabaseHolder.database.getLimiHistoryDao()

  /**
   * 初始化并开始处理文本
   *
   * @param text 要处理的文本
   */
  fun initializeWithText(text: String? = null, isEditingMode: Boolean = false) {
    isProcessing = false
    isEmpty = false
    isNotHasUrls = false
    isError = false
    if (isEditingMode) isEditing = true
    else {
      originalText = text
      processText(text)
    }
  }

  /**
   * 初始化并开始处理图片中的二维码
   *
   * @param imageUri 图片 URI
   */
  fun initializeWithImage(context: Context, imageUri: Uri) {
    isProcessing = true
    isEmpty = false
    isNotHasUrls = false
    isError = false
    isEditing = false

    viewModelScope.launch(Dispatchers.IO) {
      try {
        val qrCodeResults = scanQRCodeFromImageUri(context, imageUri)
        val resultText =
          if (qrCodeResults.isNotEmpty()) qrCodeResults.joinToString("\n")
          else getString(Res.string.no_qr_code_found)

        launch(Dispatchers.Main) {
          originalText = resultText
          processText(resultText)
        }
      } catch (e: Exception) {
        launch(Dispatchers.Main) {
          isProcessing = false
          isError = true
          processedText = getString(Res.string.processing_error, e.message ?: "")
        }
      }
    }
  }

  /** 更新原始文本并处理 */
  fun updateAndProcessText(text: String) {
    originalText = text
    processText(text)
  }

  /** 复制处理后的文本 */
  fun copyText(context: Context) {
    if (processedText.isNullOrBlank()) return

    viewModelScope.launch {
      context.textCopyThenPost(processedText ?: "")
    }
  }

  /** 分享处理后的文本 */
  fun shareText(context: Context) {
    if (processedText.isNullOrBlank()) return else context.textShare(processedText ?: "")
  }

  private fun processText(text: String?) {
    if (text.isNullOrBlank()) {
      processedText = ""
      isEmpty = true
      return
    }

    processedText = ""
    processedUrlList = mutableListOf()
    isProcessing = true
    isEmpty = false
    isError = false
    isNotHasUrls = false

    val exceptionHandler = CoroutineExceptionHandler { _, e ->
      viewModelScope.launch(Dispatchers.Main) {
        isProcessing = false
        isError = true
        processedText = getString(Res.string.processing_error, e.message ?: "")
      }
    }

    viewModelScope.launch(exceptionHandler) {
      try {
        val urls = extractUrlList(text)
        if (urls.isEmpty()) {
          processedText = getString(Res.string.no_links_found)
          isNotHasUrls = true
        } else {
          val semaphore = Semaphore(5)

          val tasks = urls.map { url ->
            async(Dispatchers.IO) {
              semaphore.withPermit {
                val processedResult = processUrl(url)
                url to processedResult
              }
            }
          }

          val processedUrls = awaitAll(*tasks.toTypedArray()).toMap()

          var resultText = text
          if (processedUrls.isNotEmpty())
            processedUrls.forEach { (originalUrl, processedUrl) ->
              resultText = resultText?.replace(originalUrl, processedUrl)
              processedUrlList += processedUrl
            }
          processedText = resultText
          val isIncognitoModeEnabled =
            DataStorePreferences.getBoolean(SettingIds.INCOGNITO_MODE, false)
          if (
            !isIncognitoModeEnabled &&
              !originalText.isNullOrBlank() &&
              !processedText.isNullOrBlank()
          )
            saveToHistory(
              originalText!!,
              processedText!!,
            )
        }
      } catch (e: CancellationException) {
        // 协程被取消时（例如用户退出屏幕），通常不需要特殊处理，但可以记录。
        // 捕获 CancellationException 是为了避免它被下面的 Exception 捕获，并在 isError 状态中设置不正确的值。
        throw e
      } catch (e: Exception) {
        processedText = getString(Res.string.processing_error, e.message ?: "")
        isError = true
      } finally {
        isProcessing = false
      }
    }
  }

  private fun saveToHistory(originUrl: String, processedUrl: String) {
    viewModelScope.launch(Dispatchers.IO) {
      dao.insert(
        LimiHistoryEntity(
          originUrl = originUrl,
          processedUrl = processedUrl,
        )
      )
    }
  }
}
