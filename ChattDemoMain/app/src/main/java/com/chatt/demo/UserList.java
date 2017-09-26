package com.chatt.demo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.chatt.demo.custom.CustomActivity;
import com.chatt.demo.model.ChatUser;
import com.chatt.demo.utils.Const;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


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

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_list);

		getActionBar().setDisplayHomeAsUpEnabled(false);

		updateUserStatus(true);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onDestroy()
	 */
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		updateUserStatus(false);
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

        uList.add(new ChatUser("id","Edgar","email",true, new ArrayList<String>()));
		ListView list = (ListView) findViewById(R.id.list);
        ArrayAdapter<ChatUser> adapter = new ArrayAdapter<ChatUser>(this,
                android.R.layout.simple_list_item_1, uList);
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
