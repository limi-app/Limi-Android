package personal.limi.ui.components.preference

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Android16SettingsScreen() {
    var wifiOn by remember { mutableStateOf(true) }
    var bluetoothOn by remember { mutableStateOf(true) }
    var deviceName by remember { mutableStateOf("Pixel 9 Pro") }
    var volume by remember { mutableFloatStateOf(0.7f) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background, // 纯黑或纯白背景
        topBar = {
            LargeTopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Group 1: Connectivity (演示 Top/Middle/Bottom 逻辑)
            PreferenceGroup(title = "Connectivity") {
                // Top: 上大圆角，下小圆角
                switch(
                    title = "Internet",
                    summary = "Wi-Fi, Mobile, Data usage",
                    icon = Icons.Default.Wifi,
                    checked = wifiOn,
                    onCheckedChange = { wifiOn = it }
                )

                // Middle: 上下都是小圆角 (2dp-4dp)
                switch(
                    title = "Bluetooth",
                    summary = if (bluetoothOn) "On" else "Off",
                    icon = Icons.Default.Bluetooth,
                    checked = bluetoothOn,
                    onCheckedChange = { bluetoothOn = it }
                )

                // Bottom: 上小圆角，下大圆角
                navigation(
                    title = "SIM Manager",
                    summary = "eSIM, Dual SIM",
                    icon = Icons.Default.SimCard,
                    onClick = {}
                )
            }

            // Group 2: Device Info (演示 Input 和 Single)
            PreferenceGroup(title = "Device") {
                // 如果只有一个 Item，会自动变成全大圆角
                input(
                    title = "Device Name",
                    value = deviceName,
                    onValueChange = { deviceName = it },
                    icon = Icons.Default.Edit
                )
            }

            // Group 3: Sound (演示 Slider 和 Navigation)
            PreferenceGroup(title = "Sound & Vibration") {
                slider(
                    title = "Media Volume",
                    value = volume,
                    onValueChange = { volume = it },
                    icon = Icons.AutoMirrored.Filled.VolumeUp
                )

                navigation(
                    title = "Phone Ringtone",
                    valueText = "Pixel Sound",
                    icon = Icons.Default.MusicNote,
                    onClick = {}
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}