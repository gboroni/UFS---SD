package com.chatt.ufs;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import com.chatt.ufs.custom.CustomActivity;
import com.chatt.ufs.model.ChatUser;
import com.chatt.ufs.model.Conversation;
import com.chatt.ufs.requests.SendMessageAsync;
import com.chatt.ufs.utils.Const;
import com.chatt.ufs.utils.Singleton;
import com.chatt.ufs.utils.Utils;


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
    public ChatUser buddy;

    /**
     * The date of last message in conversation.
     */
    private Date lastMsgDate;

    private ProgressDialog loginProgressDlg;

    public Handler incomingMessageHandler;

    public Handler incomingNewMessageHandler;

    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);


        buddy = (ChatUser) getIntent().getSerializableExtra(Const.EXTRA_DATA);

        convList = Singleton.getInstance().findConversa(buddy.getUsername());
        ListView list = (ListView) findViewById(R.id.list);
        adp = new ChatAdapter();
        list.setAdapter(adp);
        list.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        list.setStackFromBottom(true);

        txt = (EditText) findViewById(R.id.txt);
        txt.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        setTouchNClick(R.id.btnSend);

        ActionBar actionBar = getActionBar();
        if(actionBar != null)
            actionBar.setTitle(buddy.getUsername());

         incomingMessageHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                adp.notifyDataSetChanged();
            }
        };

        incomingNewMessageHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Utils.showDialogNovaMensagem(Chat.this,msg.getData().getString("sender"));
            }
        };

        Singleton.getInstance().conversaAtual = this;



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
        loginProgressDlg = ProgressDialog.show(this, null,
                getString(R.string.alert_wait));
        new SendMessageAsync(loginProgressDlg,Chat.this, this).execute(buddy.getUsername(),text);
    }

    public void updateList(String text){
        Conversation conversation = new Conversation();
        conversation.setStatus(Conversation.STATUS_SENT);
        conversation.setDate(new Date());
        conversation.setMsg(text);
        conversation.setSender(Singleton.getInstance().getUser());
        conversation.setReceiver(buddy.getUsername());

        convList.add(conversation);

        Singleton.getInstance().salvarConversa(buddy.getUsername(),convList);
        adp.notifyDataSetChanged();
    }

    public void updateListReceived(String text){
        Conversation conversation = new Conversation();
        conversation.setStatus(Conversation.STATUS_SENT);
        conversation.setDate(new Date());
        conversation.setMsg(text);
        conversation.setSender(buddy.getUsername());
        conversation.setReceiver(Singleton.getInstance().getUser());

        convList.add(conversation);

        incomingMessageHandler.sendMessage(new Message());
    }

    public void newMessageAlert(String sender){
        Message m = new Message();
        Bundle b = new Bundle();
        b.putString("sender", sender); // for example
        m.setData(b);
        incomingNewMessageHandler.sendMessage(m);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
       Singleton.getInstance().conversaAtual = null;

    }

}
