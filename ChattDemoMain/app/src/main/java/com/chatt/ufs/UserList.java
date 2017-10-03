package com.chatt.ufs;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.chatt.ufs.custom.CustomActivity;
import com.chatt.ufs.model.ChatUser;
import com.chatt.ufs.protobuf.MessageProtos;
import com.chatt.ufs.utils.Const;
import com.chatt.ufs.utils.CustomAdapter;
import com.chatt.ufs.utils.Singleton;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeoutException;


/**
 * The Class UserList is the Activity class. It shows a list of all users of
 * this app. It also shows the Offline/Online status of users.
 */
public class UserList extends CustomActivity
{

	/** The Chat list. */
	private ArrayList<ChatUser> uList;

	/** The user. */
	public static ChatUser user;

	public ArrayAdapter<ChatUser> adapter;

	Thread subscribeThread;
	Thread publishThread;

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_list);

		getActionBar().setDisplayHomeAsUpEnabled(false);

		updateUserStatus(true);
        user = new ChatUser();
        user.setId(Singleton.getInstance().getUser());
        user.setEmail(Singleton.getInstance().getUser());;

		final Handler incomingMessageHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String message = msg.getData().getString("msg");
				Date now = new Date();
				SimpleDateFormat ft = new SimpleDateFormat("hh:mm:ss");
				Log.i("CHAT RECEBIDO >>> ", ft.format(now) + ' ' + message + '\n');
			}
		};


	}


	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onDestroy()
	 */
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		updateUserStatus(false);
//		subscribeThread.interrupt();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			showAddUser();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	protected void onResume()
	{
		super.onResume();
		loadUserList();

	}


	public void showAddUser(){
		AlertDialog.Builder builder = new AlertDialog.Builder(UserList.this)
				.setTitle("Pesquisar Usuario")
				.setMessage("Informe o nome do usuaro");
		final FrameLayout frameView = new FrameLayout(UserList.this);
		builder.setView(frameView);

		final AlertDialog alertDialog = builder.create();
		LayoutInflater inflater = alertDialog.getLayoutInflater();
		View dialoglayout = inflater.inflate(R.layout.add, frameView);

		Button b = (Button) dialoglayout.findViewById(R.id.confirm);
		final EditText userName = (EditText) dialoglayout.findViewById(R.id.userName);
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				uList.add(new ChatUser("id",userName.getText().toString(),"email",true, new ArrayList<String>()));
				adapter.notifyDataSetChanged();
				alertDialog.dismiss();
			}
		});


		alertDialog.show();
	}
	/**
	 * Update user status.
	 *
	 * @param online
	 *            true if user is online
	 */
	private void updateUserStatus(boolean online)
	{
		// TODO: Add user status updates
	}

	/**
	 * Load list of users.
	 */
	private void loadUserList()
	{
//		final ProgressDialog dia = ProgressDialog.show(this, null,
//				getString(R.string.alert_loading));

//		Toast.makeText(UserList.this,
//				R.string.msg_no_user_found,
//				Toast.LENGTH_SHORT).show();

		uList = new ArrayList<ChatUser>();
//		for(DataSnapshot ds : dataSnapshot.getChildren()) {
//			ChatUser user = ds.getValue(ChatUser.class);
//			Logger.getLogger(UserList.class.getName()).log(Level.ALL,user.getUsername());
//			if(!user.getId().contentEquals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
//				uList.add(user);
//		}

		ListView list = (ListView) findViewById(R.id.list);
		adapter = new CustomAdapter(UserList.this,android.R.layout.simple_list_item_1,uList);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0,
									View arg1, int pos, long arg3)
			{
				startActivity(new Intent(UserList.this,
						Chat.class).putExtra(
						Const.EXTRA_DATA,  uList.get(pos)));
			}
		});



	}

	/**
	 * The Class UserAdapter is the adapter class for User ListView. This
	 * adapter shows the user name and it's only online status for each item.
	 */
	private class UserAdapter extends BaseAdapter
	{

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount()
		{
			return uList.size();
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public ChatUser getItem(int arg0)
		{
			return uList.get(arg0);
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int arg0)
		{
			return arg0;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(int pos, View v, ViewGroup arg2)
		{
			if (v == null)
				v = getLayoutInflater().inflate(R.layout.chat_item, null);

			ChatUser c = getItem(pos);
			TextView lbl = (TextView) v;
			lbl.setText(c.getUsername());
			lbl.setCompoundDrawablesWithIntrinsicBounds(
					c.isOnline() ? R.drawable.ic_online
							: R.drawable.ic_offline, 0, R.drawable.arrow, 0);

			return v;
		}

	}



}
