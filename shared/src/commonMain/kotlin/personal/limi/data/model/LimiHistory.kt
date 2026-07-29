package personal.limi.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Entity
data class LimiHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val originUrl: String,
    val processedUrl: String,
    val datetime: String = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        .toString()
)