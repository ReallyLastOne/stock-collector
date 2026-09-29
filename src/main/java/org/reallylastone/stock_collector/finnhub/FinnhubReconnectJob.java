package org.reallylastone.stock_collector.finnhub;

import java.time.ZonedDateTime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class FinnhubReconnectJob implements Runnable {
    private static final int RECONNECT_ATTEMPT_INTERVAL_SECONDS = 20;
    private final FinnhubWebSocketClient client;
    private ZonedDateTime lastAttempt;

    @Override
    public void run() {
        if (client.isClosed() && (lastAttempt == null || lastAttempt
                .isBefore(ZonedDateTime.now().minusSeconds(RECONNECT_ATTEMPT_INTERVAL_SECONDS)))) {
            log.info("Finnhub client is closed, reconnecting");
            try {
                lastAttempt = ZonedDateTime.now();
                client.reconnectBlocking();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Error when trying to reconnect to a Finnhub server", e);
            }
        }
    }
}
