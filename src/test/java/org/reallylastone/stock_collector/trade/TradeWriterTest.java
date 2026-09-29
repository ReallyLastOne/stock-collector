package org.reallylastone.stock_collector.trade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TradeWriterTest {

    @Mock
    private TradeGateway tradeGateway;

    private BlockingQueue<Trade> queue;
    private TradeWriter writer;

    @BeforeEach
    void setup() {
        queue = new ArrayBlockingQueue<>(50);
        writer = new TradeWriter(queue, false, tradeGateway);
    }

    @Test
    void multipleTradesSingleRun() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            queue.put(new Trade("c" + i, BigDecimal.valueOf(10 + i), "AAPL",
                    System.currentTimeMillis() / 1000, BigDecimal.ONE));
        }

        writer.run();

        ArgumentCaptor<List<Trade>> captor = ArgumentCaptor.forClass(List.class);
        verify(tradeGateway, atLeastOnce()).insertTrades(captor.capture());
        assertEquals(10, captor.getValue().size());
        assertEquals(10, writer.getTotalInserted());
    }

    @Test
    void multipleSymbols() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            queue.put(new Trade("c" + i, BigDecimal.TEN, "AAPL", System.currentTimeMillis() / 1000,
                    BigDecimal.ONE));
        }
        for (int i = 0; i < 5; i++) {
            queue.put(new Trade("c" + i, BigDecimal.TEN, "GOOG", System.currentTimeMillis() / 1000,
                    BigDecimal.ONE));
        }

        writer.run();

        ArgumentCaptor<List<Trade>> captor = ArgumentCaptor.forClass(List.class);
        verify(tradeGateway, atLeastOnce()).insertTrades(captor.capture());
        assertEquals(10, captor.getValue().size());
        assertEquals(10, writer.getTotalInserted());
    }

    @Test
    void concurrentAccess() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 20; i++) {
            queue.put(new Trade("c" + i, BigDecimal.ONE, "AAPL", System.currentTimeMillis() / 1000,
                    BigDecimal.ONE));
        }

        executor.submit(() -> writer.run());
        executor.shutdown();
        while (!executor.isTerminated()) {
            Thread.sleep(50);
        }

        assertEquals(20, writer.getTotalInserted());
    }
}
