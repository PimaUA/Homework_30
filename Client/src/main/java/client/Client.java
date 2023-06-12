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

import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class Client {
    private static final Logger LOGGER = LogManager.getLogger(Server.class);
   // private String host;        //"127.0.0.1";
   // private int port;           //5555;
    private final int int_random = ThreadLocalRandom.current().nextInt();
    private final String clientName = "Client " + int_random;
    private Bootstrap bootstrap;

    public Client() {
        bootstrap = new Bootstrap();
    }

    public void connect(String host, int port,String msg) throws Exception {
        /*
         * Configure the client.
         */
        // Since this is client, it doesn't need boss group. Create single group.
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            bootstrap = new Bootstrap();
            bootstrap.group(group) // Set EventLoopGroup to handle all events for client.
                    .channel(NioSocketChannel.class)// Use NIO to accept new connections.
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            /*
                             * Socket/channel communication happens in byte streams. String decoder &
                             * encoder helps conversion between bytes & String.
                             */
                            p.addLast(new StringDecoder());
                            p.addLast(new StringEncoder());

                            // This is our custom client handler which will have logic for chat.
                            p.addLast(new ClientHandler());

                        }
                    });
// Start the client.
            ChannelFuture f = bootstrap.connect(host, port).sync();

            //
            Channel channel = f.sync().channel();
            channel.writeAndFlush(msg);
            channel.flush();

// Wait until the connection is closed(port)
            f.channel().closeFuture().sync();
            LOGGER.info("port closed");

        } finally {
            // Shut down the event loop to terminate all threads.
            group.shutdownGracefully();
            LOGGER.info("final");
        }
    }

    public static void main(String[] args) throws Exception {
Client client1=new Client();
client1.connect("127.0.0.1",5555,"Hey Ho");

        Client client2=new Client();
        client1.connect("127.0.0.1",5555,"Let's Go!");
    }
}