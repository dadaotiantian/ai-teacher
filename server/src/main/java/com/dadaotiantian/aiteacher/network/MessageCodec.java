package com.dadaotiantian.aiteacher.network;

import com.dadaotiantian.aiteacher.utils.JsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.util.Map;

public final class MessageCodec {
    private static final int HEADER_LENGTH = 10;

    private MessageCodec() {
    }

    public static MessagePacket decode(ByteBuf buf) throws IOException {
        if (buf.readableBytes() < HEADER_LENGTH) {
            throw new IllegalArgumentException("message too short");
        }
        int msgId = buf.readUnsignedShort();
        long length = buf.readUnsignedInt();
        long uid = buf.readUnsignedInt();
        if (length > buf.readableBytes()) {
            throw new IllegalArgumentException("invalid message length");
        }
        byte[] bodyBytes = new byte[(int) length];
        buf.readBytes(bodyBytes);
        return new MessagePacket(msgId, uid, JsonUtil.parseObject(bodyBytes));
    }

    public static ByteBuf encode(ServerFunctionDef def, long uid, Map<String, Object> body) throws IOException {
        byte[] bytes = JsonUtil.toBytes(body);
        ByteBuf out = Unpooled.buffer(HEADER_LENGTH + bytes.length);
        out.writeShort(def.getMsgId());
        out.writeInt(bytes.length);
        out.writeInt((int) uid);
        out.writeBytes(bytes);
        return out;
    }
}
