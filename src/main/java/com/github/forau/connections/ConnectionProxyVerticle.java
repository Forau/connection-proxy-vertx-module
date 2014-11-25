package com.github.forau.connections;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

public class ConnectionProxyVerticle extends Verticle {

    public static final String EVENT_BUS_ADDRESS = "connection-proxy-address";

    /**
     Constructor, since we are using java, constructors are never ignored.(Or the opposite)
     */
    public ConnectionProxyVerticle() {
        super();
    }

    @Override
    public void start() {
        final String address = container.config().getString(EVENT_BUS_ADDRESS);
        vertx.eventBus().registerHandler(address, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> message) {
                container.logger().info("Got message " + message.body().toMap());
                MessageCommand command = new MessageCommand(message.body());
                switch(command.getAction()) {
                case PING:                    
                    message.reply(command.getMessage());
                    container.logger().info("Sent back '"+command.getMessage()+"' for message " + command.toString());
                    break;
                case UNDEFINED:
                default :
                    container.logger().warn("Did not match any action, will do nothing: " + command.toString());
                }                
            }
        });

        container.logger().info(String.format("%s started", getClass().getSimpleName()));
    }
}
