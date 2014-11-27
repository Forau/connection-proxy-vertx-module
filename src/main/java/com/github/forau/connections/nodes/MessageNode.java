package com.github.forau.connections.nodes;

/**
A node in the tree/chain of message passing.
Since we always communicate from uplink, sending downlink messages just need to be sent to the node address.
This interface does not put restrictions on what kind of messages you can send or receive, the implementing classes will define that. 
*/
public interface MessageNode {
    /**
    The address this node is listening on.
    @return string for eventBus address
    */
    String getNodeAddress();
    
    /**
    Add a eventBus address to receive uplink messages. 
    @param uplinkAddress
    @param unicast if true, only one recipient will get the message, but can reply directly on it
    @return success
    */
    boolean addUplinkAddress(String uplinkAddress, boolean unicast);
    
    /**
    Remove listening eventBus address
    @param uplinkAddress
    @return 
    */
    boolean removeUplinkAddress(String uplinkAddress);
}
