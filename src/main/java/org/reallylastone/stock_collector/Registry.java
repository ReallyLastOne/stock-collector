package org.reallylastone.stock_collector;

import java.util.concurrent.BlockingQueue;

import org.reallylastone.leader_election_lib.LeadershipService;
import org.reallylastone.stock_collector.common.ConnectionPool;
import org.reallylastone.stock_collector.finnhub.FinnhubWebSocketClient;
import org.reallylastone.stock_collector.statistics.StatisticsGateway;
import org.reallylastone.stock_collector.trade.Trade;
import org.reallylastone.stock_collector.trade.TradeGateway;

import lombok.Getter;
import lombok.Setter;

public class Registry {
    @Getter
    @Setter
    private static ConnectionPool connectionPool;

    @Getter
    @Setter
    private static LeadershipService leadershipService;

    @Getter
    @Setter
    private static TradeGateway tradeGateway;

    @Getter
    @Setter
    private static StatisticsGateway statisticsGateway;

    @Getter
    @Setter
    private static FinnhubWebSocketClient finnhubWebSocketClient;

    @Getter
    @Setter
    private static BlockingQueue<Trade> queue;
}
