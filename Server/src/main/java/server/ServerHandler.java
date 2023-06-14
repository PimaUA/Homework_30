package server;

import client.ClientHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Handles a server-side channel.
 */

public class ServerHandler extends SimpleChannelInboundHandler<String> {
    private static final Logger LOGGER = LogManager.getLogger(Server.class);
    private static final HashSet<ClientHandler> serverClients = new HashSet<>();
    ClientHandler eachClient;


    // List of connected client channels.
    static final List<Channel> channels = new ArrayList<>();
    static final List<ClientHandler> listOfClients = new ArrayList<>();

    /*
     * Whenever client connects to server through channel, add his channel to the
     * list of channels.
     */
    @Override
    public void channelActive(final ChannelHandlerContext channelHandlerContext) {
        eachClient = new ClientHandler();
        serverClients.add(eachClient);
        System.out.println("xx "+serverClients);
        LOGGER.info(eachClient.getClientName()+" added to serverClients list");
for(ClientHandler clientHandler:serverClients){
    channelHandlerContext.writeAndFlush(clientHandler+ " ConnectedYYY");
}


        channels.add(channelHandlerContext.channel());
        for (Channel eachChannel : channels) {
            eachChannel.writeAndFlush("Client successfully connected to server");
        }
    }

    /*
     * When a message is received from client, send that message to all channels.
     * For the sake of simplicity, currently we will send received chat message to
     * all clients instead of one specific client. This code has scope to improve to
     * send message to specific client as per senders choice.
     */
    @Override
    public void channelRead0(ChannelHandlerContext channelHandlerContext, String msg) throws IOException {
        LOGGER.info("Server received command " + msg);
            while (true){
            if(msg==null) {
               break;
            }
            else if(msg.equals("-exit")){
                channelHandlerContext.writeAndFlush("User disconnected").addListener(ChannelFutureListener.CLOSE);
                serverClients.remove(eachClient);
                System.out.println("xx after remove "+serverClients);
                LOGGER.info(eachClient.getClientName() + " have been removed from list of active connections");
                //channelHandlerContext.disconnect();
                channelHandlerContext.close();
                break;
            }
            else if (msg.startsWith("-file ")) {
                String sourcePath=msg.substring(6);
                copyFile(sourcePath);
                LOGGER.info("File saved to directory");
                channelHandlerContext.close();
                break;
            }
               else {
                channelHandlerContext.writeAndFlush("Server received unknown command_ " + msg)
                        .addListener(ChannelFutureListener.CLOSE);
                //throw new IOException();
            }
                }
        //channelHandlerContext.writeAndFlush(msg + '\n').addListener(ChannelFutureListener.CLOSE);
    }

    public void copyFile(String sourcePath) {
        Path p = Paths.get(sourcePath);
        String newFileName = p.getFileName().toString();
        final String pathToDirectory = "C:/IdeaProjects/Homework_30/Server/src/main/resources/SavedFiles";
        final File dest = new File(pathToDirectory + "/" + newFileName);
        try (FileChannel sourceChannel = new FileInputStream(sourcePath).getChannel();
             FileChannel destChannel = new FileOutputStream(dest).getChannel()) {
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * In case of exception, close channel. One may choose to custom handle exception
     * & have alternative logical flows.
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable cause) {
        LOGGER.info("Closing connection for " +eachClient.getClientName());
        cause.printStackTrace();
        channelHandlerContext.close();
    }
}
