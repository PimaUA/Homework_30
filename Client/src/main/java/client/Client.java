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

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class Client {
    private static final Logger LOGGER = LogManager.getLogger(Server.class);
    private Bootstrap bootstrap;
    private ChannelFuture f;


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

            //send message to server
            Channel channel = f.sync().channel();
            channel.writeAndFlush(msg);
            channel.flush();

// Wait until the connection is closed(port)
            f.channel().closeFuture().sync();

        } finally {
            // Shut down the event loop to terminate all threads.
            group.shutdownGracefully();
        }
    }



    //????
    public String sendCommand(String msg) throws InterruptedException {
        Channel channel = f.sync().channel();
        channel.writeAndFlush(msg);
        channel.flush();
        return msg;
    }

    public static void main(String[] args) throws Exception {
Client client1=new Client();
client1.connect("127.0.0.1",5555,"Hey Ho");
        //client1.connect("127.0.0.1",5555,"-file C:/Users/Sasha/Downloads/file1.txt");

        Client client2=new Client();
        client2.connect("127.0.0.1",5555,"Let's Go!");
        //client2.connect("127.0.0.1",5555,"-file C:/Users/Sasha/Downloads/file2.txt");
    }
}