package com.example.fantasystocks.ui.news

import com.example.fantasystocks.R

data class NewsArticleContent(
    val primaryKey: Int,
    val title: String,
    val description: String,
    val imageRes: Int,
    val content: String // XML content to be parsed later
)

val newsArticleContents = listOf(
    NewsArticleContent(
        1,
        "Investment Strategies 101",
        "Beginner-friendly guide to investing",
        R.drawable.investing_basics,
        """
        <article>
            <ArticleImage>investing_basics</ArticleImage>
            <text>Investing is a long-term strategy used to grow wealth by allocating funds into assets such as stocks, bonds, and real estate. It provides individuals with an opportunity to earn returns that outpace inflation, making it a critical component of financial planning. Whether for retirement, homeownership, or wealth preservation, investing allows people to put their money to work over time.</text>

            <quiz question="What is the primary goal of investing?" correctAnswer="Wealth" />

            <text>Diversification is one of the most important principles in investing. By spreading investments across different assets, investors can reduce risk and protect their portfolios from market volatility. For example, investing in both stocks and bonds can balance potential gains and losses, ensuring more stable returns over time.</text>

            <ArticleImage>diversification_chart</ArticleImage>

            <truefalse question="Diversification increases risk." correctAnswer="false" />

            <text>Another key aspect of investing is understanding economic indicators such as interest rates, inflation, and corporate earnings. These factors influence stock market movements and help investors make informed decisions. Additionally, patience and long-term thinking are crucial. Markets fluctuate in the short term, but historical trends show that long-term investments typically yield positive returns.</text>

            <ArticleImage>market_trends_graph</ArticleImage>

            <multiplechoice question="Which principle reduces investment risk?" correctAnswer="Diversification">
                <option>Speculation</option>
                <option>Diversification</option>
                <option>Timing</option>
                <option>Short-selling</option>
            </multiplechoice>
        </article>
        """.trimIndent()
    ),
    NewsArticleContent(
        2,
        "Virtual Stock Market",
        "Compete with friends in simulated trading",
        R.drawable.virtual_trading,
        """
        <article>
            <ArticleImage>virtual_trading</ArticleImage>
            <text>Virtual stock markets provide an excellent way for individuals to practice trading without financial risk. These platforms use real-time stock market data to simulate trading, allowing participants to buy and sell stocks, track price movements, and develop investment strategies. They serve as valuable tools for beginners to learn market mechanics and for experienced traders to test strategies.</text>

            <quiz question="What do virtual stock markets simulate?" correctAnswer="Trading" />

            <text>These simulations support various strategies such as momentum trading, value investing, and day trading. Some platforms also offer competitions where users can challenge friends or compete globally, making learning more engaging and rewarding.</text>

            <ArticleImage>trading_strategies</ArticleImage>

            <truefalse question="Virtual stock markets use real money." correctAnswer="false" />

            <text>By engaging with virtual trading, investors can improve their decision-making, gain confidence, and prepare for real-market investing without the fear of losing money.</text>

            <multiplechoice question="Which strategy focuses on quick trades?" correctAnswer="DayTrading">
                <option>Indexing</option>
                <option>DayTrading</option>
                <option>LongHolding</option>
                <option>DividendInvesting</option>
            </multiplechoice>
        </article>
        """.trimIndent()
    ),
    NewsArticleContent(
        3,
        "Market Trends 2024",
        "Latest updates on stock market trends",
        R.drawable.market_trends,
        """
        <article>
            <ArticleImage>market_trends</ArticleImage>
            <text>The stock market in 2024 is being driven by advancements in artificial intelligence, the expansion of renewable energy, and the growth of healthcare innovations. These sectors are attracting significant investments due to their potential for long-term profitability and societal impact.</text>

            <quiz question="Which sector is leading in 2024?" correctAnswer="AI" />

            <text>Interest rates play a crucial role in market performance. Central banks adjust interest rates to control inflation, which in turn affects stock prices, bond yields, and investor sentiment. Additionally, global supply chain issues and geopolitical events continue to shape investor confidence and market trends.</text>

            <ArticleImage>interest_rates_graph</ArticleImage>

            <truefalse question="Interest rates impact stock markets." correctAnswer="true" />

            <text>Investors must stay informed about economic conditions and industry trends to make educated investment decisions. Understanding these factors allows for better risk management and portfolio adjustments in response to changing market conditions.</text>

            <multiplechoice question="Which factor influences stock trends?" correctAnswer="Rates">
                <option>Astrology</option>
                <option>Rates</option>
                <option>Celebrity</option>
                <option>Weather</option>
            </multiplechoice>
        </article>
        """.trimIndent()
    ),
    NewsArticleContent(
        4,
        "Crypto Insights",
        "Deep dive into the cryptocurrency world",
        R.drawable.crypto_insights,
        """
        <article>
            <ArticleImage>crypto_insights</ArticleImage>
            <text>Cryptocurrency is a revolutionary digital asset class that operates on blockchain technology. Unlike traditional currencies, cryptocurrencies are decentralized, meaning they are not controlled by any government or financial institution. Bitcoin, Ethereum, and other cryptocurrencies offer a new way to conduct transactions and store value.</text>

            <quiz question="Which technology powers cryptocurrency?" correctAnswer="Blockchain" />

            <text>The crypto market is highly volatile, with prices fluctuating due to supply, demand, regulatory changes, and investor sentiment. While some investors see cryptocurrencies as a way to achieve high returns, others recognize the risks associated with market instability.</text>

            <ArticleImage>crypto_volatility_chart</ArticleImage>

            <truefalse question="Cryptocurrency prices are stable." correctAnswer="false" />

            <text>Security is a major concern in the crypto industry. To protect their assets, investors are encouraged to store their holdings in hardware wallets, which provide better protection against hacks and cyber threats.</text>

            <multiplechoice question="Where is crypto safest?" correctAnswer="Hardware">
                <option>Exchange</option>
                <option>Paper</option>
                <option>Hardware</option>
                <option>Cloud</option>
            </multiplechoice>
        </article>
        """.trimIndent()
    ),
    NewsArticleContent(
        5,
        "Tech Stocks to Watch",
        "Emerging tech stocks with potential",
        R.drawable.tech_stocks,
        """
        <article>
            <ArticleImage>tech_stocks</ArticleImage>
            <text>The technology sector continues to drive innovation, with artificial intelligence, semiconductor advancements, and cloud computing leading the way. Companies such as NVIDIA, AMD, and Microsoft are at the forefront, pushing the boundaries of what technology can achieve.</text>

            <quiz question="Which sector is expanding rapidly?" correctAnswer="AI" />

            <text>Quantum computing is another emerging field with high potential. Companies investing in quantum research aim to revolutionize computing power, which could transform industries like pharmaceuticals and finance. Despite its infancy, quantum computing remains a key area to watch.</text>

            <ArticleImage>quantum_computing_lab</ArticleImage>

            <truefalse question="Quantum computing is mainstream." correctAnswer="false" />

            <text>Additionally, investors are keeping an eye on autonomous vehicle technology, green energy solutions, and cybersecurity firms, all of which are poised for substantial growth in the coming years.</text>

            <multiplechoice question="Which tech area is growing?" correctAnswer="Semiconductors">
                <option>Social</option>
                <option>Semiconductors</option>
                <option>Print</option>
                <option>Fax</option>
            </multiplechoice>
        </article>
        """.trimIndent()
    )
)
