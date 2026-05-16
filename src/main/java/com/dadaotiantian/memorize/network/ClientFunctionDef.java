package com.dadaotiantian.memorize.network;

import java.util.HashMap;
import java.util.Map;

public enum ClientFunctionDef {
    LOGIN_REQ(0x0001),
    HEARTBEAT_REQ(0x0002),
    LOGOUT_REQ(0x0003),
    REGISTER_REQ(0x0011),
    LIST_PLAYER_REQ(0x0021),
    CREATE_PLAYER_REQ(0x0022),
    SELECT_PLAYER_REQ(0x0023),
    DELETE_PLAYER_REQ(0x0024),
    CREATE_AGENT_REQ(0x0031),
    LIST_AGENT_REQ(0x0032),
    WORD_TEST_REQ(0x0041),
    WORD_REVIEW_REQ(0x0042),
    CHAT_MESSAGE_REQ(0x0051);

    private static final Map<Integer, ClientFunctionDef> BY_ID = new HashMap<>();

    static {
        for (ClientFunctionDef value : values()) {
            BY_ID.put(value.msgId, value);
        }
    }

    private final int msgId;

    ClientFunctionDef(int msgId) {
        this.msgId = msgId;
    }

    public int getMsgId() {
        return msgId;
    }

    public static ClientFunctionDef fromId(int msgId) {
        return BY_ID.get(msgId);
    }
}
