package com.dadaotiantian.memorize.network;

import java.util.Map;

public class MessagePacket {
    private final int msgId;
    private final long uid;
    private final Map<String, Object> body;

    public MessagePacket(int msgId, long uid, Map<String, Object> body) {
        this.msgId = msgId;
        this.uid = uid;
        this.body = body;
    }

    public int getMsgId() {
        return msgId;
    }

    public long getUid() {
        return uid;
    }

    public Map<String, Object> getBody() {
        return body;
    }
}
