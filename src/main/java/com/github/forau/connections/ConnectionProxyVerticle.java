package com.github.forau.connections;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

public class ConnectionProxyVerticle extends Verticle {
    public final String EVENT_BUS_ADDRESS = "connection-proxy-address";
    private final Logger logger;
    
    /**
    Constructor, since we are using java, constructors are never ignored.(Or the opposite)
    */
    public ConnectionProxyVerticle() {
        super();
        logger = container.logger();
    }
    
    @Override
    public void start() {    
    final String address = container.config().getString(EVENT_BUS_ADDRESS);
    vertx.eventBus().registerHandler(address, new Handler<Message<String>>() {
      @Override
      public void handle(Message<String> message) {
        message.reply("pong!");
        logger.info("Sent back pong");
      }
    });

    logger.info(String.format("%s started", getClass().getSimpleName()));
  }
}
