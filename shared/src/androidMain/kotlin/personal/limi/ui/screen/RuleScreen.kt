package personal.limi.ui.screen

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import limi.shared.generated.resources.Res
import limi.shared.generated.resources.bilibili_rule
import limi.shared.generated.resources.bilibili_rule_desc
import limi.shared.generated.resources.common_params_rule
import limi.shared.generated.resources.common_params_rule_desc
import limi.shared.generated.resources.common_rules_group
import limi.shared.generated.resources.exceptional_rules_group
import limi.shared.generated.resources.rule
import limi.shared.generated.resources.utm_enhanced_rule
import limi.shared.generated.resources.utm_enhanced_rule_desc
import limi.shared.generated.resources.utm_rule
import limi.shared.generated.resources.utm_rule_desc
import limi.shared.generated.resources.x_rule
import personal.limi.ui.MainViewModel
import personal.limi.ui.components.preference.PreferenceGroup
import personal.limi.ui.components.preference.switch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleScreen(
    viewModel: MainViewModel = viewModel { MainViewModel() }, titleResId: StringResource = Res.string.rule
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()
    val layoutDirection = LocalLayoutDirection.current
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
        val commonParamsRuleTitle = stringResource(Res.string.common_params_rule)
        val commonParamsRuleDesc = stringResource(Res.string.common_params_rule_desc)
        val commonParamsRuleEnabled by viewModel.isCommonParamsRuleEnabled.collectAsState()
        val utmRuleTitle = stringResource(Res.string.utm_rule)
        val utmRuleDesc = stringResource(Res.string.utm_rule_desc)
        val utmRuleEnabled by viewModel.isUTMParamsRuleEnabled.collectAsState()
        val utmEnhancedTitle = stringResource(Res.string.utm_enhanced_rule)
        val utmEnhancedDesc = stringResource(Res.string.utm_enhanced_rule_desc)
        val utmEnhancedEnabled by viewModel.isUTMParamsEnhancedRuleEnabled.collectAsState()
        val commonRulesList = listOf(
            SwitchPreferenceItem(
                title = commonParamsRuleTitle,
                summary = commonParamsRuleDesc,
                checked = commonParamsRuleEnabled,
                onCheckedChange = { bool -> viewModel.setCommonParamsRuleEnabled(bool) }),
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
        val xRuleTitle = stringResource(Res.string.x_rule)
        val xRuleEnable by viewModel.isXRuleEnabled.collectAsState()
        val exceptionalRulesList = listOf(
            SwitchPreferenceItem(
                title = bilibiliRuleTitle,
                summary = bilibiliRuleDesc,
                checked = bilibiliRuleEnabled,
                onCheckedChange = { bool -> viewModel.setBilibiliRuleEnabled(bool) }),
            SwitchPreferenceItem(
                title = xRuleTitle,
                checked = xRuleEnable,
                onCheckedChange = { bool -> viewModel.setXRuleEnabled(bool) }
            )
        )
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
                PreferenceGroup(stringResource(Res.string.common_rules_group)) {
                    commonRulesList.forEach { item ->
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
}

data class SwitchPreferenceItem(
    val title: String,
    val summary: String? = null,
    val checked: Boolean,
    val onCheckedChange: (Boolean) -> Unit
)
