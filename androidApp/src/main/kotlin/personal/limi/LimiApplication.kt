package personal.limi

import android.app.Application
import personal.limi.utils.AppDatabase
import personal.limi.utils.DatabaseHolder
import personal.limi.utils.datastore.DataStorePreferences
import personal.limi.utils.getRoomDatabase

class LimiApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        DataStorePreferences.apply {
            this@LimiApplication.initialize()
        }
        DatabaseHolder.initialize(getRoomDatabase(this))
    }
}