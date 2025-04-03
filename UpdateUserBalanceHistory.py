import time
import math
import requests
import asyncio
from datetime import datetime, timedelta
from supabase import create_client, Client

# Supabase API Connection
SUPABASE_URL = "https://lnfecoxuwybrlhzjqxkb.supabase.co"
SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImxuZmVjb3h1d3licmxoempxeGtiIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Mzg5NDQwMTMsImV4cCI6MjA1NDUyMDAxM30.wCjoCRqMTLOWyPxX-9lMohKbxESbP8z6G0FM2Gk3GLY"

# Create Supabase client
supabase: Client = create_client(SUPABASE_URL, SUPABASE_KEY)

def java_string_hashcode(s: str) -> int:
    """Replicates Java's String.hashCode() implementation."""
    h = 0
    for ch in s:
        h = (31 * h + ord(ch)) & 0xFFFFFFFF
    # Convert to signed 32-bit integer
    if h > 0x7FFFFFFF:
        h = h - 0x100000000
    return h

def simulate_stock_price(open_price: float, close_price: float, ticker: str) -> float:
    # Note: a user can change their system time, which might not be ideal.
    current_time_millis = int(time.time() * 1000)

    # Calculate the mid-price and an amplitude for oscillations.
    mid_price = (open_price + close_price) / 2.0
    amplitude = ((close_price - open_price) / 2.0) * 0.9

    # Convert time to minutes for smoother change.
    minutes = current_time_millis / 60000.0

    # Use the custom Java-like hash code for consistency.
    ticker_seed = float(java_string_hashcode(ticker))
    angle = minutes + (ticker_seed % 360)

    # Use sine for smooth oscillation.
    oscillation = math.sin(angle)

    # Add a small lower-frequency bump.
    bump = 0.02 * mid_price * math.sin(angle / 10)

    # Calculate and return the simulated price.
    return mid_price + (oscillation * amplitude) + bump

async def get_user_league_balances():
    """Retrieve all user portfolio balances from the database."""
    response = supabase.table("user_league").select("*").execute()
    return response.data

async def get_historical_stock_price():
    """Retrieve all user portfolio balances from the database."""
    response = supabase.table("historical_stock_price").select("*").execute()
    return response.data

async def get_user_portfolio():
    """Retrieve all user portfolio balances from the database."""
    response = supabase.table("portfolio").select("*").execute()
    return response.data

# global time
current_timestamp = datetime.now().isoformat()

async def insert_historical_portfolio_balance(uid, league_id, value):
    """Insert a single stock price data point into the database."""
    # Insert historical stock price with the specific date
    price_response = supabase.table("historical_portfolio_value").insert({
        "uid": uid,
        "league_id": league_id,
        "value": value,
        "timestamp": current_timestamp
    }).execute()

    print(f"Inserted historical price for {uid}")

if __name__ == '__main__':
    user_stocks_data = asyncio.run(get_user_portfolio())
    historical_prices = asyncio.run(get_historical_stock_price())
    user_balances = asyncio.run(get_user_league_balances())

    # Create a map of (user_id, league_id) -> array of stocks
    user_stock_map = {}
    for stock_data in user_stocks_data:
        uid = stock_data['uid']
        league_id = stock_data['league_id']
        key = (uid, league_id)  # Composite key using tuple
        if key not in user_stock_map:
            user_stock_map[key] = []
        user_stock_map[key].append({
            'stock_id': stock_data['stock_id'],
            'quantity': stock_data['quantity']
            # league_id removed from here since it's now part of the key
        })

    # Get the latest price for each stock
    latest_prices = {}
    for price in historical_prices:
        stock_id = price['stock_id']
        if stock_id not in latest_prices or price['timestamp'] > latest_prices[stock_id]['timestamp']:
            latest_prices[stock_id] = price

    # Calculate total portfolio value for each user
    for user in user_balances:
        uid = user['uid']
        total_value = user['cash']  # Start with cash balance
        league_id = user['league_id']

        if (uid, league_id) in user_stock_map:
            for stock in user_stock_map[(uid, league_id)]:
                stock_id = stock['stock_id']
                if stock_id in latest_prices:
                    current_price = simulate_stock_price(
                        latest_prices[stock_id]['open'],
                        latest_prices[stock_id]['close'],
                        str(stock_id)  # Using stock_id as ticker for simulation
                    )
                    stock_value = current_price * stock['quantity']
                    total_value += stock_value

        print(f"User {uid} league {league_id} total portfolio value: ${total_value:.2f}")
        asyncio.run(insert_historical_portfolio_balance(uid, user['league_id'], total_value))
