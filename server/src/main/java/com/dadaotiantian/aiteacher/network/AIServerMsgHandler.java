package com.dadaotiantian.aiteacher.network;

import com.dadaotiantian.aiteacher.account.AccountManager;
import com.dadaotiantian.aiteacher.agent.AgentManager;
import com.dadaotiantian.aiteacher.player.PlayerManager;
import com.dadaotiantian.aiteacher.thread.ThreadManager;
import com.dadaotiantian.aiteacher.word.WordManager;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public final class AIServerMsgHandler {
    private static final Logger log = LoggerFactory.getLogger(AIServerMsgHandler.class);

    private AIServerMsgHandler() {
    }

    public static void handle(Channel channel, MessagePacket packet) {
        ThreadManager.getBusinessExecutor().execute(() -> dispatch(channel, packet));
    }

    private static void dispatch(Channel channel, MessagePacket packet) {
        ClientFunctionDef def = ClientFunctionDef.fromId(packet.getMsgId());
        if (def == null) {
            send(channel, ServerFunctionDef.ERROR_RSP, packet.getUid(), error("UNKNOWN_MESSAGE", "未知消息ID: " + packet.getMsgId()));
            return;
        }
        try {
            switch (def) {
                case REGISTER_REQ -> send(channel, ServerFunctionDef.REGISTER_RSP, 0, AccountManager.register(packet.getBody()));
                case LOGIN_REQ -> send(channel, ServerFunctionDef.LOGIN_RSP, 0, AccountManager.login(packet.getBody()));
                case HEARTBEAT_REQ -> send(channel, ServerFunctionDef.HEARTBEAT_RSP, packet.getUid(), ok("pong"));
                case LOGOUT_REQ -> send(channel, ServerFunctionDef.LOGOUT_RSP, packet.getUid(), AccountManager.logout(packet.getBody()));
                case LIST_PLAYER_REQ -> send(channel, ServerFunctionDef.LIST_PLAYER_RSP, 0, PlayerManager.list(packet.getBody()));
                case CREATE_PLAYER_REQ -> send(channel, ServerFunctionDef.CREATE_PLAYER_RSP, 0, PlayerManager.create(packet.getBody()));
                case SELECT_PLAYER_REQ -> send(channel, ServerFunctionDef.SELECT_PLAYER_RSP, packet.getUid(), PlayerManager.select(packet.getBody()));
                case DELETE_PLAYER_REQ -> send(channel, ServerFunctionDef.DELETE_PLAYER_RSP, packet.getUid(), PlayerManager.delete(packet.getBody()));
                case CREATE_AGENT_REQ -> AgentManager.create(channel, packet);
                case LIST_AGENT_REQ -> send(channel, ServerFunctionDef.LIST_AGENT_RSP, packet.getUid(), AgentManager.list(packet));
                case CHAT_MESSAGE_REQ -> AgentManager.chat(channel, packet);
                case WORD_TEST_REQ -> send(channel, ServerFunctionDef.WORD_TEST_RSP, packet.getUid(), WordManager.test(packet));
                case WORD_REVIEW_REQ -> send(channel, ServerFunctionDef.WORD_REVIEW_RSP, packet.getUid(), WordManager.review(packet));
            }
        } catch (Exception ex) {
            log.error("handle message failed: {}", def, ex);
            send(channel, ServerFunctionDef.ERROR_RSP, packet.getUid(), error("SERVER_ERROR", ex.getMessage()));
        }
    }

    public static void send(Channel channel, ServerFunctionDef def, long uid, Map<String, Object> body) {
        try {
            channel.writeAndFlush(new io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame(MessageCodec.encode(def, uid, body)));
        } catch (Exception ex) {
            log.error("send message failed: {}", def, ex);
        }
    }

    public static Map<String, Object> ok(String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("result", 0);
        map.put("message", message);
        return map;
    }

    public static Map<String, Object> error(String code, String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("result", 1);
        map.put("code", code);
        map.put("message", message == null ? code : message);
        return map;
    }
}
