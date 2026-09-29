package org.reallylastone.stock_collector.finnhub;

import java.net.URI;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.reallylastone.stock_collector.trade.Trade;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FinnhubWebSocketClient extends WebSocketClient {
    private final ObjectMapper mapper = new ObjectMapper();
    private final List<String> stocks;
    private final BlockingQueue<Trade> messageQueue;

    public FinnhubWebSocketClient(URI serverUri, List<String> stocks,
            BlockingQueue<Trade> messageQueue) {
        super(serverUri);
        this.stocks = stocks;
        this.messageQueue = messageQueue;
        setConnectionLostTimeout(-1);
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        log.info("Connected to Finnhub server");
        stocks.forEach(stock -> send("{\"type\":\"subscribe\",\"symbol\":\"" + stock + "\"}"));
        log.info("Subscribed to {} stocks", stocks);
    }

    @Override
    public void onMessage(String message) {
        try {
            FinnhubMessageEvent event = mapper.readValue(message, FinnhubMessageEvent.class);
            if ("ping".equals(event.type())) {
                send("{\"type\":\"pong\"}");
                return;
            }
            for (Trade trade : event.data()) {
                messageQueue.put(trade);
            }
        } catch (Exception e) {
            log.error("Error onMessage", e);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("Connection to Finnhub server closed for reason {}, code {}, remote {}", reason,
                code, remote);
    }

    @Override
    public void onError(Exception ex) {
        log.error("Error in Finnhub websocket connection", ex);
    }
}
