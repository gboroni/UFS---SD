package com.chatt.ufs;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.chatt.ufs.custom.CustomActivity;
import com.chatt.ufs.requests.createUserAsync;
import com.chatt.ufs.utils.Utils;


/**
 * The Class Register is the Activity class that shows user registration screen
 * that allows user to register itself on Parse server for this Chat app.
 */
public class Register extends CustomActivity
{

	/** The password EditText. */
	private EditText pwd;

	/** The email EditText. */
	private EditText email;

    /** The displayName EditText. */
    private EditText displayName;

    /** Register progress dialog */
    private ProgressDialog registerProgressDlg;

	/* (non-Javadoc)
	 * @see com.chatt.custom.CustomActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);

		setTouchNClick(R.id.btnReg);

		pwd = (EditText) findViewById(R.id.pwd);
		email = (EditText) findViewById(R.id.email);
        displayName = (EditText) findViewById(R.id.displayName);
 	}

	/* (non-Javadoc)
	 * @see com.chatt.custom.CustomActivity#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v)
	{
		super.onClick(v);

        // Extract form fields
		final String password = pwd.getText().toString();
        final String email = this.email.getText().toString();
        final String displayName = this.displayName.getText().toString();

		if ( password.length() == 0 || email.length() == 0 || displayName.length() == 0)
		{
			Utils.showDialog(this, R.string.err_fields_empty);
			return;
		}

        // Register the user
        registerProgressDlg = ProgressDialog.show(this, null,
				getString(R.string.alert_wait));
        new createUserAsync(registerProgressDlg,Register.this).execute(email);
	}
}
