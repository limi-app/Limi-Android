package personal.limi.ui.screen

import android.content.Intent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import limi.shared.generated.resources.Res
import limi.shared.generated.resources.about
import limi.shared.generated.resources.about_limi
import limi.shared.generated.resources.about_limi_version
import limi.shared.generated.resources.general
import limi.shared.generated.resources.incognito_mode
import limi.shared.generated.resources.incognito_mode_desc
import limi.shared.generated.resources.open_source_license
import limi.shared.generated.resources.setting
import limi.shared.generated.resources.use_intent_filter
import limi.shared.generated.resources.use_intent_filter_desc
import personal.limi.ui.MainViewModel
import personal.limi.ui.OSSLicenseMenuActivity
import personal.limi.ui.components.preference.PreferenceGroup
import personal.limi.ui.components.preference.navigation
import personal.limi.ui.components.preference.switch

object SettingIds {
    const val INCOGNITO_MODE = "incognito_mode"
    const val USE_INTENT_FILTER = "use_intent_filter"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun SettingScreen(
    viewModel: MainViewModel = viewModel { MainViewModel() }, titleResId: StringResource = Res.string.setting
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()
    val layoutDirection = LocalLayoutDirection.current
    val context = LocalContext.current
    val appVersion by produceState(initialValue = "") {
        value = try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionName = packageInfo.versionName
            val versionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
            "v$versionName ($versionCode)"
        } catch (_: Exception) {
            "Unknown"
        }
    }
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(titleResId)) },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
    ) { innerPadding ->
        val increognitoMode = stringResource(Res.string.incognito_mode)
        val increognitoModeDesc = stringResource(Res.string.incognito_mode_desc)
        val increognitoModeEnabled by viewModel.isIncreognitoModeEnabled.collectAsState()
        val useIntentFilter = stringResource(Res.string.use_intent_filter)
        val useIntentFilterDesc = stringResource(Res.string.use_intent_filter_desc)
        val useIntentFilterEnabled by viewModel.isUsedIntentFilter.collectAsState()
        val openSourceLicense = stringResource(Res.string.open_source_license)
        LazyColumn(
            state = listState, modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = innerPadding.calculateStartPadding(layoutDirection),
                    end = innerPadding.calculateEndPadding(layoutDirection),
                    top = innerPadding.calculateTopPadding(),
                )
        ) {
            item {
                PreferenceGroup(stringResource(Res.string.general)) {
                    switch(
                        title = increognitoMode,
                        summary = increognitoModeDesc,
                        checked = increognitoModeEnabled,
                        onCheckedChange = { bool -> viewModel.setIncognitoModeEnabled(bool) })
                    switch(
                        title = useIntentFilter,
                        summary = useIntentFilterDesc,
                        checked = useIntentFilterEnabled,
                        onCheckedChange = { bool -> viewModel.setUsedIntentFilter(bool) })
                }
            }
            item {
                val aboutLimi = stringResource(Res.string.about_limi)
                val aboutLimiVersion = stringResource(Res.string.about_limi_version, appVersion)
                PreferenceGroup(stringResource(Res.string.about)) {
                    navigation(
                        title = aboutLimi,
                        summary = aboutLimiVersion,
                        showArrow = false,
                        onClick = {})
                    navigation(
                        title = openSourceLicense, onClick = {
                            val intent = Intent(context, OSSLicenseMenuActivity::class.java).apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            }
                            context.startActivity(intent)
                        })
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}