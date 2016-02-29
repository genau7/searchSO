package com.kstepek.searchSO;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class NetworkStateReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkStateReceiver";
    protected List<NetworkStateReceiverListener> listeners;
    protected Boolean connected;

    public NetworkStateReceiver() {
        listeners = new ArrayList<NetworkStateReceiverListener>();
        connected = false;
    }

    boolean networkOK(){
        return connected;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.d(TAG, "Network connectivity change");
        if(intent == null || intent.getExtras() == null)
            return;

        ConnectivityManager connectivityManager
                = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting()) {
            Log.i(TAG, "Network " + activeNetworkInfo.getTypeName() + " connected");
            connected = true;
        }
        else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
            Log.d(TAG, "There's no network connectivity");
            connected = false;
        }

        notifyAll_state();
    }

    private void notifyAll_state() {
        for(NetworkStateReceiverListener listener : listeners)
            notify_state(listener);
    }

    private void notify_state(NetworkStateReceiverListener listener){
        if(connected == null || listener == null)
            return;

        if(connected == true)
            listener.onNetworkAvailable();
        else
            listener.onNetworkUnavailable();
    }

    public interface NetworkStateReceiverListener {
        public void onNetworkAvailable();
        public void onNetworkUnavailable();
    }

    public void addListener(NetworkStateReceiverListener l) {
        listeners.add(l);
        notify_state(l);
    }

    public void removeListener(NetworkStateReceiverListener l) {
        listeners.remove(l);
    }

}