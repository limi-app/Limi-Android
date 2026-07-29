package personal.limi.utils.datastore

import kotlinx.coroutines.flow.Flow

interface DataStorePreferencesHelper {
    // String 类型
    suspend fun putString(key: String, value: String)
    fun putStringSync(key: String, value: String)
    suspend fun getString(key: String, defaultValue: String): String
    fun getStringSync(key: String, defaultValue: String): String
    fun getStringFlow(key: String, defaultValue: String): Flow<String>

    // Boolean 类型
    suspend fun putBoolean(key: String, value: Boolean)
    fun putBooleanSync(key: String, value: Boolean)
    suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun getBooleanSync(key: String, defaultValue: Boolean): Boolean
    fun getBooleanFlow(key: String, defaultValue: Boolean): Flow<Boolean>

    // Int 类型
    suspend fun putInt(key: String, value: Int)
    fun putIntSync(key: String, value: Int)
    suspend fun getInt(key: String, defaultValue: Int): Int
    fun getIntSync(key: String, defaultValue: Int): Int
    fun getIntFlow(key: String, defaultValue: Int): Flow<Int>

    // Long 类型
    suspend fun putLong(key: String, value: Long)
    fun putLongSync(key: String, value: Long)
    suspend fun getLong(key: String, defaultValue: Long): Long
    fun getLongSync(key: String, defaultValue: Long): Long
    fun getLongFlow(key: String, defaultValue: Long): Flow<Long>

    // Float 类型
    suspend fun putFloat(key: String, value: Float)
    fun putFloatSync(key: String, value: Float)
    suspend fun getFloat(key: String, defaultValue: Float): Float
    fun getFloatSync(key: String, defaultValue: Float): Float
    fun getFloatFlow(key: String, defaultValue: Float): Flow<Float>

    // Double 类型
    suspend fun putDouble(key: String, value: Double)
    fun putDoubleSync(key: String, value: Double)
    suspend fun getDouble(key: String, defaultValue: Double): Double
    fun getDoubleSync(key: String, defaultValue: Double): Double
    fun getDoubleFlow(key: String, defaultValue: Double): Flow<Double>

    // Set<String> 类型
    suspend fun putStringSet(key: String, value: Set<String>)
    fun putStringSetSync(key: String, value: Set<String>)
    suspend fun getStringSet(key: String, defaultValue: Set<String>): Set<String>
    fun getStringSetSync(key: String, defaultValue: Set<String>): Set<String>
    fun getStringSetFlow(key: String, defaultValue: Set<String>): Flow<Set<String>>

    // 清除所有数据
    suspend fun clear()
    fun clearSync()

    // 删除指定键
    suspend fun remove(key: String)
    fun removeSync(key: String)
}