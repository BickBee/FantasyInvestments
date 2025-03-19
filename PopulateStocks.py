import requests
import asyncio
import time
from datetime import datetime, timedelta
from supabase import create_client, Client

# Polygon.io API Key
POLYGON_API_KEY = "V3H2llAJBWPwTeZtRA8_YkZpcy_3e_jr"
OPEN_CLOSE_URL = "https://api.polygon.io/v1/open-close/{ticker}/{date}?adjusted=true&apiKey={api_key}"

# Supabase API Connection
SUPABASE_URL = "https://lnfecoxuwybrlhzjqxkb.supabase.co"
SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImxuZmVjb3h1d3licmxoempxeGtiIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Mzg5NDQwMTMsImV4cCI6MjA1NDUyMDAxM30.wCjoCRqMTLOWyPxX-9lMohKbxESbP8z6G0FM2Gk3GLY"

# Create Supabase client
supabase: Client = create_client(SUPABASE_URL, SUPABASE_KEY)

# Get the most recent valid trading day
def get_previous_trading_day():
    previous_day = datetime.now() - timedelta(1)  # Start with yesterday
    while previous_day.weekday() >= 5:  # Skip weekends
        previous_day -= timedelta(1)
    return previous_day.strftime("%Y-%m-%d")

async def fetch_all_stock_data(tickers):
    """Fetch stock data for all tickers before inserting into the database."""
    all_stock_data = []
    date = get_previous_trading_day()  # Ensure all requests use the same valid date
    for i, ticker in enumerate(tickers):
        stock_data = await fetch_stock_data(ticker, date)
        print(f"API call completed for {ticker} on {date}.")
        if stock_data:
            all_stock_data.append(stock_data)
            print(f"Fetched data for {ticker} on {date}.")
        if (i + 1) % 5 == 0:  # Rate limit: 5 requests per minute
            print("Rate limit reached. Waiting for 60 seconds...")
            time.sleep(65)
    return all_stock_data

async def fetch_stock_data(ticker, date):
    """Fetch stock data from Polygon.io."""
    url = OPEN_CLOSE_URL.format(ticker=ticker, date=date, api_key=POLYGON_API_KEY)
    response = requests.get(url)
    if response.status_code == 200:
        data = response.json()
        if "status" in data and data["status"] == "OK":
            return {
                "ticker": data["symbol"],
                "name": data["symbol"],  # Name is not available in this API call
                "open": data.get("open", 0),
                "high": data.get("high", 0),
                "low": data.get("low", 0),
                "close": data.get("close", 0)
            }
    print(f"Failed to fetch {ticker} for {date}: {response.status_code}")
    return None

async def upsert_stock_data(all_stock_data):
    """Insert or update stock data into the historical_stock_price table."""
    for stock_data in all_stock_data:
        # Fetch stock_id for the given ticker
        stock_query = supabase.table("stock").select("stock_id").eq("ticker", stock_data["ticker"]).execute()
        
        if not stock_query.data:
            print(f"Stock {stock_data['ticker']} not found in the stock table. Adding it now.")
            insert_response = supabase.table("stock").insert({
                "name": stock_data["name"],
                "ticker": stock_data["ticker"]
            }).execute()
            print(f"Inserted new stock: {stock_data['ticker']} - {insert_response}")
            
            # Re-fetch stock_id after insertion
            stock_query = supabase.table("stock").select("stock_id").eq("ticker", stock_data["ticker"]).execute()
            if not stock_query.data:
                print(f"Error: Could not retrieve stock_id for {stock_data['ticker']} after insertion.")
                continue
        
        stock_id = stock_query.data[0]["stock_id"]
        
        # Insert historical stock price
        price_response = supabase.table("historical_stock_price").insert({
            "stock_id": stock_id,
            "timestamp": datetime.now().isoformat(),
            "open": stock_data["open"],
            "close": stock_data["close"],
            "high": stock_data["high"],
            "low": stock_data["low"]
        }).execute()
        print(f"Inserted historical price for {stock_data['ticker']} - {price_response}")
        
        # Log after each stock is successfully added
        print(f"Successfully processed stock data for {stock_data['ticker']}.")

async def main():
    """Main function to fetch and update stock data."""
    tickers = ["AAPL", "MSFT", "GOOGL", "AMZN", "TSLA", "NVDA", "BRK.B", "FB", "JNJ", "V", "UNH", "JPM", "XOM", "PG", "MA", "HD", "CVX", "ABBV", "LLY", "AVGO", "COST", "MRK", "PEP", "KO", "MCD", "TMO", "DHR", "WMT", "ACN", "AMD", "LIN", "NFLX", "BMY", "PM", "TXN", "ADBE", "HON", "ORCL", "LOW", "INTC", "RTX", "UPS", "MS", "GS", "NOW", "SCHW", "CAT", "AMT", "BA", "IBM"]
    all_stock_data = await fetch_all_stock_data(tickers)
    await upsert_stock_data(all_stock_data)

if __name__ == "__main__":
    asyncio.run(main())
