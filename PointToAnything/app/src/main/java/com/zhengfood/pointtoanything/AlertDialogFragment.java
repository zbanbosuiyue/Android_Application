package com.zhengfood.pointtoanything;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;


/**
 * Created by robert on 4/13/15.
 */
public class AlertDialogFragment extends DialogFragment{

    private String myTitle;
    private String myMessage;
    private Boolean myStatus;

    public void setAttribute(FragmentManager FM, String title, String message,
            boolean status){
        this.myTitle = title;
        this.myMessage = message;
        this.myStatus = status;
        this.show(FM,title);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(myTitle).setMessage(myMessage)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //fire missiles
                    }
                })
                .setIcon((myStatus)? R.drawable.success : R.drawable.fail);

        return builder.create();

    }

}
