package com.chatt.ufs.utils;

import com.chatt.ufs.Chat;
import com.chatt.ufs.UserList;
import com.chatt.ufs.model.ChatConversation;
import com.chatt.ufs.model.ChatUser;
import com.chatt.ufs.model.Conversation;
import com.chatt.ufs.protobuf.MessageProtos;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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

    public UserList userListAct;

    private ArrayList<ChatUser> uList;

    private ArrayList<ChatConversation> uCList;

    public static final String HOST = "34.214.209.137"; // "rhino.rmq.cloudamqp.com"
    public static final String USER = "kirk"; // "ygqnbuox"
    public static final String PASS = "senha123"; // "fhT5SRBYpG700pSZ3NcoyVeEj8RdosAx"

    public Connection getConnection() throws IOException, TimeoutException {
        if (connection == null){
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(HOST);
            factory.setUsername(USER);
            factory.setPassword(PASS);
//            factory.setVirtualHost("ygqnbuox");
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
        factory.setHost(HOST);
        factory.setUsername(USER);
        factory.setPassword(PASS);
//        factory.setVirtualHost("ygqnbuox");
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
                    }else if (conversaAtual != null){
                        Conversation conversation = new Conversation();
                        conversation.setStatus(Conversation.STATUS_SENT);
                        conversation.setDate(new Date());
                        conversation.setMsg(msg);
                        conversation.setSender(fromUser);
                        conversation.setReceiver(Singleton.getInstance().getUser());

                        addUlist(fromUser);
                        ArrayList<Conversation> ac =  Singleton.getInstance().findConversa(fromUser);
                        ac.add(conversation);
                        salvarConversa(fromUser,ac);

                        conversaAtual.newMessageAlert(fromUser);
//                        conversaAtual.finish();
                    }else {
                        Conversation conversation = new Conversation();
                        conversation.setStatus(Conversation.STATUS_SENT);
                        conversation.setDate(new Date());
                        conversation.setMsg(msg);
                        conversation.setSender(fromUser);
                        conversation.setReceiver(Singleton.getInstance().getUser());

                        addUlist(fromUser);
                        ArrayList<Conversation> ac =  Singleton.getInstance().findConversa(fromUser);
                        ac.add(conversation);
                        salvarConversa(fromUser,ac);

                        userListAct.updateList();
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

    public ArrayList<ChatUser> getuList() {
        if (uList == null){
            uList = new ArrayList<ChatUser>();
        }
        return uList;
    }

    public void setuList(ArrayList<ChatUser> uList) {
        this.uList = uList;
    }

    public ChatUser addUlist(String sender){
        int cont = 0;
        for (ChatUser c: getuList()) {
            if (c.getUsername().equals(sender)){
                   return c;
            }
        }
        getuList().add(new ChatUser("id",sender,"email",true, new ArrayList<String>()));
        return getuList().get(getuList().size()-1);
    }

    public void salvarConversa(String user, ArrayList<Conversation> convList){
        for (ChatConversation c: getuCList()) {
            if (c.getUser().equals(user)){
                if (convList != null) {
                    c.setConvList(new ArrayList<Conversation>(convList));
                    return;
                }
            }
        }
        ChatConversation c = new ChatConversation(new ArrayList<Conversation>(convList),user);
        Singleton.getInstance().getuCList().add(c);
    }

    public ArrayList<Conversation> findConversa(String user){
        for (ChatConversation c: getuCList()) {
            if (c.getUser().equals(user)){
                return c.getConvList();
            }
        }
        return new  ArrayList<Conversation>();
    }

    public ArrayList<ChatConversation> getuCList() {
        if (uCList == null)
            uCList = new ArrayList<ChatConversation>();
        return uCList;
    }

    public void setuCList(ArrayList<ChatConversation> uCList) {
        this.uCList = uCList;
    }
}

