package com.github.forau.connections;

import org.vertx.java.core.json.JsonObject;

class MessageCommand {
    public static final String ACTION_PARAMETER = "ACTION";
    public static final String UID_PARAMETER = "UID";
    public static final String MESSAGE_PARAMETER = "MESSAGE";
    
    public enum ACTION {BIND, CONNECT, SEND, PING, UNDEFINED};
    
    private final ACTION action;
    private final String uid;
    private final String message;
    
    public MessageCommand(JsonObject jsonMessage) {
        this.action = parseAction(jsonMessage.getString(ACTION_PARAMETER));
        this.uid = jsonMessage.getString(UID_PARAMETER);
        this.message = jsonMessage.getString(MESSAGE_PARAMETER);
    }
    
    private ACTION parseAction(String string) {
        try {
            return ACTION.valueOf(string);
        }catch(IllegalArgumentException iae) {
            return ACTION.UNDEFINED;
        }
    }

    public ACTION getAction() {
        return action;
    }

    public String getUid() {
        return uid;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "MessageCommand{" + "action=" + action + ", uid=" + uid + ", message=" + message + '}';
    }
}
