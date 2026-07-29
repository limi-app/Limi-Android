package personal.limi.ui.share_panel

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import limi.shared.generated.resources.Res
import limi.shared.generated.resources.app_name
import limi.shared.generated.resources.confirm
import limi.shared.generated.resources.copy
import limi.shared.generated.resources.discard_and_back
import limi.shared.generated.resources.edit_original_text
import limi.shared.generated.resources.input_string_is_empty
import limi.shared.generated.resources.processing_results
import limi.shared.generated.resources.share
import org.jetbrains.compose.resources.stringResource
import personal.limi.utils.textCopyThenPost

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview
fun SharePanel(
    viewModel: SharePanelViewModel = viewModel { SharePanelViewModel() },
    onActivityClose: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val urlListScrollState = rememberScrollState()
    var editText by remember { mutableStateOf(viewModel.originalText ?: "") }

    LaunchedEffect(Unit) { showBottomSheet = true }

    LaunchedEffect(showBottomSheet) { if (!showBottomSheet) onActivityClose() }

    if (showBottomSheet) ModalBottomSheet(
        onDismissRequest = { showBottomSheet = false }, sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            )

            AnimatedContent(
                targetState = viewModel.isEditing, transitionSpec = {
                    if (!targetState) slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(durationMillis = 300)
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth },
                        animationSpec = tween(durationMillis = 300)
                    ) else slideInHorizontally(
                        initialOffsetX = { fullWidth -> -fullWidth },
                        animationSpec = tween(durationMillis = 300)
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(durationMillis = 300)
                    )
                }, label = "EditTransition"
            ) { editing ->
                if (editing) EditingContent(
                    editText = editText,
                    onEditTextChange = { editText = it },
                    onCancel = { viewModel.isEditing = false },
                    isCancelEnabled = !viewModel.originalText.isNullOrBlank(),
                    onConfirm = {
                        viewModel.updateAndProcessText(editText)
                        viewModel.isEditing = false
                    },
                    isConfirmEnabled = editText.isNotBlank() && (editText != viewModel.originalText || viewModel.originalText.isNullOrBlank())
                ) else ProcessingContent(
                    viewModel = viewModel,
                    urlListScrollState = urlListScrollState,
                    onStartEdit = {
                        viewModel.isEditing = true
                        editText = viewModel.originalText ?: ""
                    },
                    onCopyUrl = { coroutineScope.launch { context.textCopyThenPost(it) } },
                    onShare = { viewModel.shareText(context) },
                    onCopy = { viewModel.copyText(context) })
            }
        }
    }
}

@Composable
private fun EditingContent(
    editText: String,
    onEditTextChange: (String) -> Unit,
    onCancel: () -> Unit,
    isCancelEnabled: Boolean,
    onConfirm: () -> Unit,
    isConfirmEnabled: Boolean
) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, end = 16.dp, start = 16.dp, bottom = 16.dp)
    ) {
        OutlinedTextField(
            value = editText,
            onValueChange = onEditTextChange,
            label = { Text(stringResource(Res.string.edit_original_text)) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium,
            minLines = 1,
            maxLines = 6
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                onClick = {
                    focusManager.clearFocus()
                    onCancel()
                },
                enabled = isCancelEnabled,
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(Res.string.discard_and_back),
                    modifier = Modifier.size(
                        ButtonDefaults.IconSize
                    )
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(Res.string.discard_and_back))
            }
            FilledTonalButton(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                onClick = {
                    focusManager.clearFocus()
                    onConfirm()
                },
                enabled = isConfirmEnabled,
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = stringResource(Res.string.confirm),
                    modifier = Modifier.size(
                        ButtonDefaults.IconSize
                    )
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(Res.string.confirm))
            }
        }
    }
}

@Composable
private fun ProcessingContent(
    viewModel: SharePanelViewModel,
    urlListScrollState: androidx.compose.foundation.ScrollState,
    onStartEdit: () -> Unit,
    onCopyUrl: (String) -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EditCard(onClick = onStartEdit)

        ResultCard(viewModel = viewModel)

        if (!viewModel.isProcessing && viewModel.processedUrlList.isNotEmpty()) {
            UrlListRow(
                urlList = viewModel.processedUrlList,
                scrollState = urlListScrollState,
                onCopyUrl = onCopyUrl
            )
        }

        ActionButtonsRow(
            isEnabled = isButtonEnabled(viewModel), onShare = onShare, onCopy = onCopy
        )
    }
}

@Composable
private fun EditCard(onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                contentDescription = stringResource(Res.string.edit_original_text),
                modifier = Modifier.padding(end = 4.dp)
            )
            Text(
                text = stringResource(Res.string.edit_original_text),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ResultCard(viewModel: SharePanelViewModel) {
    val scrollState = rememberScrollState()

    // 只有当文本可以滚动时才消费滚动事件和 fling 事件
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset, available: Offset, source: NestedScrollSource
            ): Offset {
                return if (scrollState.canScrollForward || scrollState.canScrollBackward) available else Offset.Zero
            }

            override suspend fun onPostFling(
                consumed: Velocity, available: Velocity
            ): Velocity {
                return if (scrollState.canScrollForward || scrollState.canScrollBackward) available else Velocity.Zero
            }
        }
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
        onClick = { },
    ) {
        if (viewModel.isProcessing) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                LoadingIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                if (!viewModel.isProcessing && !viewModel.isEmpty) {
                    Text(
                        text = stringResource(Res.string.processing_results),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 170.dp)
                            .nestedScroll(nestedScrollConnection)
                    ) {
                        Text(
                            text = viewModel.processedText ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState),
                        )
                    }
                } else if (viewModel.isEmpty) {
                    Text(
                        text = stringResource(Res.string.input_string_is_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun UrlListRow(
    urlList: List<String>,
    scrollState: androidx.compose.foundation.ScrollState,
    onCopyUrl: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 12.dp)
            .horizontalScroll(scrollState), verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.size(width = 8.dp, height = 0.dp))
        if (urlList.size > 1) {
            for (url in urlList) {
                UrlCard(
                    url = url, onCopyUrl = onCopyUrl
                )
            }
        } else if (urlList.size == 1) {
            UrlCard(
                url = urlList.first(), onCopyUrl = onCopyUrl
            )
        }
        Spacer(Modifier.size(width = 8.dp, height = 0.dp))
    }
}

@Composable
private fun UrlCard(
    url: String, onCopyUrl: (String) -> Unit
) {
    CompositionLocalProvider(
        LocalMinimumInteractiveComponentSize provides 0.dp
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            modifier = Modifier.padding(start = 8.dp, end = 8.dp),
            onClick = { onCopyUrl(url) },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = url,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(ButtonDefaults.IconSize)
                )
                Text(
                    text = url,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun ActionButtonsRow(
    isEnabled: Boolean, onShare: () -> Unit, onCopy: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalButton(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            onClick = onShare,
            enabled = isEnabled,
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        ) {
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = stringResource(Res.string.share),
                modifier = Modifier.size(
                    ButtonDefaults.IconSize
                )
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(Res.string.share))
        }
        FilledTonalButton(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            onClick = onCopy,
            enabled = isEnabled,
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        ) {
            Icon(
                imageVector = Icons.Outlined.ContentCopy,
                contentDescription = stringResource(Res.string.copy),
                modifier = Modifier.size(
                    ButtonDefaults.IconSize
                )
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(Res.string.copy))
        }
    }
}

@Composable
private fun isButtonEnabled(viewModel: SharePanelViewModel): Boolean {
    return !viewModel.isEmpty && !viewModel.isProcessing && !viewModel.isNotHasUrls && !viewModel.isError
}