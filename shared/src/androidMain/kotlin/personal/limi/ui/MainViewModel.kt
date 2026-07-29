package personal.limi.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import personal.limi.data.model.LimiHistoryEntity
import personal.limi.logic.RuleIds
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

    val isCommonParamsRuleEnabled = DataStorePreferences.getBooleanFlow(RuleIds.COMMON_PARAMS, true)
        .asState(viewModelScope, true)
    val isUTMParamsRuleEnabled =
        DataStorePreferences.getBooleanFlow(RuleIds.UTM_PARAMS, true).asState(viewModelScope, true)
    val isUTMParamsEnhancedRuleEnabled =
        DataStorePreferences.getBooleanFlow(RuleIds.UTM_PARAMS_ENHANCED, false)
            .asState(viewModelScope, false)
    val isBilibiliRuleEnabled =
        DataStorePreferences.getBooleanFlow(RuleIds.BILIBILI, true).asState(viewModelScope, true)
    val isXRuleEnabled = DataStorePreferences.getBooleanFlow(RuleIds.X, true)
        .asState(viewModelScope, true)

    fun setCommonParamsRuleEnabled(bool: Boolean) =
        DataStorePreferences.putBooleanSync(RuleIds.COMMON_PARAMS, bool)

    fun setUTMParamsRuleEnabled(bool: Boolean) =
        DataStorePreferences.putBooleanSync(RuleIds.UTM_PARAMS, bool)

    fun setUTMParamsEnhancedRuleEnabled(bool: Boolean) =
        DataStorePreferences.putBooleanSync(RuleIds.UTM_PARAMS_ENHANCED, bool)

    fun setBilibiliRuleEnabled(bool: Boolean) =
        DataStorePreferences.putBooleanSync(RuleIds.BILIBILI, bool)

    fun setXRuleEnabled(bool: Boolean) =
        DataStorePreferences.putBooleanSync(RuleIds.X, bool)

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
        val intent = Intent(context, SharePanelActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)
    }

    fun startScanQRCode(context: Context) {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                val result = withContext(Dispatchers.IO) { startPlayBarcodeScanner(context) }
                val intent = Intent(context, SharePanelActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra(
                        Intent.EXTRA_TEXT,
                        if (result.isNotEmpty()) result.joinToString("\n") else "扫码结果为空"
                    )
                }
                context.startActivity(intent)
            } catch (_: Exception) {
            }
        }
    }

    fun startSelectFromGallery(context: Context, imageUri: Uri?) {
        if (imageUri != null) {
            val intent = Intent(context, SharePanelActivity::class.java).apply {
                action = Intent.ACTION_SEND
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}