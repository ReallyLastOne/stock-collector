package org.reallylastone.stock_collector.finnhub;

import java.util.List;

import org.reallylastone.stock_collector.trade.Trade;

public record FinnhubMessageEvent(List<Trade> data, String type) {
}
