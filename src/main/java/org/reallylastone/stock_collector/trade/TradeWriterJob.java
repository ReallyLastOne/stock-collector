package org.reallylastone.stock_collector.trade;

import org.reallylastone.leader_election_lib.LeadershipService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TradeWriterJob implements Runnable {
    private final TradeWriter writer;
    private final LeadershipService leadershipService;

    @Override
    public void run() {
        if (leadershipService.amILeader()) {
            writer.run();
        }
    }
}
