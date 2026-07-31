package personal.limi.ui.screen

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.darkokoa.pangu.Pangu
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import limi.shared.generated.resources.Res
import limi.shared.generated.resources.bilibili_rule
import limi.shared.generated.resources.bilibili_rule_desc
import limi.shared.generated.resources.brave_clean_urls_rule
import limi.shared.generated.resources.brave_debounce_rule
import limi.shared.generated.resources.brave_query_filter_rule
import limi.shared.generated.resources.cloud_rules_group
import limi.shared.generated.resources.cloud_rules_not_synced
import limi.shared.generated.resources.cloud_rules_version
import limi.shared.generated.resources.confirm
import limi.shared.generated.resources.exceptional_rules_group
import limi.shared.generated.resources.firefox_query_stripping_rule
import limi.shared.generated.resources.local_rules_group
import limi.shared.generated.resources.rule
import limi.shared.generated.resources.sync_rules
import limi.shared.generated.resources.sync_rules_failed_message
import limi.shared.generated.resources.sync_rules_failed_title
import limi.shared.generated.resources.unknown
import limi.shared.generated.resources.utm_enhanced_rule
import limi.shared.generated.resources.utm_enhanced_rule_desc
import limi.shared.generated.resources.utm_rule
import limi.shared.generated.resources.utm_rule_desc
import personal.limi.ui.MainViewModel
import personal.limi.ui.components.preference.PreferenceGroup
import personal.limi.ui.components.preference.switch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleScreen(
    viewModel: MainViewModel = viewModel { MainViewModel() }, titleResId: StringResource = Res.string.rule
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()
    val layoutDirection = LocalLayoutDirection.current

    val utmRuleTitle = stringResource(Res.string.utm_rule)
    val utmRuleDesc = stringResource(Res.string.utm_rule_desc)
    val utmRuleEnabled by viewModel.isUTMParamsRuleEnabled.collectAsState()
    val utmEnhancedTitle = stringResource(Res.string.utm_enhanced_rule)
    val utmEnhancedDesc = stringResource(Res.string.utm_enhanced_rule_desc)
    val utmEnhancedEnabled by viewModel.isUTMParamsEnhancedRuleEnabled.collectAsState()
    val localRulesList = listOf(
        SwitchPreferenceItem(
            title = utmRuleTitle,
            summary = utmRuleDesc,
            checked = utmRuleEnabled,
            onCheckedChange = { bool -> viewModel.setUTMParamsRuleEnabled(bool) }),
        SwitchPreferenceItem(
            title = utmEnhancedTitle,
            summary = utmEnhancedDesc,
            checked = utmEnhancedEnabled,
            onCheckedChange = { bool -> viewModel.setUTMParamsEnhancedRuleEnabled(bool) })
    )
    val bilibiliRuleTitle = stringResource(Res.string.bilibili_rule)
    val bilibiliRuleDesc = stringResource(Res.string.bilibili_rule_desc)
    val bilibiliRuleEnabled by viewModel.isBilibiliRuleEnabled.collectAsState()
    val exceptionalRulesList = listOf(
        SwitchPreferenceItem(
            title = bilibiliRuleTitle,
            summary = bilibiliRuleDesc,
            checked = bilibiliRuleEnabled,
            onCheckedChange = { bool -> viewModel.setBilibiliRuleEnabled(bool) })
    )
    val isSyncing by viewModel.isSyncingCloudRules.collectAsState()
    val cloudRulesSyncFailed by viewModel.cloudRulesSyncFailed.collectAsState()
    val cloudRulesSyncErrorMessage by viewModel.cloudRulesSyncErrorMessage.collectAsState()

    val firefoxRuleEnabled by viewModel.isFirefoxQueryStrippingRuleEnabled.collectAsState()
    val braveCleanUrlsEnabled by viewModel.isBraveCleanUrlsRuleEnabled.collectAsState()
    val braveDebounceEnabled by viewModel.isBraveDebounceRuleEnabled.collectAsState()
    val braveQueryFilterEnabled by viewModel.isBraveQueryFilterRuleEnabled.collectAsState()
    val cloudRulesVersionTime by viewModel.cloudRulesVersionTime.collectAsState()
    val braveCleanUrlsVersionHash by viewModel.braveCleanUrlsVersionHash.collectAsState()
    val braveDebounceVersionHash by viewModel.braveDebounceVersionHash.collectAsState()
    val braveQueryFilterVersionHash by viewModel.braveQueryFilterVersionHash.collectAsState()

    val locale = LocalConfiguration.current.locales[0]
    val unknownText = stringResource(Res.string.unknown)

    val cloudRuleSpecs = listOf(
        CloudRuleSpec(
            titleResId = Res.string.firefox_query_stripping_rule,
            enabled = firefoxRuleEnabled,
            versionSummary = if (cloudRulesVersionTime > 0L) {
                stringResource(
                    Res.string.cloud_rules_version,
                    formatSyncTime(cloudRulesVersionTime, locale, unknownText)
                )
            } else {
                null
            },
            onCheckedChange = { bool -> viewModel.setFirefoxQueryStrippingRuleEnabled(bool) }
        ),
        CloudRuleSpec(
            titleResId = Res.string.brave_clean_urls_rule,
            enabled = braveCleanUrlsEnabled,
            versionSummary = braveCleanUrlsVersionHash.takeIf { it.isNotBlank() }?.let {
                stringResource(Res.string.cloud_rules_version, it.take(8))
            },
            onCheckedChange = { bool -> viewModel.setBraveCleanUrlsRuleEnabled(bool) }
        ),
        CloudRuleSpec(
            titleResId = Res.string.brave_query_filter_rule,
            enabled = braveQueryFilterEnabled,
            versionSummary = braveQueryFilterVersionHash.takeIf { it.isNotBlank() }?.let {
                stringResource(Res.string.cloud_rules_version, it.take(8))
            },
            onCheckedChange = { bool -> viewModel.setBraveQueryFilterRuleEnabled(bool) }
        ),
        CloudRuleSpec(
            titleResId = Res.string.brave_debounce_rule,
            enabled = braveDebounceEnabled,
            versionSummary = braveDebounceVersionHash.takeIf { it.isNotBlank() }?.let {
                stringResource(Res.string.cloud_rules_version, it.take(8))
            },
            onCheckedChange = { bool -> viewModel.setBraveDebounceRuleEnabled(bool) }
        )
    )
    val cloudRulesEnabled = cloudRuleSpecs.any { it.enabled }
    val cloudRulesList = cloudRuleSpecs.map { spec ->
        SwitchPreferenceItem(
            title = stringResource(spec.titleResId),
            summary = spec.versionSummary ?: stringResource(Res.string.cloud_rules_not_synced),
            checked = spec.enabled,
            onCheckedChange = spec.onCheckedChange
        )
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
                ), actions = {
                    if (cloudRulesEnabled) {
                        IconButton(onClick = { viewModel.syncCloudRules() }, enabled = !isSyncing) {
                            if (!isSyncing) Icon(
                                imageVector = Icons.Outlined.Sync,
                                contentDescription = stringResource(Res.string.sync_rules)
                            ) else LoadingIndicator()
                        }
                    }
                }
            )
        },
    ) { innerPadding ->
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
                PreferenceGroup(stringResource(Res.string.local_rules_group)) {
                    localRulesList.forEach { item ->
                        switch(
                            title = item.title,
                            summary = item.summary,
                            checked = item.checked,
                            onCheckedChange = item.onCheckedChange
                        )
                    }
                }
            }
            item {
                PreferenceGroup(stringResource(Res.string.cloud_rules_group)) {
                    cloudRulesList.forEach { item ->
                        switch(
                            title = item.title,
                            summary = item.summary,
                            checked = item.checked,
                            onCheckedChange = item.onCheckedChange
                        )
                    }
                }
            }
            item {
                PreferenceGroup(stringResource(Res.string.exceptional_rules_group)) {
                    exceptionalRulesList.forEach { item ->
                        switch(
                            title = item.title,
                            summary = item.summary,
                            checked = item.checked,
                            onCheckedChange = item.onCheckedChange
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }

    if (cloudRulesSyncFailed) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCloudRulesSyncError() },
            title = { Text(text = stringResource(Res.string.sync_rules_failed_title)) },
            text = {
                val message = cloudRulesSyncErrorMessage
                Text(
                    text = message?.takeIf { it.isNotBlank() }
                        ?: stringResource(Res.string.sync_rules_failed_message)
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissCloudRulesSyncError() }) {
                    Text(text = stringResource(Res.string.confirm))
                }
            })
    }
}

private fun formatSyncTime(epochMillis: Long, locale: Locale, fallback: String = "-"): String {
    return try {
        val dateTime = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDateTime()
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(locale)
        Pangu.spacingText(dateTime.format(formatter))
    } catch (_: Exception) {
        fallback
    }
}

data class SwitchPreferenceItem(
    val title: String,
    val summary: String? = null,
    val checked: Boolean,
    val onCheckedChange: (Boolean) -> Unit
)

data class CloudRuleSpec(
    val titleResId: StringResource,
    val enabled: Boolean,
    val versionSummary: String?,
    val onCheckedChange: (Boolean) -> Unit
)
