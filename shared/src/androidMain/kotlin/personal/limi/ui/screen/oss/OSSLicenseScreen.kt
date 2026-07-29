package personal.limi.ui.screen.oss

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.aboutlibraries.Libs
import limi.shared.generated.resources.Res
import limi.shared.generated.resources.back
import limi.shared.generated.resources.open_source_license
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import personal.limi.utils.openUrl

@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun OSSLicense(uniqueId: String, finish: () -> Unit) {
    var libraries by remember { mutableStateOf<Libs?>(null) }
    LaunchedEffect(Unit) {
        val json = Res.readBytes("files/aboutlibraries.json").decodeToString()
        libraries = Libs.Builder().withJson(json).build()
    }
    val library by remember(libraries) {
        derivedStateOf {
            libraries?.libraries?.find { it.uniqueId == uniqueId }
        }
    }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()
    val layoutDirection = LocalLayoutDirection.current
    val context = LocalContext.current

    if (libraries != null && library == null) {
        finish()
        return
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
            TopAppBar(
                title = { Text(text = stringResource(Res.string.open_source_license)) },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                navigationIcon = {
                    IconButton(onClick = finish) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(Res.string.back)
                        )
                    }
                },
            )
        }) { innerPadding ->
        library?.let {
            LazyColumn(
                state = listState, modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = innerPadding.calculateStartPadding(layoutDirection),
                        end = innerPadding.calculateEndPadding(layoutDirection),
                        top = innerPadding.calculateTopPadding(),
                    )
                    .padding(horizontal = 16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(16.dp)) }
                item {
                    val url = if (library!!.website != null) library!!.website.toString() else ""
                    if (url.isNotEmpty()) Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        border = BorderStroke(0.dp, MaterialTheme.colorScheme.secondaryContainer),
                        onClick = {
                            if (url.isNotEmpty()) context.openUrl(url)
                        }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Link,
                                contentDescription = stringResource(Res.string.open_source_license),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(
                                text = url, style = TextStyle(
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }
                }
                library!!.licenses.forEach { license ->
                    item {
                        if (!license.licenseContent.isNullOrEmpty()) SelectionContainer {
                            Text(
                                text = license.licenseContent ?: "",
                                style = typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        } else if (license.name.isNotEmpty()) SelectionContainer {
                            Text(
                                text = license.name,
                                style = typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
                item { Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding())) }
            }
        }
    }
}
