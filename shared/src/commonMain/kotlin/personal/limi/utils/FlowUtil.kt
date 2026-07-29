package personal.limi.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * 将 DataStore 的 Flow 转换为 ViewModel 中的 StateFlow。
 *
 * 用法:
 *
 * ```Kotlin
 * val isDarkTheme = DataStorePreferences.getBooleanFlow("is_dark", false).asState(viewModelScope, false)
 * ```
 *
 * @param scope 通常传 `viewModelScope`
 * @param initialValue 初始值（在 DataStore 读取完成前显示的值）
 */
fun <T> Flow<T>.asState(scope: CoroutineScope, initialValue: T): StateFlow<T> {
    // 当 UI 不可见超过 5秒 后停止收集流，节省资源；
    // 当 UI 重新可见（如旋转屏幕）时无缝恢复，不会重新触发加载。
    return this.stateIn(
        scope = scope, started = SharingStarted.WhileSubscribed(5000), initialValue = initialValue
    )
}