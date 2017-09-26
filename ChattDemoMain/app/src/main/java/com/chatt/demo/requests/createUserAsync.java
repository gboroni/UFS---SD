package com.chatt.demo.requests;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.chatt.demo.Login;
import com.chatt.demo.UserList;
import com.chatt.demo.utils.Singleton;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by guilhermeboroni on 26/09/2017.
 */

public class createUserAsync  extends AsyncTask<String, Void, String>{
    ProgressDialog dialog;
    Context ctx;

    public createUserAsync(ProgressDialog dialog, Context ctx){
        this.dialog = dialog;
        this.ctx = ctx;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            Singleton.getInstance().getChannel().queueDeclare(params[0], false, false, false, null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return "";

    }


    @Override
    protected void onPostExecute(String result) {
        if (dialog.isShowing())
            dialog.dismiss();
        ctx.startActivity(new Intent(ctx, UserList.class));

    }

}


