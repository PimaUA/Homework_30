package server;

import io.netty.channel.*;
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
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class ServerHandler extends SimpleChannelInboundHandler<String> {
    private static final Logger LOGGER = LogManager.getLogger(Server.class);
    private static final Set<ClientConnection> serverClients = new HashSet<>();
    ClientConnection clientConnection;
    static final List<Channel> channels = new ArrayList<>();

    static class ClientConnection {
        private final int int_random = ThreadLocalRandom.current().nextInt();
        private final String clientName = "Client " + int_random;
        private final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        private final LocalDateTime now = LocalDateTime.now();
        private final String timeOfConnection = dateTimeFormat.format(now);

        public ClientConnection(Channel channel) {
        }
    }

    //when channel is active
    @Override
    public void channelActive(final ChannelHandlerContext channelHandlerContext) {
        Channel channel = channelHandlerContext.channel();
        clientConnection = new ClientConnection(channel);
        serverClients.add(clientConnection);

        channels.add(channelHandlerContext.channel());
        for (Channel eachChannel : channels) {
            eachChannel.writeAndFlush(" Channel successfully connected to server "
                    + clientConnection.clientName);
        }
    }

    //main logic
    @Override
    public void channelRead0(ChannelHandlerContext channelHandlerContext, String msg) {
        LOGGER.info("Server received command " + msg);
        while (true) {
            if (msg == null) {
                break;
            } else if (msg.equals("-exit")) {
                channelHandlerContext.writeAndFlush(clientConnection.clientName + " disconnected")
                        .addListener(ChannelFutureListener.CLOSE);
                serverClients.remove(clientConnection);
                LOGGER.info(clientConnection.clientName + " have been removed from list of active connections");
                channelHandlerContext.close();
                break;
            } else if (msg.startsWith("-file ")) {
                String sourcePath = msg.substring(6);
                copyFile(sourcePath);
                channelHandlerContext.writeAndFlush("File saved to directory")
                        .addListener(ChannelFutureListener.CLOSE);
                LOGGER.info("File saved to directory");
                channelHandlerContext.close();
                break;
            } else {
                channelHandlerContext.writeAndFlush("Server received unknown command_ " + msg)
                        .addListener(ChannelFutureListener.CLOSE);
            }
        }
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

    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable cause) {
        LOGGER.info("Closing connection for " + clientConnection.clientName);
        cause.printStackTrace();
        channelHandlerContext.close();
    }
}
