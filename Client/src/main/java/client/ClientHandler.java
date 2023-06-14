package client;

import io.netty.channel.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.jmx.Server;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

public class ClientHandler extends SimpleChannelInboundHandler<String> {
    private static final Logger LOGGER = LogManager.getLogger(Server.class);
    private final int int_random = ThreadLocalRandom.current().nextInt();
    private final String clientName = "Client " + int_random;
    private final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final LocalDateTime now = LocalDateTime.now();
    private final String timeOfConnection = dateTimeFormat.format(now);


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        /*eachClient = new ClientHandler();
        serverClients.add(eachClient);
        System.out.println("xx "+serverClients);
        LOGGER.info(eachClient.getClientName()+" added to serverClients list");*/
       /* for (ClientHandler eachClient : serverClients) {
            eachClient.writeAndFlush(eachClient.getClientName()+" successfully connected to server");
        }*/
    }

    /*@Override
    public void channelInactive(ChannelHandlerContext ctx){
        System.out.println("Client removed");
    }*/

    //message received from server
  @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg)  {
        LOGGER.info("Message from server: " + msg);
        //ctx.writeAndFlush(msg);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("Error caught in the communication service: " + cause);
        cause.printStackTrace();
        ctx.close();
    }

    public String getClientName() {
        return clientName;
    }
}
