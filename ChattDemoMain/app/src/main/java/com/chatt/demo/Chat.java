package com.chatt.demo;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.DateUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.chatt.demo.custom.CustomActivity;
import com.chatt.demo.model.ChatUser;
import com.chatt.demo.model.Conversation;
import com.chatt.demo.protobuf.MessageProtos;
import com.chatt.demo.utils.Const;
import com.chatt.demo.utils.Singleton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.protobuf.ByteString;
import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeoutException;


/**
 * The Class Chat is the Activity class that holds main chat screen. It shows
 * all the conversation messages between two users and also allows the user to
 * send and receive messages.
 */
public class Chat extends CustomActivity {

    /**
     * The Conversation list.
     */
    private ArrayList<Conversation> convList;

    /**
     * The chat adapter.
     */
    private ChatAdapter adp;

    /**
     * The Editext to compose the message.
     */
    private EditText txt;

    /**
     * The user name of buddy.
     */
    private ChatUser buddy;

    /**
     * The date of last message in conversation.
     */
    private Date lastMsgDate;

    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        convList = new ArrayList<Conversation>();
        ListView list = (ListView) findViewById(R.id.list);
        adp = new ChatAdapter();
        list.setAdapter(adp);
        list.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        list.setStackFromBottom(true);

        txt = (EditText) findViewById(R.id.txt);
        txt.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        setTouchNClick(R.id.btnSend);

        buddy = (ChatUser) getIntent().getSerializableExtra(Const.EXTRA_DATA);

        ActionBar actionBar = getActionBar();
        if(actionBar != null)
            actionBar.setTitle(buddy.getUsername());

    }

    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();

    }

    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();
    }

    /* (non-Javadoc)
     * @see com.socialshare.custom.CustomFragment#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.btnSend) {
            try {
                sendMessage();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Call this method to Send message to opponent. It does nothing if the text
     * is empty otherwise it creates a Parse object for Chat message and send it
     * to Parse server.
     */
    private void sendMessage() throws IOException, TimeoutException {
        if (txt.length() == 0)
            return;

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txt.getWindowToken(), 0);

        String s = txt.getText().toString();

        sendMessageRabbit(s.toString());

        Conversation conversation = new Conversation();
        conversation.setStatus(Conversation.STATUS_SENT);
        conversation.setDate(new Date());
        conversation.setMsg(txt.getText().toString());
        conversation.setSender(Singleton.getInstance().getUser());
        conversation.setReceiver(buddy.getUsername());

        convList.add(conversation);

        adp.notifyDataSetChanged();
        txt.setText(null);
    }

    /**
     * The Class ChatAdapter is the adapter class for Chat ListView. This
     * adapter shows the Sent or Receieved Chat message in each list item.
     */
    private class ChatAdapter extends BaseAdapter {

        /* (non-Javadoc)
         * @see android.widget.Adapter#getCount()
         */
        @Override
        public int getCount() {
            return convList.size();
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getItem(int)
         */
        @Override
        public Conversation getItem(int arg0) {
            return convList.get(arg0);
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getItemId(int)
         */
        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
         */
        @SuppressLint("InflateParams")
        @Override
        public View getView(int pos, View v, ViewGroup arg2) {
            Conversation c = getItem(pos);
            if (c.isSent())
                v = getLayoutInflater().inflate(R.layout.chat_item_sent, null);
            else
                v = getLayoutInflater().inflate(R.layout.chat_item_rcv, null);

            TextView lbl = (TextView) v.findViewById(R.id.lbl1);
            lbl.setText(DateUtils.getRelativeDateTimeString(Chat.this, c
                            .getDate().getTime(), DateUtils.SECOND_IN_MILLIS,
                    DateUtils.DAY_IN_MILLIS, 0));

            lbl = (TextView) v.findViewById(R.id.lbl2);
            lbl.setText(c.getMsg());

            lbl = (TextView) v.findViewById(R.id.lbl3);
            if (c.isSent()) {
                if (c.getStatus() == Conversation.STATUS_SENT)
                    lbl.setText(R.string.delivered_text);
                else {
                    if (c.getStatus() == Conversation.STATUS_SENDING)
                        lbl.setText(R.string.sending_text);
                    else {
                        lbl.setText(R.string.failed_text);
                    }
                }
            } else
                lbl.setText("");

            return v;
        }

    }

    /* (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Envia a mensagem lida para a fila de mensagens do destinatário.
     *
     */
    public void sendMessageRabbit(String text) throws IOException, TimeoutException {
        Channel channel = Singleton.getInstance().getConnection().createChannel();
        // Protocolo da mensagem: grupo | usuário | conteúdo
//        if (isGroup) {
//            channel.exchangeDeclare(sendTo, "fanout");
//            // channel.basicPublish(sendTo, "", null, (sendTo + SEPARATOR + user + SEPARATOR
//            // + msg).getBytes("UTF-8"));
//            channel.basicPublish(sendTo, "", null, makeMessage(user, msg, sendTo));
//        } else {
            channel.queueDeclare(buddy.getUsername(), false, false, false, null);
            // channel.basicPublish("", sendTo, null, ("" + SEPARATOR + user + SEPARATOR +
            // msg).getBytes("UTF-8"));
            channel.basicPublish("", buddy.getUsername(), null, makeMessage(Singleton.getInstance().getUser(), text,""));
//        }
        channel.close();
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
