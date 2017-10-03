package com.chatt.ufs.utils;

import com.chatt.ufs.Chat;
import com.chatt.ufs.protobuf.MessageProtos;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by guilhermeboroni on 26/09/2017.
 */

public class Singleton {
    private static Singleton instance;

    public static Singleton getInstance() {
        if (instance == null)
            instance = new Singleton();
        return instance;
    }

    /**
     * Conexão com o servidor que processa a fila de mensagens.
     */
    private Connection connection;

    /**
     * Canal de comunicação que utiliza a conexão com o servidor AMQP.
     */
    private Channel channel;

    private String user;

    private boolean recebendoMsg = false;

    public Chat conversaAtual;

    public Connection getConnection() throws IOException, TimeoutException {
        if (connection == null){
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("rhino.rmq.cloudamqp.com");
            factory.setUsername("ygqnbuox");
            factory.setPassword("fhT5SRBYpG700pSZ3NcoyVeEj8RdosAx");
            factory.setVirtualHost("ygqnbuox");
            connection = factory.newConnection();
        }
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Channel getChannel() throws IOException, TimeoutException {
        if (channel == null){
            channel = getConnection().createChannel();
            channel.queueDeclare(user, false, false, false, null);
        }
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Connection newConnection() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rhino.rmq.cloudamqp.com");
        factory.setUsername("ygqnbuox");
        factory.setPassword("fhT5SRBYpG700pSZ3NcoyVeEj8RdosAx");
        factory.setVirtualHost("ygqnbuox");
        Connection connection = factory.newConnection();
        return connection;
    }

    public boolean isRecebendoMsg() {
        return recebendoMsg;
    }

    public void setRecebendoMsg(boolean recebendoMsg) {
        this.recebendoMsg = recebendoMsg;
    }

    public void subscribe() throws IOException, TimeoutException {
        Consumer consumer = new DefaultConsumer(Singleton.getInstance().getChannel()) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                String msg = "";

                MessageProtos.Message message = MessageProtos.Message.parseFrom(body);
                String fromGroup = message.getGroup();
                String fromUser = message.getSender();
                String date = message.getDate();
                String time = message.getTime();
                msg = message.getContent(0).getData().toStringUtf8();

                if (fromGroup.equals("")) {
                    // Exibe a mensagem direta
                    System.out.println("");
                    Chat conversaAtual = Singleton.getInstance().conversaAtual;
                    if (conversaAtual != null && conversaAtual.buddy.getUsername().equals(fromUser)){
                        conversaAtual.updateListReceived(msg);
                    }
                    System.out.println("(" + date + " Ã s " + time + ") " + fromUser + " diz: " + msg);
                } else {
                    // Ã‰ uma mensagem de um grupo
                    if (!fromUser.equals(user)) {
                        // Exibe a mensagem se o emissor nÃ£o for o prÃ³prio usuÃ¡rio (previne o "eco")
                        System.out.println("");
                        System.out.println(fromUser + " (" + fromGroup + ") diz: " + msg);
                    }
                }

            }
        };
        Singleton.getInstance().getChannel().basicConsume(Singleton.getInstance().getUser(), true, consumer);
        Singleton.getInstance().setRecebendoMsg(true);
    }

}

