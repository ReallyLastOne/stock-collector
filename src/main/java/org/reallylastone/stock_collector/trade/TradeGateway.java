package org.reallylastone.stock_collector.trade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class TradeGateway {
    private static final String INSERT_SQL =
            "INSERT INTO finnhub_trades (c, p, s, t, v) VALUES (?, ?, ?, ?, ?)";
    private final Connection connection;

    public void insertTrades(List<Trade> trades) {
        try {
            connection.setAutoCommit(false);

            try (PreparedStatement stmt = connection.prepareStatement(INSERT_SQL)) {
                for (Trade trade : trades) {
                    stmt.setString(1, String.valueOf(trade.c()));
                    stmt.setBigDecimal(2, trade.p());
                    stmt.setString(3, trade.s());
                    stmt.setLong(4, trade.t());
                    stmt.setBigDecimal(5, trade.v());
                    stmt.addBatch();
                }

                stmt.executeBatch();
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                log.error("Error on committing transaction insertTrades", e);
            }
        } catch (SQLException e) {
            log.error("Error on transaction rollback", e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                log.error("Error on setting auto commit");
            }
        }
    }
}
