package personal.limi.utils.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import personal.limi.data.model.LimiHistoryEntity

@Dao
interface LimiHistoryDao {
    @Insert
    suspend fun insert(item: LimiHistoryEntity)

    @Query("SELECT count(*) FROM LimiHistoryEntity")
    suspend fun count(): Int

    @Query("SELECT * FROM LimiHistoryEntity")
    fun getAllAsFlow(): Flow<List<LimiHistoryEntity>>

    @Query("SELECT * FROM LimiHistoryEntity ORDER BY datetime DESC")
    fun getAllAsFlowSortedByDatetimeDesc(): Flow<List<LimiHistoryEntity>>

    @Query("SELECT * FROM LimiHistoryEntity ORDER BY datetime DESC")
    suspend fun getAllSortedByDatetimeDesc(): List<LimiHistoryEntity>
    
    @Delete
    suspend fun delete(item: LimiHistoryEntity)
}