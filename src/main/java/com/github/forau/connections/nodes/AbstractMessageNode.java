package com.github.forau.connections.nodes;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

public abstract class AbstractMessageNode implements MessageNode {    
    private Map<String, Boolean> uplinkAddresses = new HashMap<>();
    
    // The address we will listen to
    private final String nodeAddress;
    
    // Reference to the eventBus, since we are very dependent on it.
    private final EventBus eventBus;
    
    private final Handler<Message> downlinkMessageHandler = new Handler<Message>() {
        @Override
        public void handle(Message e) {                
            handleDownlinkMessage(e);
        }
    };
    
    protected AbstractMessageNode(String address, EventBus eventBus) {
        this(address, eventBus, null);
    }
    
    protected AbstractMessageNode(String address, EventBus eventBus, Handler<AsyncResult<Void>> bindResultHandler) {
        this.nodeAddress = address;        
        this.eventBus = eventBus;                
        
        if(bindResultHandler != null) {
            eventBus.registerHandler(address, downlinkMessageHandler, bindResultHandler);
        } else {
            eventBus.registerHandler(address, downlinkMessageHandler);
        }
    }

    /**
    More verbose name then handle.
    @param e
    */
    protected abstract void handleDownlinkMessage(Message e);
    
    protected EventBus getEventBus() {
        return eventBus;
    }
        
    protected void broadcast(JsonObject message) {
        Iterator<Map.Entry<String, Boolean>> it = uplinkAddresses.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<String, Boolean> entry = it.next();
            if(entry.getValue()) {
                eventBus.send(entry.getKey(), message, downlinkMessageHandler);
            } else {
                eventBus.publish(entry.getKey(), message);
            }
        }        
    }

    protected void broadcast(byte [] message) {
        Iterator<Map.Entry<String, Boolean>> it = uplinkAddresses.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<String, Boolean> entry = it.next();
            if(entry.getValue()) {
                eventBus.send(entry.getKey(), message, downlinkMessageHandler);
            } else {
                eventBus.publish(entry.getKey(), message);
            }
        }        
    }
    
    protected void broadcastError(Throwable error) {
        // We probably want a message type to make this easier
        JsonObject errMsg = new JsonObject();
        errMsg.putString("TYPE", "ERROR");
        errMsg.putString("MESSAGE",  error.getMessage());
        broadcast(errMsg);
    }
        
    @Override
    public String getNodeAddress() {
        return nodeAddress;
    }

    @Override
    public boolean addUplinkAddress(final String uplinkAddress, boolean unicast) {                
        uplinkAddresses.put(uplinkAddress, unicast);        
        return true;
    }

    @Override
    public boolean removeUplinkAddress(final String uplinkAddress) {
        return uplinkAddresses.remove(uplinkAddress);
    }
}
