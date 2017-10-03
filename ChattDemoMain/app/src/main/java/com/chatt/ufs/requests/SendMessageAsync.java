package com.chatt.ufs.requests;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.chatt.ufs.Chat;
import com.chatt.ufs.model.Conversation;
import com.chatt.ufs.protobuf.MessageProtos;
import com.chatt.ufs.utils.Singleton;
import com.google.protobuf.ByteString;
import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeoutException;

/**
 * Created by guilhermeboroni on 26/09/2017.
 */

public class SendMessageAsync extends AsyncTask<String, Void, String>{
    ProgressDialog dialog;
    Context ctx;
    ArrayList<Conversation> convList;
    Chat act;

    public SendMessageAsync(ProgressDialog dialog, Context ctx, Chat act){
        this.dialog = dialog;
        this.ctx = ctx;
        this.act = act;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected String doInBackground(String... params) {

        try {
            Channel channel = Singleton.getInstance().getConnection().createChannel();
            // Protocolo da mensagem: grupo | usuário | conteúdo
//        if (isGroup) {
//            channel.exchangeDeclare(sendTo, "fanout");
//            // channel.basicPublish(sendTo, "", null, (sendTo + SEPARATOR + user + SEPARATOR
//            // + msg).getBytes("UTF-8"));
//            channel.basicPublish(sendTo, "", null, makeMessage(user, msg, sendTo));
//        } else {
            channel.queueDeclare(params[0], false, false, false, null);
            // channel.basicPublish("", sendTo, null, ("" + SEPARATOR + user + SEPARATOR +
            // msg).getBytes("UTF-8"));
            channel.basicPublish("", params[0], null, makeMessage(Singleton.getInstance().getUser(), params[1],""));
//        }
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        return params[1];

    }


    @Override
    protected void onPostExecute(String result) {
        if (dialog.isShowing())
            dialog.dismiss();

        if (result != null && !result.equals("")){
            act.updateList(result);
        }



    }

    /**
     * Retorna os bytes da mensagem no formato protocol buffer.
     *
     * @param sender
     *            Usuário que enviou a mensagem
     * @param text
     *            Conteúdo textual da mensagem
     * @param group
     *            Nome do grupo destinatário
     * @return Mensagem serializada
     */
    private static byte[] makeMessage(String sender, String text, String group) {

        MessageProtos.Message.Content.Builder data = MessageProtos.Message.Content.newBuilder().setData(ByteString.copyFromUtf8(text))
                .setType(MessageProtos.Message.ContentType.TEXT);
        MessageProtos.Message.Builder m = MessageProtos.Message.newBuilder().setSender(sender).setDate(getCurrentDate()).setTime(getCurrentTime())
                .addContent(data);
        if (!group.isEmpty()) {
            m.setGroup(group);
        }
        return m.build().toByteArray();
    }

    /**
     * Pega o dia atual
     *
     * @return strDate
     */
    public static String getCurrentDate() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    /**
     * Pega a Hora atual
     *
     * @return strTime
     */
    public static String getCurrentTime() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm");
        Date now = new Date();
        String strTime = sdfDate.format(now);
        return strTime;
    }

}


