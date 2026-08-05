package personal.limi.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import personal.limi.data.model.LimiHistoryEntity
import personal.limi.logic.RuleIds
import personal.limi.logic.rules.cloud.CloudRuleRepository
import personal.limi.logic.rules.cloud.brave.CleanUrlsRuleKeys
import personal.limi.logic.rules.cloud.brave.DebounceRuleKeys
import personal.limi.logic.rules.cloud.brave.QueryFilterRuleKeys
import personal.limi.logic.rules.cloud.clearurlsxyz.ClearUrlsRuleKeys
import personal.limi.logic.rules.cloud.firefox.FirefoxRuleKeys
import personal.limi.ui.screen.SettingIds
import personal.limi.ui.share_panel.SharePanelActivity
import personal.limi.utils.DatabaseHolder
import personal.limi.utils.asState
import personal.limi.utils.datastore.DataStorePreferences
import personal.limi.utils.room.LimiHistoryDao
import personal.limi.utils.startPlayBarcodeScanner

class MainViewModel() : ViewModel() {
  val dao: LimiHistoryDao = DatabaseHolder.database.getLimiHistoryDao()

  val historyListStateFlow: StateFlow<List<LimiHistoryEntity>> =
    dao.getAllAsFlowSortedByDatetimeDesc().asState(viewModelScope, emptyList())

  val isUTMParamsRuleEnabled =
    DataStorePreferences.getBooleanFlow(RuleIds.UTM_PARAMS, true).asState(viewModelScope, true)
  val isUTMParamsEnhancedRuleEnabled =
    DataStorePreferences.getBooleanFlow(RuleIds.UTM_PARAMS_ENHANCED, false)
      .asState(viewModelScope, false)
  val isBilibiliRuleEnabled =
    DataStorePreferences.getBooleanFlow(RuleIds.BILIBILI, true).asState(viewModelScope, true)
  val isFirefoxQueryStrippingRuleEnabled =
    DataStorePreferences.getBooleanFlow(RuleIds.FIREFOX_QUERY_STRIPPING, false)
      .asState(viewModelScope, false)
  val isBraveCleanUrlsRuleEnabled =
    DataStorePreferences.getBooleanFlow(RuleIds.BRAVE_CLEAN_URLS, false)
      .asState(viewModelScope, false)
  val isBraveDebounceRuleEnabled =
    DataStorePreferences.getBooleanFlow(RuleIds.BRAVE_DEBOUNCE, false)
      .asState(viewModelScope, false)
  val isBraveQueryFilterRuleEnabled =
    DataStorePreferences.getBooleanFlow(RuleIds.BRAVE_QUERY_FILTER, false)
      .asState(viewModelScope, false)
  val isClearUrlsRuleEnabled =
    DataStorePreferences.getBooleanFlow(RuleIds.CLEAR_URLS, false).asState(viewModelScope, false)
  val cloudRulesVersionTime =
    DataStorePreferences.getLongFlow(
        FirefoxRuleKeys.QUERY_STRIPPING_LAST_MODIFIED,
        0L,
      )
      .asState(viewModelScope, 0L)
  val braveCleanUrlsVersionHash =
    DataStorePreferences.getStringFlow(
        CleanUrlsRuleKeys.VERSION_HASH,
        "",
      )
      .asState(viewModelScope, "")
  val braveDebounceVersionHash =
    DataStorePreferences.getStringFlow(
        DebounceRuleKeys.VERSION_HASH,
        "",
      )
      .asState(viewModelScope, "")
  val braveQueryFilterVersionHash =
    DataStorePreferences.getStringFlow(
        QueryFilterRuleKeys.VERSION_HASH,
        "",
      )
      .asState(viewModelScope, "")
  val clearUrlsVersionHash =
    DataStorePreferences.getStringFlow(
        ClearUrlsRuleKeys.VERSION_HASH,
        "",
      )
      .asState(viewModelScope, "")

  private val _isSyncingCloudRules = MutableStateFlow(false)
  val isSyncingCloudRules: StateFlow<Boolean> = _isSyncingCloudRules.asStateFlow()

  private val _cloudRulesSyncFailed = MutableStateFlow(false)
  val cloudRulesSyncFailed: StateFlow<Boolean> = _cloudRulesSyncFailed.asStateFlow()

  private val _cloudRulesSyncErrorMessage = MutableStateFlow<String?>(null)
  val cloudRulesSyncErrorMessage: StateFlow<String?> = _cloudRulesSyncErrorMessage.asStateFlow()

  fun setUTMParamsRuleEnabled(bool: Boolean) =
    DataStorePreferences.putBooleanSync(RuleIds.UTM_PARAMS, bool)

  fun setUTMParamsEnhancedRuleEnabled(bool: Boolean) =
    DataStorePreferences.putBooleanSync(RuleIds.UTM_PARAMS_ENHANCED, bool)

  fun setBilibiliRuleEnabled(bool: Boolean) =
    DataStorePreferences.putBooleanSync(RuleIds.BILIBILI, bool)

  fun setFirefoxQueryStrippingRuleEnabled(bool: Boolean) =
    DataStorePreferences.putBooleanSync(RuleIds.FIREFOX_QUERY_STRIPPING, bool)

  fun setBraveCleanUrlsRuleEnabled(bool: Boolean) =
    DataStorePreferences.putBooleanSync(RuleIds.BRAVE_CLEAN_URLS, bool)

  fun setBraveDebounceRuleEnabled(bool: Boolean) =
    DataStorePreferences.putBooleanSync(RuleIds.BRAVE_DEBOUNCE, bool)

  fun setBraveQueryFilterRuleEnabled(bool: Boolean) =
    DataStorePreferences.putBooleanSync(RuleIds.BRAVE_QUERY_FILTER, bool)

  fun setClearUrlsRuleEnabled(bool: Boolean) =
    DataStorePreferences.putBooleanSync(RuleIds.CLEAR_URLS, bool)

  fun syncCloudRules() {
    if (_isSyncingCloudRules.value) return
    viewModelScope.launch(Dispatchers.IO) {
      _isSyncingCloudRules.value = true
      try {
        if (isFirefoxQueryStrippingRuleEnabled.value) {
          CloudRuleRepository.syncFirefoxQueryStripping()
        }
        if (isBraveCleanUrlsRuleEnabled.value) {
          CloudRuleRepository.syncCleanUrls()
        }
        if (isBraveDebounceRuleEnabled.value) {
          CloudRuleRepository.syncDebounce()
        }
        if (isBraveQueryFilterRuleEnabled.value) {
          CloudRuleRepository.syncQueryFilter()
        }
        if (isClearUrlsRuleEnabled.value) {
          CloudRuleRepository.syncClearUrls()
        }
      } catch (e: Exception) {
        _cloudRulesSyncFailed.value = true
        _cloudRulesSyncErrorMessage.value = if (e is IOException) null else e.message
      } finally {
        _isSyncingCloudRules.value = false
      }
    }
  }

  fun dismissCloudRulesSyncError() {
    _cloudRulesSyncFailed.value = false
    _cloudRulesSyncErrorMessage.value = null
  }

  val isIncreognitoModeEnabled =
    DataStorePreferences.getBooleanFlow(SettingIds.INCOGNITO_MODE, false)
      .asState(viewModelScope, false)

  val isUsedIntentFilter =
    DataStorePreferences.getBooleanFlow(SettingIds.USE_INTENT_FILTER, false)
      .asState(viewModelScope, false)

  fun setIncognitoModeEnabled(bool: Boolean) =
    DataStorePreferences.putBooleanSync(SettingIds.INCOGNITO_MODE, bool)

  fun setUsedIntentFilter(bool: Boolean) =
    DataStorePreferences.putBooleanSync(SettingIds.USE_INTENT_FILTER, bool)

  fun startSharePanel(context: Context) {
    val intent =
      Intent(context, SharePanelActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK)
      }
    context.startActivity(intent)
  }

  fun startScanQRCode(context: Context) {
    viewModelScope.launch(Dispatchers.Main) {
      try {
        val result = withContext(Dispatchers.IO) { startPlayBarcodeScanner(context) }
        val intent =
          Intent(context, SharePanelActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(
              Intent.EXTRA_TEXT,
              if (result.isNotEmpty()) result.joinToString("\n") else "扫码结果为空",
            )
          }
        context.startActivity(intent)
      } catch (_: Exception) {}
    }
  }

  fun startSelectFromGallery(context: Context, imageUri: Uri?) {
    if (imageUri != null) {
      val intent =
        Intent(context, SharePanelActivity::class.java).apply {
          action = Intent.ACTION_SEND
          type = "image/*"
          putExtra(Intent.EXTRA_STREAM, imageUri)
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
      context.startActivity(intent)
    }
  }
}
