package org.reallylastone.stock_collector.statistics;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class StatisticsGateway {
    private static final String CALL_CALCULATE_TRADE_STATISTICS =
            "CALL calculate_trade_statistics(?, ?)";
    private final DataSource dataSource;

    public void calculateTradeStatistics(long startMillis, long endMillis) {
        try (Connection connection = dataSource.getConnection();
                CallableStatement stmt = connection.prepareCall(CALL_CALCULATE_TRADE_STATISTICS)) {
            stmt.setLong(1, startMillis);
            stmt.setLong(2, endMillis);
            stmt.execute();
        } catch (SQLException e) {
            log.error("Error calculating trade statistics", e);
        }
    }
}
