package com.example.fantasystocks

import com.example.fantasystocks.API.StockApiService
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class StockApiServiceTest {
    @Test
    fun `test getStockData returns valid response`() = runBlocking {
        val response = StockApiService.getStockData("AAPL", "2025-02-11", "2025-02-12")

        assertNotNull(response)
        assertEquals("AAPL", response?.ticker)
        assertEquals(1, response?.resultsCount)
    }

    @Test
    fun `test getStockData handles errors gracefully`() = runBlocking {
        val response = StockApiService.getStockData("INVALID", "2024-01-01", "2024-01-10")

        assertNull(response)
    }
}
