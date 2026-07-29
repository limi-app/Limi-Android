package personal.limi.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import personal.limi.data.model.LimiHistoryEntity
import personal.limi.theme.LimiTheme
import personal.limi.ui.screen.HistoryDetailScreen

class HistoryDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // 从Intent获取历史记录数据
        val history = LimiHistoryEntity(
            id = intent.getLongExtra("history_id", 0),
            originUrl = intent.getStringExtra("history_origin_url") ?: "",
            processedUrl = intent.getStringExtra("history_processed_url") ?: "",
            datetime = intent.getStringExtra("history_datetime") ?: ""
        )

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent
            ) {
                LimiTheme { HistoryDetailScreen(history = history, onBack = { finish() }) }
            }
        }
    }
}