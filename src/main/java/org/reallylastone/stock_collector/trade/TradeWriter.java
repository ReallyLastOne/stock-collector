package org.reallylastone.stock_collector.trade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class TradeWriter implements Runnable {
    private static final double THRESHOLD = 0.05;

    private final BlockingQueue<Trade> queue;
    private final boolean slowMode;
    private final TradeGateway tradeGateway;
    private final AtomicLong totalInserted = new AtomicLong(0);
    private long lastLogTime = System.currentTimeMillis();

    @Override
    public void run() {
        List<Trade> tradesToInsert = new ArrayList<>();
        queue.drainTo(tradesToInsert);

        if (slowMode) {
            Collections.shuffle(tradesToInsert);
            tradesToInsert = tradesToInsert.subList(0, (int) (tradesToInsert.size() * THRESHOLD));
        }

        if (!tradesToInsert.isEmpty()) {
            tradeGateway.insertTrades(tradesToInsert);
            totalInserted.addAndGet(tradesToInsert.size());
        }

        if (System.currentTimeMillis() - lastLogTime >= TimeUnit.HOURS.toMillis(1)) {
            log.info("Total inserted trades {}", totalInserted.get());
            lastLogTime = System.currentTimeMillis();
        }
    }

    public long getTotalInserted() {
        return totalInserted.get();
    }
}
