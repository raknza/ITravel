package com.example.itravel;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Handler;

class LoadDialog extends Thread{
    ProgressDialog progressDialog;
    Handler handler;
    String message = "";
    int count;
    Activity activity;
    LoadDialog(Activity activity){
        handler=new Handler();
        progressDialog = new ProgressDialog(activity);
        this.activity = activity;
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        //progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    }

    @Override
    public void run(){
        count=(int)(Math.random()*10);
        while(true){
            final int assign=count;
            if(count>=100){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                });
                break;
            }
            else{
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.setProgress(assign);
                    }
                });
            }
            count+=(int)(Math.random()*5);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.setMessage("Loading"+message);
                }
            });
            message +=".";
            if(message.length() > 4)
                message = "";
            try {
                Thread.sleep(200);
            }catch (Exception e){e.printStackTrace();}
        }
    }

    public void End(){
        count = 100;
        progressDialog.dismiss();
    }
    public void ReSet(){
        count = 0;
        progressDialog.show();
    }
}
