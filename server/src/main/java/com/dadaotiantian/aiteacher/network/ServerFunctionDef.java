package com.dadaotiantian.aiteacher.network;

public enum ServerFunctionDef {
    LOGIN_RSP(0x1001),
    HEARTBEAT_RSP(0x1002),
    LOGOUT_RSP(0x1003),
    REGISTER_RSP(0x1011),
    LIST_PLAYER_RSP(0x1021),
    CREATE_PLAYER_RSP(0x1022),
    SELECT_PLAYER_RSP(0x1023),
    DELETE_PLAYER_RSP(0x1024),
    CREATE_AGENT_RSP(0x1031),
    LIST_AGENT_RSP(0x1032),
    WORD_TEST_RSP(0x1041),
    WORD_REVIEW_RSP(0x1042),
    CHAT_MESSAGE_RSP(0x1051),
    ERROR_RSP(0x1FFF);

    private final int msgId;

    ServerFunctionDef(int msgId) {
        this.msgId = msgId;
    }

    public int getMsgId() {
        return msgId;
    }
}
