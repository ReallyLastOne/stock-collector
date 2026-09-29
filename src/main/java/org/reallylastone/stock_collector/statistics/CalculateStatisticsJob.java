package org.reallylastone.stock_collector.statistics;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.reallylastone.leader_election_lib.LeadershipService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CalculateStatisticsJob implements Runnable {

    private final StatisticsGateway statisticsGateway;
    private final LeadershipService leadershipService;

    @Override
    public void run() {
        if (!leadershipService.amILeader()) {
            return;
        }

        ZonedDateTime start = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        calculate(start, start.plusMinutes(1));
        calculate(start.minusMinutes(1), start);
    }

    private void calculate(ZonedDateTime start, ZonedDateTime end) {
        statisticsGateway.calculateTradeStatistics(start.toInstant().toEpochMilli(),
                end.toInstant().toEpochMilli());
    }
}
