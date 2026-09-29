package org.reallylastone.stock_collector.statistics;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reallylastone.leader_election_lib.LeadershipService;

@ExtendWith(MockitoExtension.class)
class CalculateStatisticsJobTest {

    @Mock
    private StatisticsGateway statisticsGateway;

    @Mock
    private LeadershipService leadershipService;

    @Test
    void doesNothingWhenNotLeader() {
        when(leadershipService.amILeader()).thenReturn(false);

        new CalculateStatisticsJob(statisticsGateway, leadershipService).run();

        verify(statisticsGateway, never()).calculateTradeStatistics(anyLong(), anyLong());
    }

    @Test
    void callsProcedureForCurrentAndPreviousMinuteWhenLeader() {
        when(leadershipService.amILeader()).thenReturn(true);

        new CalculateStatisticsJob(statisticsGateway, leadershipService).run();

        verify(statisticsGateway, times(2)).calculateTradeStatistics(anyLong(), anyLong());
    }
}
