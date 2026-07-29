package personal.limi.utils

/**
 * Singleton holder for the Room database instance.
 * Initialized by the Application class in the androidApp module.
 */
object DatabaseHolder {
    lateinit var database: AppDatabase
        private set

    fun initialize(db: AppDatabase) {
        database = db
    }
}
