package com.github.forau.connections;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.net.NetSocket;
import org.vertx.java.platform.Verticle;

public class ConnectionHandler {
    public static final String EVENT_PARAMETER = "event";
    public static final String DATA_PARAMETER = "data";
    
    public enum EVENT { CONNECT, DISCONNECT, DATA };
    
    private final NetSocket socket;
    private final Verticle owner;    
    private final String uplinkAddress;
    
    public ConnectionHandler(final Verticle owner, final NetSocket socket, final String downlinkAddress, final String uplinkAddress) {
        super();
        this.owner = owner;
        this.uplinkAddress = uplinkAddress;
        this.socket = socket;        
        this.socket.closeHandler(new Handler<Void>() {
            @Override
            public void handle(Void e) {
                owner.getContainer().logger().warn("Socket close");
                onClose();
            }
        });
        this.socket.endHandler(new Handler<Void>() {
            @Override
            public void handle(Void e) {
                owner.getContainer().logger().warn("Socket end");
                onClose();
            }
        });
        this.socket.exceptionHandler(new Handler<Throwable>() {
            @Override
            public void handle(Throwable e) {
                owner.getContainer().logger().error("Socket error", e);                
            }
        });
        this.socket.dataHandler(new Handler<Buffer>() {
            @Override
            public void handle(Buffer e) {
                onData(e);
            }
        });
        
        owner.getVertx().eventBus().registerHandler(downlinkAddress, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> e) {
                onDownlinkEvent(e);
            }
        });
    }
    
    private void onClose() {
        sendUplinkEvent(EVENT.DISCONNECT, "close");
    }
    
    public void onData(Buffer e) {
        sendUplinkEvent(EVENT.DATA, new String(e.getBytes()));
    }

    private void onDownlinkEvent(Message<JsonObject> msg) {
        MessageCommand command = new MessageCommand(msg.body());
        switch(command.getAction()) {
        case SEND:
             owner.getContainer().logger().debug("Sending downlink: "+command.getMessage());
             owner.getVertx().eventBus().send(socket.writeHandlerID(), command.getMessage());
             break;
        case CONNECT:
        case BIND:
                owner.getContainer().logger().debug("Already connected and bound, should be caught earlier");
            break;
        default:
            owner.getContainer().logger().warn("Message "+msg+" did not contain an valid downlink action");
        }
    }
    
    private void sendUplinkEvent(EVENT eventsType, String data) {
        JsonObject msg = new JsonObject();
        msg.putString(EVENT_PARAMETER, eventsType.name());
        msg.putString(DATA_PARAMETER, data);
        owner.getVertx().eventBus().publish(uplinkAddress, msg);
    }

}
