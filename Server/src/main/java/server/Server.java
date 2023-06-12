package server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Server {
    private static final Logger LOGGER = LogManager.getLogger(Server.class);
    private static final int PORT=5555;

    public static void main(String[] args) throws InterruptedException {
        /*
         * Configure the server.
         */

        // Create boss & worker groups. Boss accepts connections from client. Worker
        // handles further communication through connections.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup) // Set boss & worker groups
                    .channel(NioServerSocketChannel.class)// Use NIO to accept new connections.
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel socketChannel) {
                            ChannelPipeline channelPipeline = socketChannel.pipeline();
                            /*
                             * Socket/channel communication happens in byte streams. String decoder &
                             * encoder helps conversion between bytes & String.
                             */
                            channelPipeline.addLast(new StringDecoder());
                            channelPipeline.addLast(new StringEncoder());

                            // This is our custom server handler which will have logic for chat.
                            channelPipeline.addLast(new ServerHandler());
                        }
                    });

            // Start the server.
            ChannelFuture channelFuture = serverBootstrap.bind(PORT).sync();
            LOGGER.info("Chat Server started. Ready to accept chat clients.");

            // Wait until the server socket is closed.
            channelFuture.channel().closeFuture().sync();
        } finally {
            // Shut down all event loops to terminate all threads.
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}