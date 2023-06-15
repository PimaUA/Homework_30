package client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.jmx.Server;

public class Client {
    private static final Logger LOGGER = LogManager.getLogger(Server.class);
    private static Bootstrap bootstrap;
    private static ChannelFuture channelFuture;
    private static EventLoopGroup group;

    public Client() {
        bootstrap = new Bootstrap();
        group = new NioEventLoopGroup();
        bootstrap.group(group) // Set EventLoopGroup to handle all events for client.
                .channel(NioSocketChannel.class)// Use NIO to accept new connections.
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel socketChannel) {
                        ChannelPipeline channelPipeline = socketChannel.pipeline();
                        /*
                         * Socket/channel communication happens in byte streams. String decoder &
                         * encoder helps conversion between bytes & String.
                         */
                        channelPipeline.addLast(new StringDecoder());
                        channelPipeline.addLast(new StringEncoder());
                        // This is our custom client handler which will have some logic.
                        channelPipeline.addLast(new ClientHandler());
                    }
                });
    }

    public void startClientExecution(String host, int port, String command) throws InterruptedException {
        // Start the client & send command
        channelFuture = bootstrap.connect(host, port).sync();
        LOGGER.info("Client started");
        sendCommand(command);
        // Wait until the connection is closed
        channelFuture.channel().closeFuture().sync();
    }

    private void sendCommand(String command) throws InterruptedException {
        Channel channel = channelFuture.sync().channel();
        channel.writeAndFlush(command);
        channel.flush();
    }

    public void closeConnection() {
        // Shut down the event loop to terminate all threads.
        LOGGER.info("--Client connection closed");
        group.shutdownGracefully();
    }

    public static void main(String[] args) throws Exception {
        Client client1 = new Client();
        client1.startClientExecution("127.0.0.1", 5555, "-file C:/Users/Sasha/Downloads/file1.txt");
        client1.startClientExecution("127.0.0.1", 5555, "hello");
        client1.startClientExecution("127.0.0.1", 5555, "-exit");
        client1.closeConnection();

        Client client2 = new Client();
        client2.startClientExecution("127.0.0.1", 5555, "-file C:/Users/Sasha/Downloads/file2.txt");
        client2.startClientExecution("127.0.0.1", 5555, "hello");
        client2.startClientExecution("127.0.0.1", 5555, "-exit");
        client2.closeConnection();

        Client client3 = new Client();
        client3.startClientExecution("127.0.0.1", 5555, "-file C:/Users/Sasha/Downloads/file3.txt");
        client3.startClientExecution("127.0.0.1", 5555, "hello");
        client3.startClientExecution("127.0.0.1", 5555, "-exit");
        client3.closeConnection();

        Client client4 = new Client();
        client4.startClientExecution("127.0.0.1", 5555, "-file C:/Users/Sasha/Downloads/file4.txt");
        client4.startClientExecution("127.0.0.1", 5555, "hello");
        client4.startClientExecution("127.0.0.1", 5555, "-exit");
        client4.closeConnection();
    }
}