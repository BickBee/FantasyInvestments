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

# Get a list of trading days for the past month
def get_trading_days_past_month():
    trading_days = []
    current_date = datetime.now() - timedelta(1)  # Start with yesterday
    end_date = current_date - timedelta(30)  # Go back 30 days
    
    while current_date >= end_date:
        # Skip weekends (Saturday = 5, Sunday = 6)
        if current_date.weekday() < 5:
            trading_days.append(current_date.strftime("%Y-%m-%d"))
        current_date -= timedelta(1)
    
    return trading_days

async def fetch_stock_data(ticker, date):
    """Fetch stock data from Polygon.io for a specific ticker and date."""
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
                "close": data.get("close", 0),
                "date": date  # Store the date for this data point
            }
    print(f"Failed to fetch {ticker} for {date}: {response.status_code}")
    return None

async def get_or_create_stock_id(ticker, name):
    """Get existing stock_id or create a new entry if it doesn't exist."""
    stock_query = supabase.table("stock").select("stock_id").eq("ticker", ticker).execute()
    
    if not stock_query.data:
        print(f"Stock {ticker} not found in the stock table. Adding it now.")
        insert_response = supabase.table("stock").insert({
            "name": name,
            "ticker": ticker
        }).execute()
        print(f"Inserted new stock: {ticker}")
        
        # Re-fetch stock_id after insertion
        stock_query = supabase.table("stock").select("stock_id").eq("ticker", ticker).execute()
        if not stock_query.data:
            print(f"Error: Could not retrieve stock_id for {ticker} after insertion.")
            return None
    
    return stock_query.data[0]["stock_id"]

async def insert_stock_price(stock_id, stock_data):
    """Insert a single stock price data point into the database."""
    # Parse the date string into a datetime object
    date_object = datetime.strptime(stock_data["date"], "%Y-%m-%d")
    
    # Insert historical stock price with the specific date
    price_response = supabase.table("historical_stock_price").insert({
        "stock_id": stock_id,
        "timestamp": date_object.isoformat(),
        "open": stock_data["open"],
        "close": stock_data["close"],
        "high": stock_data["high"],
        "low": stock_data["low"]
    }).execute()
    
    print(f"Inserted historical price for {stock_data['ticker']} on {stock_data['date']}")
    return price_response

async def process_ticker_for_month(ticker, trading_days):
    """Process one ticker for all trading days in the past month."""
    print(f"Starting to process {ticker} for the past month...")
    
    # Get or create stock_id first (do this only once per ticker)
    stock_id = await get_or_create_stock_id(ticker, ticker)
    if not stock_id:
        print(f"Could not process {ticker} due to missing stock_id.")
        return 0
    
    data_points_added = 0
    
    # Process each trading day for this ticker
    for date in trading_days:
        # Fetch data for this ticker and date
        stock_data = await fetch_stock_data(ticker, date)
        
        if stock_data:
            # Immediately insert this data point into the database
            await insert_stock_price(stock_id, stock_data)
            data_points_added += 1
            print(f"Processed {ticker} for {date}")
        else:
            print(f"No data available for {ticker} on {date}")
        
        # Sleep to respect API rate limits
        time.sleep(13)  # ~5 requests per minute
    
    print(f"Completed processing {ticker}. Added {data_points_added} data points.")
    return data_points_added

async def main():
    """Main function to fetch and update stock data for the past month."""
    # List of tickers to fetch
    # tickers = ["AAPL", "MSFT", "GOOGL", "AMZN", "TSLA", "NVDA", "BRK.B", "FB", "JNJ", "V", "UNH", "JPM", "XOM", "PG", "MA", "HD", "CVX", "ABBV", "LLY", "AVGO", "COST", "MRK", "PEP", "KO", "MCD", "TMO", "DHR", "WMT", "ACN", "AMD", "LIN", "NFLX", "BMY", "PM", "TXN", "ADBE", "HON", "ORCL", "LOW", "INTC", "RTX", "UPS", "MS", "GS", "NOW", "SCHW", "CAT", "AMT", "BA", "IBM"]
    tickers = ["BRK.B", "FB", "JNJ", "V", "UNH", "JPM", "XOM", "PG", "MA", "HD", "CVX", "ABBV", "LLY", "AVGO", "COST", "MRK", "PEP", "KO", "MCD", "TMO", "DHR", "WMT", "ACN", "AMD", "LIN", "NFLX", "BMY", "PM", "TXN", "ADBE", "HON", "ORCL", "LOW", "INTC", "RTX", "UPS", "MS", "GS", "NOW", "SCHW", "CAT", "AMT", "BA", "IBM"]
    
    # Get list of trading days for the past month
    trading_days = get_trading_days_past_month()
    print(f"Found {len(trading_days)} trading days in the past month.")
    
    # Process each ticker one at a time
    total_data_points = 0
    for ticker in tickers:
        data_points = await process_ticker_for_month(ticker, trading_days)
        total_data_points += data_points
        
        # Add a pause between tickers
        print(f"Pausing before next ticker...")
        time.sleep(5)
    
    print(f"Monthly stock data update complete! Added {total_data_points} total data points.")

if __name__ == "__main__":
    asyncio.run(main())