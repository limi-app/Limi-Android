package personal.limi.utils

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import personal.limi.data.model.LimiHistoryEntity
import personal.limi.utils.room.LimiHistoryDao

const val limiRoomDB = "limi_room.db"

@Database(entities = [LimiHistoryEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getLimiHistoryDao(): LimiHistoryDao
}

fun getRoomDatabase(context: Context): AppDatabase {
    return Room.databaseBuilder(
        context.applicationContext, AppDatabase::class.java, limiRoomDB
    ).setDriver(BundledSQLiteDriver()).setQueryCoroutineContext(Dispatchers.IO).build()
}