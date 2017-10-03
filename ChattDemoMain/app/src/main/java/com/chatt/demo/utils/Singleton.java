package com.chatt.demo.utils;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

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
}
