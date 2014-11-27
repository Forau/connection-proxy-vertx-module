package com.github.forau.connections.nodes;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.net.NetSocket;
import org.vertx.java.platform.Verticle;

public class TcpClientEndpointNode extends AbstractMessageNode implements AsyncResultHandler<NetSocket>, AsyncResult {
    private NetSocket socket = null;
    private final Verticle owner;
    private Handler<AsyncResult<Void>> finalResultHandler;
    
    private Throwable cause = null;
    
    public TcpClientEndpointNode(final String address, final Verticle owner, final Handler<AsyncResult<Void>> bindResultHandler) {
        super(address, owner.getVertx().eventBus(), new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> event) {
                // TODO
            }
        });
        this.finalResultHandler = bindResultHandler;
        this.owner = owner;
    }
    
    @Override
    public void handle(AsyncResult<NetSocket> asyncResult) {        
        if (asyncResult.succeeded()) {
            socket = asyncResult.result();
            owner.getContainer().logger().info("We have connected! Socket is " + socket);
            socket.dataHandler(new Handler<Buffer>() {
                @Override
                public void handle(Buffer event) {
                    broadcast(event.getBytes());
                }
            });
            
            socket.closeHandler(new Handler<Void>() {

                @Override
                public void handle(Void event) {
                    broadcastError(new RuntimeException("Socket closed"));
                }
            });
            
        } else {
            cause = asyncResult.cause();
            owner.getContainer().logger().error("Unable to connect", cause);
            broadcastError(cause);
        }
        
        if(finalResultHandler!=null) {
            finalResultHandler.handle(this);
        }
    }

    
    @Override
    protected void handleDownlinkMessage(Message e) {
        owner.getContainer().logger().info("Sending downlink messages of type "+e.body().getClass().getSimpleName()+" to "+socket);
        if(e.body() instanceof Buffer) {
            socket.write((Buffer)e.body());
        } else if(e.body() instanceof String) {
            socket.write((String)e.body());
        } else {
            owner.getContainer().logger().warn("Sending downlink messages of type "+e.body().getClass().getSimpleName()+" is not yet implemented");
        }
    }

    @Override
    public Object result() {
        return this;
    }

    @Override
    public Throwable cause() {
        return cause;
    }

    @Override
    public boolean succeeded() {
        return socket != null;
    }

    @Override
    public boolean failed() {
        return !succeeded();
    }

}
