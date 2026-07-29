package personal.limi.ui.components.preference

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

// --- 扩展函数，方便在 DSL 中使用 ---

// 1. 普通点击项 / 导航项
fun PreferenceScope.navigation(
    title: String,
    summary: String? = null,
    icon: ImageVector? = null,
    valueText: String? = null,
    showArrow: Boolean = true,
    onClick: () -> Unit
) {
    item { shape ->
        BasePreferenceCell(
            shape = shape,
            title = title,
            summary = summary,
            icon = icon,
            onClick = onClick,
            trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (valueText != null) {
                        Text(
                            text = valueText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    if (showArrow) Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }
}

// 2. 开关 (Switch)
fun PreferenceScope.switch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    summary: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    item { shape ->
        BasePreferenceCell(
            shape = shape,
            title = title,
            summary = summary,
            icon = icon,
            enabled = enabled,
            onClick = { onCheckedChange(!checked) },
            trailingContent = {
                Switch(
                    checked = checked,
                    onCheckedChange = null, // 点击事件交由 Cell 处理
                    enabled = enabled
                )
            }
        )
    }
}

// 3. 带输入框 (TextField)
// Android 16 风格中，输入框往往是点击后弹窗，或者直接嵌入在 Cell 中 (较少见)。
fun PreferenceScope.input(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Enter text",
    icon: ImageVector? = null
) {
    item { shape ->
        Surface(
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                    }

                    // 左侧标题
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(0.4f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = { Text(placeholder) },
                    modifier = Modifier.weight(0.6f).fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent
                    )
                )
            }
        }
    }
}

// 4. Slider Cell (音量/亮度风格)
// 在 M3 Expressive 中，Slider 常常是粗壮的药丸形状，这里模拟包含 Slider 的卡片。
fun PreferenceScope.slider(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    icon: ImageVector? = null
) {
    item { shape ->
        Surface(
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    Text(text = title, style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = value,
                    onValueChange = onValueChange,
                    valueRange = valueRange
                )
            }
        }
    }
}