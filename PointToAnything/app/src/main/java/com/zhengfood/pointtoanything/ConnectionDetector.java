package com.zhengfood.pointtoanything;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by robert on 4/13/15.
 */
public class ConnectionDetector {

    private Context myContext;

    public ConnectionDetector(Context context){
        this.myContext = context;
    }

    public boolean isConnectingToInternet(){
        ConnectivityManager connectivity = (ConnectivityManager)
                myContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity != null){
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null){
                for (int i=0; i < info.length;i++){
                    if (info[i].getState() == NetworkInfo.State.CONNECTED){
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
