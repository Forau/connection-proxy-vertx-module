package com.github.forau.connections.test.integration;

import com.github.forau.connections.ConnectionProxyVerticle;
import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;
import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.assertNotNull;
import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;

public class ConnectionProxyTest extends TestVerticle {
    private JsonObject config;
    
    @Override
    public void start() {
        config = new JsonObject();
        config.putString(ConnectionProxyVerticle.EVENT_BUS_ADDRESS, "testAddress");
        
        initialize();
        container.deployModule(System.getProperty("vertx.modulename"), config, new AsyncResultHandler<String>() {
            @Override
            public void handle(AsyncResult<String> asyncResult) {
                if (asyncResult.failed()) {
                    container.logger().error(asyncResult.cause(), asyncResult.cause());
                }
                assertTrue(asyncResult.succeeded());
                assertNotNull("deploymentID should not be null", asyncResult.result());
                startTests();
            }
        });
    }
    
    @Test
    public void testPing() {
        container.logger().info("in testPing()");
        JsonObject msg = new JsonObject();
        msg.putString("ACTION","PING");
        msg.putString("MESSAGE", "pong!");
        
        vertx.eventBus().send("testAddress", msg, new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> reply) {
                assertEquals("pong!", reply.body());                
                testComplete();
            }
        });
    }
    
}
