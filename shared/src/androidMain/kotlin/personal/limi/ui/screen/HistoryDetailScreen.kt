package personal.limi.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.darkokoa.pangu.Pangu
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import limi.shared.generated.resources.Res
import limi.shared.generated.resources.back
import limi.shared.generated.resources.cancel
import limi.shared.generated.resources.confirm
import limi.shared.generated.resources.confirm_delete_message
import limi.shared.generated.resources.confirm_delete_title
import limi.shared.generated.resources.delete
import limi.shared.generated.resources.history_detail
import limi.shared.generated.resources.original_text
import limi.shared.generated.resources.processing_results
import limi.shared.generated.resources.time_label
import org.jetbrains.compose.resources.stringResource
import personal.limi.data.model.LimiHistoryEntity
import personal.limi.ui.HistoryDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailScreen(
  history: LimiHistoryEntity,
  onBack: () -> Unit,
  viewModel: HistoryDetailViewModel = viewModel { HistoryDetailViewModel() },
) {
  var showDialog by remember { mutableStateOf(false) }
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
  val listState = rememberScrollState()
  val layoutDirection = LocalLayoutDirection.current

  if (showDialog) {
    AlertDialog(
      onDismissRequest = { showDialog = false },
      title = { Text(text = stringResource(Res.string.confirm_delete_title)) },
      text = { Text(text = stringResource(Res.string.confirm_delete_message)) },
      confirmButton = {
        TextButton(
          onClick = {
            viewModel.deleteHistory(history) {
              showDialog = false
              onBack()
            }
          }
        ) {
          Text(text = stringResource(Res.string.confirm))
        }
      },
      dismissButton = {
        TextButton(onClick = { showDialog = false }) {
          Text(text = stringResource(Res.string.cancel))
        }
      },
    )
  }

  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      TopAppBar(
        title = { Text(text = stringResource(Res.string.history_detail)) },
        scrollBehavior = scrollBehavior,
        colors =
          TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
          ),
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(
              imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
              contentDescription = stringResource(Res.string.back),
            )
          }
        },
        actions = {
          IconButton(onClick = { showDialog = true }) {
            Icon(
              imageVector = Icons.Outlined.Delete,
              contentDescription = stringResource(Res.string.delete),
            )
          }
        },
      )
    },
  ) { innerPadding ->
    Column(
      modifier =
        Modifier.fillMaxSize()
          .padding(
            start = innerPadding.calculateStartPadding(layoutDirection),
            end = innerPadding.calculateEndPadding(layoutDirection),
            top = innerPadding.calculateTopPadding(),
          )
          .verticalScroll(listState)
          .padding(horizontal = 16.dp)
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      // 处理结果
      Text(
        text = stringResource(Res.string.processing_results),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelMedium,
      )

      SelectionContainer {
        Text(
          text = history.processedUrl,
          modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
          style = MaterialTheme.typography.bodyMedium,
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 原始链接
      Text(
        text = stringResource(Res.string.original_text),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelMedium,
      )

      SelectionContainer {
        Text(
          text = history.originUrl,
          modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
          style = MaterialTheme.typography.bodyMedium,
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = stringResource(Res.string.time_label),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelMedium,
      )

      SelectionContainer {
        Text(
          text = Pangu.spacingText(formatDateTime(history.datetime, LocalContext.current)),
          modifier = Modifier.padding(vertical = 8.dp),
          style = MaterialTheme.typography.bodyMedium,
        )
      }

      Spacer(modifier = Modifier.height(16.dp))
      Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding()))
    }
  }
}

private fun formatDateTime(dateTimeString: String, context: android.content.Context): String {
  return try {
    val dateTime = LocalDateTime.parse(dateTimeString.replace(" ", "T"))
    val locale = context.resources.configuration.locales[0]
    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(locale)
    dateTime.format(formatter)
  } catch (_: Exception) {
    dateTimeString
  }
}
