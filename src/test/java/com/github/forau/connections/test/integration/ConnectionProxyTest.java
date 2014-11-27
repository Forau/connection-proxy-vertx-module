package com.github.forau.connections.test.integration;

import com.github.forau.connections.ConnectionProxyVerticle;
import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.net.NetSocket;
import org.vertx.java.core.streams.Pump;
import org.vertx.testtools.TestVerticle;
import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.assertNotNull;
import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;

public class ConnectionProxyTest extends TestVerticle {
    private JsonObject config;
    private final int TEST_PORT = 60123;
    private final String TEST_UPLINK_ADDRESS = "testUplink";
    private final String TEST_DOWNLINK_ADDRESS = "testDownlink";
    
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
        msg.putString("MESSAGE", "This is the string to echo");
        
        vertx.eventBus().send("testAddress", msg, new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> reply) {
                assertEquals("This is the string to echo", reply.body());                
                testComplete();
            }
        });
    }
    
    @Test
    public void testTcpClient() {
        container.logger().info("in testTcpClient()");
        
        // Echo server for testing
        vertx.createNetServer().connectHandler(new Handler<NetSocket>() {
            @Override
            public void handle(NetSocket socket) {
                Pump.createPump(socket, socket).start();
            }
        }).listen(TEST_PORT);
        
        
        
        JsonObject msg = new JsonObject();
        msg.putString("ACTION","BIND");
        msg.putString("MESSAGE", "Not used now");
        
        msg.putString("downlinkAddress", TEST_DOWNLINK_ADDRESS);
        msg.putString("uplinkAddress", TEST_UPLINK_ADDRESS);
        msg.putString("host", "localhost");
        msg.putNumber("port", TEST_PORT);
                
        vertx.eventBus().send("testAddress", msg, new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> reply) {
                assertEquals("OK", reply.body());
                
                vertx.eventBus().registerHandler(TEST_UPLINK_ADDRESS, new Handler<Message>() {
                    @Override
                    public void handle(Message event) {
                        // The return-data should be better speced
                        assertEquals("TestMessage sent and echoed back", new String((byte[])event.body()));
                        container.logger().info("Got expected result from "+event);
                        testComplete();
                    }
                }, new Handler<AsyncResult<Void>>() {
                    @Override
                    public void handle(AsyncResult<Void> event) {
                        assertTrue(event.succeeded());
                        vertx.eventBus().publish(TEST_DOWNLINK_ADDRESS, "TestMessage sent and echoed back");
                    }
                });                                
            }
        });
    }
    
    
    
}
