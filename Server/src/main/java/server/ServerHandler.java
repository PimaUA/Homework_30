package server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Handles a server-side channel.
 */

public class ServerHandler extends SimpleChannelInboundHandler<String> {
    private static final Logger LOGGER = LogManager.getLogger(Server.class);

    private final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final LocalDateTime now = LocalDateTime.now();
    private final String timeOfConnection = dateTimeFormat.format(now);

    // List of connected client channels.
    static final List<Channel> channels = new ArrayList<>();

    /*
     * Whenever client connects to server through channel, add his channel to the
     * list of channels.
     */
    @Override
    public void channelActive(final ChannelHandlerContext channelHandlerContext) {
        LOGGER.info("Client joined - " + channelHandlerContext);
        channels.add(channelHandlerContext.channel());
        for (Channel eachChannel : channels) {
            eachChannel.writeAndFlush("Client connected know it");
        }
    }

    /*
     * When a message is received from client, send that message to all channels.
     * For the sake of simplicity, currently we will send received chat message to
     * all clients instead of one specific client. This code has scope to improve to
     * send message to specific client as per senders choice.
     */
    @Override
    public void channelRead0(ChannelHandlerContext channelHandlerContext, String msg) {
        LOGGER.info("Server received - " + msg);
            while (true){
            if(msg==null) {
               break;
            }
            else if(msg.equals("-exit")){
                channelHandlerContext.writeAndFlush("User disconnected").addListener(ChannelFutureListener.CLOSE);
                LOGGER.info("Closed by user's request");
                channelHandlerContext.close();
                break;
            } else if (msg.equals("-file")) {

            }
                channelHandlerContext.writeAndFlush("-> " + msg + '\n').addListener(ChannelFutureListener.CLOSE);}
    }







    /*
     * In case of exception, close channel. One may choose to custom handle exception
     * & have alternative logical flows.
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable cause) {
        LOGGER.info("Closing connection for client XXXXXX- " + channelHandlerContext);
        cause.printStackTrace();
        channelHandlerContext.close();
    }
}
