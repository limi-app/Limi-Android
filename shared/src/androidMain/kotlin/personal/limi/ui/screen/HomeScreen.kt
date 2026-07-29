package personal.limi.ui.screen

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.AllInclusive
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.darkokoa.pangu.Pangu
import org.jetbrains.compose.resources.stringResource
import limi.shared.generated.resources.Res
import limi.shared.generated.resources.app_name
import limi.shared.generated.resources.from_text
import limi.shared.generated.resources.no_history
import limi.shared.generated.resources.scan_qrcode
import limi.shared.generated.resources.select_from_gallery
import personal.limi.ui.HistoryDetailActivity
import personal.limi.ui.MainViewModel
import personal.limi.ui.components.preference.PreferenceGroup
import personal.limi.ui.components.preference.navigation
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview
fun HomeScreen(viewModel: MainViewModel = viewModel { MainViewModel() }) {
    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val fabVisible by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset <= 0
        }
    }
    val focusRequester = FocusRequester()
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    val historyList by viewModel.historyListStateFlow.collectAsState()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(text = stringResource(Res.string.app_name)) },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = innerPadding.calculateStartPadding(layoutDirection),
                    end = innerPadding.calculateEndPadding(layoutDirection),
                    top = innerPadding.calculateTopPadding(),
                )
        ) {
            LazyColumn(
                state = listState, modifier = Modifier.fillMaxSize()
            ) {
                // 按日期分组显示历史记录
                val groupedHistory = historyList.groupBy {
                    // 提取日期部分 (YYYY-MM-DD)
                    it.datetime.substringBefore("T")
                }
                val locale = context.resources.configuration.locales[0]

                groupedHistory.forEach { (date, histories) ->
                    val localDate = LocalDate.parse(date)
                    val formatter =
                        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
                    val formattedDate = Pangu.spacingText(localDate.format(formatter))
                    item {
                        PreferenceGroup(title = formattedDate) {
                            histories.forEach { history ->
                                val timePart =
                                    history.datetime.substringAfter("T").substringBeforeLast(".")
                                navigation(
                                    title = timePart, summary = history.processedUrl, onClick = {
                                        val intent = Intent(
                                            context, HistoryDetailActivity::class.java
                                        ).apply {
                                            putExtra("history_id", history.id)
                                            putExtra("history_origin_url", history.originUrl)
                                            putExtra("history_processed_url", history.processedUrl)
                                            putExtra("history_datetime", history.datetime)
                                            flags =
                                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        }
                                        context.startActivity(intent)
                                    })
                            }
                        }
                    }
                }

                if (historyList.isEmpty()) item {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 16.dp, end = 16.dp, top = 64.dp, bottom = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(Res.string.no_history),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else item { Spacer(Modifier.height(8.dp)) }
            }

            val fromText = stringResource(Res.string.from_text)
            val scanQRCode = stringResource(Res.string.scan_qrcode)
            val selectFromGallery = stringResource(Res.string.select_from_gallery)

            val items = listOf(
                Icons.Outlined.DriveFileRenameOutline to fromText,
                Icons.Outlined.QrCodeScanner to scanQRCode,
                Icons.Outlined.AddPhotoAlternate to selectFromGallery
            )

            var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }

            BackHandler(fabMenuExpanded) { fabMenuExpanded = false }

            FloatingActionButtonMenu(
                modifier = Modifier.align(Alignment.BottomEnd),
                expanded = fabMenuExpanded,
                button = {
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                            if (fabMenuExpanded) TooltipAnchorPosition.Start else TooltipAnchorPosition.Above
                        ),
                        tooltip = { PlainTooltip { Text("Toggle menu") } },
                        state = rememberTooltipState(),
                    ) {
                        ToggleFloatingActionButton(
                            modifier = Modifier
                                .semantics {
                                    traversalIndex = -1f
                                    stateDescription =
                                        if (fabMenuExpanded) "Expanded" else "Collapsed"
                                    contentDescription = "Toggle menu"
                                }
                                .animateFloatingActionButton(
                                    visible = fabVisible || fabMenuExpanded,
                                    alignment = Alignment.BottomEnd,
                                )
                                .focusRequester(focusRequester),
                            checked = fabMenuExpanded,
                            onCheckedChange = { fabMenuExpanded = !fabMenuExpanded },
                        ) {
                            val imageVector by remember {
                                derivedStateOf {
                                    if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add
                                }
                            }
                            Icon(
                                painter = rememberVectorPainter(imageVector),
                                contentDescription = null,
                                modifier = Modifier.animateIcon({ checkedProgress }),
                            )
                        }
                    }
                },
            ) {
                val pickMedia = rememberLauncherForActivityResult(
                    ActivityResultContracts.PickVisualMedia()
                ) { uri ->
                    viewModel.startSelectFromGallery(context, uri)
                }
                items.forEachIndexed { i, item ->
                    FloatingActionButtonMenuItem(
                        modifier = Modifier
                            .semantics {
                                isTraversalGroup = true
                                if (i == items.size - 1) customActions = listOf(
                                    CustomAccessibilityAction(
                                        label = "Close menu",
                                        action = {
                                            fabMenuExpanded = false
                                            true
                                        },
                                    )
                                )
                            }
                            .then(if (i == 0) Modifier.onKeyEvent {
                                if (it.type == KeyEventType.KeyDown && (it.key == Key.DirectionUp || (it.isShiftPressed && it.key == Key.Tab))) {
                                    focusRequester.requestFocus()
                                    return@onKeyEvent true
                                }
                                return@onKeyEvent false
                            } else Modifier),
                        onClick = {
                            fabMenuExpanded = false
                            when (item.second) {
                                fromText -> viewModel.startSharePanel(context)
                                scanQRCode -> viewModel.startScanQRCode(context)
                                selectFromGallery -> pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        },
                        icon = { Icon(item.first, contentDescription = null) },
                        text = { Text(text = item.second) },
                    )
                }
            }
        }
    }
}