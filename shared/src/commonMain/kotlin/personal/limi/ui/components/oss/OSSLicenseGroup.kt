package personal.limi.ui.components.oss

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

// --- M3 Expressive Constants ---
private val OuterCornerRadius = 24.dp // 外部大圆角
private val InnerCornerRadius = 8.dp  // 内部微圆角 (连接处)
private val ItemSpacing = 6.dp        // Item 之间的物理间隙

// 定义 Item 在组内的位置，用于决定形状
enum class ItemPosition {
    Single, Top, Middle, Bottom
}

// 根据位置获取 Shape
private fun getShapeForPosition(position: ItemPosition): Shape {
    return when (position) {
        ItemPosition.Single -> RoundedCornerShape(OuterCornerRadius)
        ItemPosition.Top -> RoundedCornerShape(
            topStart = OuterCornerRadius, topEnd = OuterCornerRadius,
            bottomStart = InnerCornerRadius, bottomEnd = InnerCornerRadius
        )
        ItemPosition.Middle -> RoundedCornerShape(InnerCornerRadius)
        ItemPosition.Bottom -> RoundedCornerShape(
            topStart = InnerCornerRadius, topEnd = InnerCornerRadius,
            bottomStart = OuterCornerRadius, bottomEnd = OuterCornerRadius
        )
    }
}

// --- DSL Scope Definition ---
class OSSLicenseScope {
    val items = mutableListOf<@Composable (ItemPosition) -> Unit>()

    // 添加通用 Item
    fun item(content: @Composable (Shape) -> Unit) {
        items.add { position ->
            content(getShapeForPosition(position))
        }
    }
}

/**
 * 核心容器：Expressive 风格分组
 * 自动计算内部子元素的圆角逻辑
 */
@Composable
fun OSSLicenseGroup(
    title: String? = null,
    content: OSSLicenseScope.() -> Unit
) {
    val scope = OSSLicenseScope().apply(content)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
        }

        // 渲染所有 Item，并计算位置传给子组件
        scope.items.forEachIndexed { index, itemContent ->
            val position = when {
                scope.items.size == 1 -> ItemPosition.Single
                index == 0 -> ItemPosition.Top
                index == scope.items.lastIndex -> ItemPosition.Bottom
                else -> ItemPosition.Middle
            }

            itemContent(position)

            // 组内间距 (如果不是最后一个)
            if (index < scope.items.lastIndex) {
                Spacer(modifier = Modifier.height(ItemSpacing))
            }
        }
    }
}