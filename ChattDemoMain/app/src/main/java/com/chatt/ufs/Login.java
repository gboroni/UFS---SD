package com.chatt.ufs;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.chatt.ufs.custom.CustomActivity;
import com.chatt.ufs.requests.createUserAsync;
import com.chatt.ufs.utils.Singleton;
import com.chatt.ufs.utils.Utils;


/**
 * The Class Login is an Activity class that shows the login screen to users.
 * The current implementation simply includes the options for Login and button
 * for Register. On login button click, it sends the Login details to Parse
 * server to verify user.
 */
public class Login extends CustomActivity
{

	/** The username edittext. */
	private EditText user;

    /** Login progress dialog */
    private ProgressDialog loginProgressDlg;

    /* (non-Javadoc)
	 * @see com.chatt.custom.CustomActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		setTouchNClick(R.id.btnLogin);
		setTouchNClick(R.id.btnReg);

		user = (EditText) findViewById(R.id.user);

	}

	/* (non-Javadoc)
	 * @see com.chatt.custom.CustomActivity#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v)
	{
		super.onClick(v);
		if (v.getId() == R.id.btnReg)
		{
			startActivityForResult(new Intent(this, Register.class), 10);
		}
		else
		{
			// Extract form fields
			String user = this.user.getText().toString();
			if (user.length() == 0 )
			{
				Utils.showDialog(this, R.string.err_fields_empty);
				return;
			}

            loginProgressDlg = ProgressDialog.show(this, null,
                    getString(R.string.alert_wait));

			Singleton.getInstance().setUser(user);
			new createUserAsync(loginProgressDlg,Login.this).execute(user);

		}
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 10 && resultCode == RESULT_OK)
			finish();
	}
}
