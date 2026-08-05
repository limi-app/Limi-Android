package personal.limi.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import personal.limi.data.model.LimiHistoryEntity
import personal.limi.utils.DatabaseHolder
import personal.limi.utils.room.LimiHistoryDao

class HistoryDetailViewModel : ViewModel() {
  private val dao: LimiHistoryDao = DatabaseHolder.database.getLimiHistoryDao()

  fun deleteHistory(history: LimiHistoryEntity, onDeleteSuccess: () -> Unit) {
    viewModelScope.launch {
      try {
        dao.delete(history)
        onDeleteSuccess()
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }
}
