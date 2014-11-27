package com.github.forau.connections;

import com.github.forau.connections.nodes.TcpClientEndpointNode;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.net.NetClient;
import org.vertx.java.platform.Verticle;

public class ConnectionProxyVerticle extends Verticle {

    public static final String EVENT_BUS_ADDRESS = "connection-proxy-address";

    @Override
    public void start() {
        final String address = container.config().getString(EVENT_BUS_ADDRESS);
        vertx.eventBus().registerHandler(address, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(final Message<JsonObject> message) {
                container.logger().info("Got message " + message.body().toMap());
                MessageCommand command = new MessageCommand(message.body());
                switch(command.getAction()) {
                case PING:                    
                    message.reply(command.getMessage());
                    container.logger().info("Sent back '"+command.getMessage()+"' for message " + command.toString());
                    break;
                
                case CONNECT:
                    String reply = connectCommand(command);
                case BIND:
                    String host = command.getRawMessage().getString("host");
                    int port = command.getRawMessage().getInteger("port");
                    String downlinkAddress = command.getRawMessage().getString("downlinkAddress");
                    String uplinkAddress = command.getRawMessage().getString("uplinkAddress");
                    Handler<AsyncResult<Void>> resultHandler = new Handler<AsyncResult<Void>>() {                        
                        @Override
                        public void handle(AsyncResult<Void> event) {
                            String success = event.succeeded()?"OK":event.cause().getMessage();
                            message.reply(success);
                        }
                    };
                    initializeTcpConnection(host, port, downlinkAddress, uplinkAddress, resultHandler);
                    break;
                case SEND:
                    
                case UNDEFINED:
                default :
                    container.logger().warn("Did not match any action, will do nothing: " + command.toString());
                }                
            }
        });
        container.logger().info(String.format("%s started", getClass().getSimpleName()));
    }
    
    private String connectCommand(MessageCommand command) {
        final String channelId = command.getUid();
        
        
        return null;
    }
    
    private void initializeTcpConnection(String host, int port, final String downlinkAddress, final String uplinkAddress, final Handler<AsyncResult<Void>> resultHandler) {        
        NetClient client = vertx.createNetClient();
        client.setReconnectAttempts(10);
        client.setReconnectInterval(500);
        final TcpClientEndpointNode tcpNode = new TcpClientEndpointNode(downlinkAddress, this, resultHandler);
        
        container.logger().info("Adding unicast uplink address: "+uplinkAddress);
        tcpNode.addUplinkAddress(uplinkAddress, true);
        
        client.connect(port, host, tcpNode);       
    }   
}
