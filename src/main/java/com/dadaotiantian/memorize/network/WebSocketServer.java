package com.dadaotiantian.memorize.network;

import com.dadaotiantian.memorize.config.ServerConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketServer {
    private static final Logger log = LoggerFactory.getLogger(WebSocketServer.class);

    private final ServerConfig config;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture channelFuture;

    public WebSocketServer(ServerConfig config) {
        this.config = config;
    }

    public void start() throws InterruptedException {
        bossGroup = new NioEventLoopGroup(config.getServer().getBossThreads());
        workerGroup = new NioEventLoopGroup(config.getServer().getWorkerThreads());
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast(new HttpServerCodec())
                                .addLast(new ChunkedWriteHandler())
                                .addLast(new HttpObjectAggregator(65536))
                                .addLast(new WebSocketServerProtocolHandler(config.getServer().getWebsocketPath(), null, true))
                                .addLast(new NettyFrameHandler());
                    }
                });
        channelFuture = bootstrap.bind(config.getServer().getPort()).sync();
    }

    public void stop() {
        if (channelFuture != null) {
            channelFuture.channel().close();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        log.info("WebSocket server stopped");
    }

    private static class NettyFrameHandler extends io.netty.channel.SimpleChannelInboundHandler<BinaryWebSocketFrame> {
        @Override
        protected void channelRead0(io.netty.channel.ChannelHandlerContext ctx, BinaryWebSocketFrame frame) throws Exception {
            MessagePacket packet = MessageCodec.decode(frame.content());
            AIServerMsgHandler.handle(ctx.channel(), packet);
        }
    }
}
