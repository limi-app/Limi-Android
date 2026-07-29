package personal.limi.utils.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath

private fun createCommonDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(produceFile = { producePath().toPath() })

internal const val dataStoreFileName = "dice.preferences_pb"

actual object DataStorePreferences : DataStorePreferencesHelper {
    private lateinit var dataStore: DataStore<Preferences>

    fun Context.initialize() {
        if (!::dataStore.isInitialized) {
            dataStore = createCommonDataStore(
                producePath = { this.applicationContext.filesDir.resolve(dataStoreFileName).absolutePath }
            )
        }
    }
    /**
     * 异步存储字符串值
     *
     * @param key 键名
     * @param value 要存储的字符串值
     */
    override suspend fun putString(key: String, value: String) {
        putValue(key, value) { stringPreferencesKey(it) }
    }

    /**
     * 同步存储字符串值
     *
     * @param key 键名
     * @param value 要存储的字符串值
     */
    override fun putStringSync(key: String, value: String) {
        putValueSync(key, value) { stringPreferencesKey(it) }
    }

    /**
     * 异步获取字符串值
     *
     * @param key 键名
     * @param defaultValue 默认值，当键不存在时返回此值
     * @return 存储的字符串值或默认值
     */
    override suspend fun getString(key: String, defaultValue: String): String {
        return getValue(key, defaultValue) { stringPreferencesKey(it) }
    }

    /**
     * 同步获取字符串值
     *
     * @param key 键名
     * @param defaultValue 默认值，当键不存在时返回此值
     * @return 存储的字符串值或默认值
     */
    override fun getStringSync(key: String, defaultValue: String): String {
        return getValueSync(key, defaultValue) { stringPreferencesKey(it) }
    }

    /**
     * 获取字符串值的Flow
     *
     * @param key 键名
     * @param defaultValue 默认值，当键不存在时返回此值
     * @return 包含字符串值的Flow
     */
    override fun getStringFlow(key: String, defaultValue: String): Flow<String> {
        return getValueFlow(key, defaultValue) { stringPreferencesKey(it) }
    }

    /**
     * 异步存储布尔值
     *
     * @param key 键名
     * @param value 要存储的布尔值
     */
    override suspend fun putBoolean(key: String, value: Boolean) {
        putValue(key, value) { booleanPreferencesKey(it) }
    }

    /**
     * 同步存储布尔值
     *
     * @param key 键名
     * @param value 要存储的布尔值
     */
    override fun putBooleanSync(key: String, value: Boolean) {
        putValueSync(key, value) { booleanPreferencesKey(it) }
    }

    /**
     * 异步获取布尔值
     *
     * @param key 键名
     * @param defaultValue 默认值，当键不存在时返回此值
     * @return 存储的布尔值或默认值
     */
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return getValue(key, defaultValue) { booleanPreferencesKey(it) }
    }

    /**
     * 同步获取布尔值
     *
     * @param key 键名
     * @param defaultValue 默认值，当键不存在时返回此值
     * @return 存储的布尔值或默认值
     */
    override fun getBooleanSync(key: String, defaultValue: Boolean): Boolean {
        return getValueSync(key, defaultValue) { booleanPreferencesKey(it) }
    }

    /**
     * 获取布尔值的Flow
     *
     * @param key 键名
     * @param defaultValue 默认值，当键不存在时返回此值
     * @return 包含布尔值的Flow
     */
    override fun getBooleanFlow(key: String, defaultValue: Boolean): Flow<Boolean> {
        return getValueFlow(key, defaultValue) { booleanPreferencesKey(it) }
    }

    /**
     * 异步存储整数值
     *
     * @param key 键名
     * @param value 要存储的整数值
     */
    override suspend fun putInt(key: String, value: Int) {
        putValue(key, value) { intPreferencesKey(it) }
    }

    /**
     * 同步存储整数值
     *
     * @param key 键名
     * @param value 要存储的整数值
     */
    override fun putIntSync(key: String, value: Int) {
        putValueSync(key, value) { intPreferencesKey(it) }
    }

    /**
     * 异步获取整数值
     *
     * @param key 键名
     * @param defaultValue 默认值，当键不存在时返回此值
     * @return 存储的整数值或默认值
     */
    override suspend fun getInt(key: String, defaultValue: Int): Int {
        return getValue(key, defaultValue) { intPreferencesKey(it) }
    }

    /**
     * 同步获取整数值
     *
     * @param key 键名
     * @param defaultValue 默认值，当键不存在时返回此值
     * @return 存储的整数值或默认值
     */
    override fun getIntSync(key: String, defaultValue: Int): Int {
        return getValueSync(key, defaultValue) { intPreferencesKey(it) }
    }

    /**
     * 获取整数值的Flow
     *
     * @param key 键名
     * @param defaultValue 默认值，当键不存在时返回此值
     * @return 包含整数值的Flow
     */
    override fun getIntFlow(key: String, defaultValue: Int): Flow<Int> {
        return getValueFlow(key, defaultValue) { intPreferencesKey(it) }
    }

    /**
     * 异步存储长整数值
     *
     * @param key 键名
     * @param value 要存储的长整数值
     */
    override suspend fun putLong(key: String, value: Long) {
        putValue(key, value) { longPreferencesKey(it) }
    }

    /**
     * 同步存储长整数值
     *
     * @param key 键名
     * @param value 要存储的长整数值
     */
    override fun putLongSync(key: String, value: Long) {
        putValueSync(key, value) { longPreferencesKey(it) }
    }

    /**
     * 异步获取长整数值
     *
     * @param key 键名
     * @param defaultValue 默认值，当键不存在时返回此值
     * @return 存储的长整数值或默认值
     */
    override suspend fun getLong(key: String, defaultValue: Long): Long {
        return getValue(key, defaultValue) { longPreferencesKey(it) }
    }

    /**
     * 同步获取长整数值
     *
     * @param key 键名
     * @param defaultValue 默认值，当键不存在时返回此值
     * @return 存储的长整数值或默认值
     */
    override fun getLongSync(key: String, defaultValue: Long): Long {
        return getValueSync(key, defaultValue) { longPreferencesKey(it) }
    }

    /**
     * 获取长整数值的Flow
     *
     * @param key 键名
     * @param defaultValue 默认值，当键不存在时返回此值
     * @return 包含长整数值的Flow
     */
    override fun getLongFlow(key: String, defaultValue: Long): Flow<Long> {
        return getValueFlow(key, defaultValue) { longPreferencesKey(it) }
    }

    /**
     * 异步存储浮点数值
     *
     * @param key 键名
     * @param value 要存储的浮点数值
     */
    override suspend fun putFloat(key: String, value: Float) {
        putValue(key, value) { floatPreferencesKey(it) }
    }

    /**
     * 同步存储浮点数值
     *
     * @param key 键名
     * @param value 要存储的浮点数值
     */
    override fun putFloatSync(key: String, value: Float) {
        putValueSync(key, value) { floatPreferencesKey(it) }
    }

    /**
     * 异步获取浮点数值
     *
     * @param key 键名
     * @param defaultValue 默认值，当键不存在时返回此值
     * @return 存储的浮点数值或默认值
     */
    override suspend fun getFloat(key: String, defaultValue: Float): Float {
        return getValue(key, defaultValue) { floatPreferencesKey(it) }
    }

    /**
     * 同步获取浮点数值
     *
     * @param key 键名
     * @param defaultValue 默认值，当键不存在时返回此值
     * @return 存储的浮点数值或默认值
     */
    override fun getFloatSync(key: String, defaultValue: Float): Float {
        return getValueSync(key, defaultValue) { floatPreferencesKey(it) }
    }

    /**
     * 获取浮点数值的Flow
     *
     * @param key 键名
     * @param defaultValue 默认值，当键不存在时返回此值
     * @return 包含浮点数值的Flow
     */
    override fun getFloatFlow(key: String, defaultValue: Float): Flow<Float> {
        return getValueFlow(key, defaultValue) { floatPreferencesKey(it) }
    }

    /**
     * 异步存储双精度浮点数值
     *
     * @param key 键名
     * @param value 要存储的双精度浮点数值
     */
    override suspend fun putDouble(key: String, value: Double) {
        putValue(key, value) { doublePreferencesKey(it) }
    }

    /**
     * 同步存储双精度浮点数值
     *
     * @param key 键名
     * @param value 要存储的双精度浮点数值
     */
    override fun putDoubleSync(key: String, value: Double) {
        putValueSync(key, value) { doublePreferencesKey(it) }
    }

    /**
     * 异步获取双精度浮点数值
     *
     * @param key 键名
     * @param defaultValue 默认值，当键不存在时返回此值
     * @return 存储的双精度浮点数值或默认值
     */
    override suspend fun getDouble(key: String, defaultValue: Double): Double {
        return getValue(key, defaultValue) { doublePreferencesKey(it) }
    }

    /**
     * 同步获取双精度浮点数值
     *
     * @param key 键名
     * @param defaultValue 默认值，当键不存在时返回此值
     * @return 存储的双精度浮点数值或默认值
     */
    override fun getDoubleSync(key: String, defaultValue: Double): Double {
        return getValueSync(key, defaultValue) { doublePreferencesKey(it) }
    }

    /**
     * 获取双精度浮点数值的Flow
     *
     * @param key 键名
     * @param defaultValue 默认值，当键不存在时返回此值
     * @return 包含双精度浮点数值的Flow
     */
    override fun getDoubleFlow(key: String, defaultValue: Double): Flow<Double> {
        return getValueFlow(key, defaultValue) { doublePreferencesKey(it) }
    }

    /**
     * 异步存储字符串集合值
     *
     * @param key 键名
     * @param value 要存储的字符串集合值
     */
    override suspend fun putStringSet(key: String, value: Set<String>) {
        putValue(key, value) { stringSetPreferencesKey(it) }
    }

    /**
     * 同步存储字符串集合值
     *
     * @param key 键名
     * @param value 要存储的字符串集合值
     */
    override fun putStringSetSync(key: String, value: Set<String>) {
        putValueSync(key, value) { stringSetPreferencesKey(it) }
    }

    /**
     * 异步获取字符串集合值
     *
     * @param key 键名
     * @param defaultValue 默认值，当键不存在时返回此值
     * @return 存储的字符串集合值或默认值
     */
    override suspend fun getStringSet(key: String, defaultValue: Set<String>): Set<String> {
        return getValue(key, defaultValue) { stringSetPreferencesKey(it) }
    }

    /**
     * 同步获取字符串集合值
     *
     * @param key 键名
     * @param defaultValue 默认值，当键不存在时返回此值
     * @return 存储的字符串集合值或默认值
     */
    override fun getStringSetSync(key: String, defaultValue: Set<String>): Set<String> {
        return getValueSync(key, defaultValue) { stringSetPreferencesKey(it) }
    }

    /**
     * 获取字符串集合值的Flow
     *
     * @param key 键名
     * @param defaultValue 默认值，当键不存在时返回此值
     * @return 包含字符串集合值的Flow
     */
    override fun getStringSetFlow(key: String, defaultValue: Set<String>): Flow<Set<String>> {
        return getValueFlow(key, defaultValue) { stringSetPreferencesKey(it) }
    }

    /**
     * 异步清除所有数据
     */
    override suspend fun clear() {
        dataStore.edit { preferences -> preferences.clear() }
    }

    /**
     * 同步清除所有数据
     */
    override fun clearSync() {
        runBlocking { clear() }
    }

    /**
     * 异步移除指定键的数据
     *
     * @param key 要移除的键名
     */
    override suspend fun remove(key: String) {
        removeKey(key)
    }

    /**
     * 同步移除指定键的数据
     *
     * @param key 要移除的键名
     */
    override fun removeSync(key: String) {
        removeKeySync(key)
    }

    private suspend inline fun <T> putValue(key: String, value: T, crossinline keyCreator: (String) -> Preferences.Key<T>) {
        dataStore.edit { preferences ->
            preferences[keyCreator(key)] = value
        }
    }

    private inline fun <T> putValueSync(key: String, value: T, crossinline keyCreator: (String) -> Preferences.Key<T>) {
        runBlocking { putValue(key, value, keyCreator) }
    }

    private suspend inline fun <T> getValue(key: String, defaultValue: T, crossinline keyCreator: (String) -> Preferences.Key<T>): T {
        return dataStore.data.map { preferences ->
            preferences[keyCreator(key)] ?: defaultValue
        }.first()
    }

    private inline fun <T> getValueSync(key: String, defaultValue: T, crossinline keyCreator: (String) -> Preferences.Key<T>): T {
        return runBlocking { getValue(key, defaultValue, keyCreator) }
    }

    private inline fun <T> getValueFlow(key: String, defaultValue: T, crossinline keyCreator: (String) -> Preferences.Key<T>): Flow<T> {
        return dataStore.data.map { preferences ->
            preferences[keyCreator(key)] ?: defaultValue
        }
    }

    private suspend fun removeKey(keyName: String) {
        dataStore.edit { preferences ->
            val keyToRemove = preferences.asMap().keys.firstOrNull { it.name == keyName }
            if (keyToRemove != null) preferences.remove(keyToRemove)
        }
    }

    private fun removeKeySync(keyName: String) {
        runBlocking { removeKey(keyName) }
    }
}
