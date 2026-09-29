package org.reallylastone.stock_collector;

import java.net.URI;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.reallylastone.leader_election_lib.AcquireLeadershipJob;
import org.reallylastone.leader_election_lib.LeadershipServiceImpl;
import org.reallylastone.stock_collector.common.ConnectionPool;
import org.reallylastone.stock_collector.common.SimpleDataSource;
import org.reallylastone.stock_collector.finnhub.FinnhubReconnectJob;
import org.reallylastone.stock_collector.finnhub.FinnhubWebSocketClient;
import org.reallylastone.stock_collector.statistics.CalculateStatisticsJob;
import org.reallylastone.stock_collector.statistics.StatisticsGateway;
import org.reallylastone.stock_collector.trade.TradeGateway;
import org.reallylastone.stock_collector.trade.TradeWriter;
import org.reallylastone.stock_collector.trade.TradeWriterJob;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
    private static final String FINNHUB_URL = "wss://ws.finnhub.io?token=";
    private static final String DEFAULT_DB_URL = "jdbc:postgresql://localhost:5433/stock_collector";
    private static final int LEADER_APP_ID = 1;
    private static final long LEADER_TIMEOUT_MS = 30_000;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    static void main(String[] args) throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        String url = System.getenv().getOrDefault("STOCK_COLLECTOR_DB_URL", DEFAULT_DB_URL)
                + "?currentSchema=stock_collector";
        String username =
                System.getenv().getOrDefault("STOCK_COLLECTOR_DB_USER", "stock_collector");
        String password =
                System.getenv().getOrDefault("STOCK_COLLECTOR_DB_PASSWORD", "stock_collector");
        String finnhubToken = System.getenv("FINNHUB_TOKEN");
        String stocksEnv = System.getenv("STOCK_COLLECTOR_STOCKS");

        Objects.requireNonNull(finnhubToken, "FINNHUB_TOKEN is required");
        Objects.requireNonNull(stocksEnv, "STOCK_COLLECTOR_STOCKS is required");

        log.info("Connecting to {} with user {}", url, username);

        List<String> stocks = Arrays.stream(stocksEnv.split(",")).toList();

        initializeRegistry(url, username, password, finnhubToken, stocks);

        log.info("Process ID {}", ProcessHandle.current().pid());

        Registry.getFinnhubWebSocketClient().connect();
        scheduler.scheduleAtFixedRate(new AcquireLeadershipJob(Registry.getLeadershipService()), 0,
                15, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(new CalculateStatisticsJob(Registry.getStatisticsGateway(),
                Registry.getLeadershipService()), 0, 1, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(new FinnhubReconnectJob(Registry.getFinnhubWebSocketClient()),
                0, 1, TimeUnit.SECONDS);
        scheduler.scheduleWithFixedDelay(new TradeWriterJob(
                new TradeWriter(Registry.getQueue(), false, Registry.getTradeGateway()),
                Registry.getLeadershipService()), 0, 5, TimeUnit.SECONDS);

        Thread.currentThread().join();
    }

    private static void initializeRegistry(String url, String username, String password,
            String finnhubToken, List<String> stocks) throws SQLException, InterruptedException {
        Registry.setConnectionPool(new ConnectionPool(url, username, password, 1));
        Registry.setLeadershipService(new LeadershipServiceImpl(
                new SimpleDataSource(url, username, password), LEADER_APP_ID, LEADER_TIMEOUT_MS));
        Registry.setTradeGateway(new TradeGateway(Registry.getConnectionPool().getConnection()));
        Registry.setStatisticsGateway(
                new StatisticsGateway(new SimpleDataSource(url, username, password)));
        Registry.setQueue(new ArrayBlockingQueue<>(10_000_000));
        Registry.setFinnhubWebSocketClient(new FinnhubWebSocketClient(
                URI.create(FINNHUB_URL + finnhubToken), stocks, Registry.getQueue()));
    }
}
