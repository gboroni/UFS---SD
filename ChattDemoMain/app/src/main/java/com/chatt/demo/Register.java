package com.chatt.demo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;

import com.chatt.demo.custom.CustomActivity;
import com.chatt.demo.model.ChatUser;
import com.chatt.demo.requests.createUserAsync;
import com.chatt.demo.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


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
