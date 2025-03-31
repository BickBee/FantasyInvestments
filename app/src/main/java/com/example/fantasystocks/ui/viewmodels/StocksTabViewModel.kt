import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fantasystocks.DATA_FETCHING_DELAY_MS
import com.example.fantasystocks.database.StockDetails
import com.example.fantasystocks.database.StockRouter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StockTabState(
    val stockDetails: List<StockDetails> = emptyList(),
    val isLoading: Boolean = true
)

class StocksTabViewModel() : ViewModel() {
    private val _state = MutableStateFlow(StockTabState())
    val state: StateFlow<StockTabState> = _state.asStateFlow()

    init {
        startFetchingData()
    }

    private fun startFetchingData() {
        viewModelScope.launch {
            while (true) {
                try {
                    val stockDetails = StockRouter.getAvailableStocks()
                    _state.value = _state.value.copy(stockDetails = stockDetails, isLoading = false)
                    println("Fetched fresh stocks!")
                } catch (e: Exception) {
                    // Handle error case
                    Log.e("StocksTab", "Error fetching stocks", e)
                }
                delay(DATA_FETCHING_DELAY_MS)
            }
        }
    }

}