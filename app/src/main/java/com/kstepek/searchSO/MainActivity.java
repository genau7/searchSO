package com.kstepek.searchSO;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.droidparts.widget.ClearableEditText;

public class MainActivity extends AppCompatActivity implements NetworkStateReceiver.NetworkStateReceiverListener {
    private TextView txtNetworkError;
    private ClearableEditText txtQuery;
    private ImageButton btnSearch;
    private LinearLayout searchLayout;
    public static final String ID_QUERY = "Query string";

    private final NetworkStateReceiver networkStateReceiver = new NetworkStateReceiver();
    private ResultsFragment resultFragment = null;

    @Override
    public void onNetworkAvailable(){
        txtNetworkError.setVisibility(View.GONE);
        btnSearch.setEnabled(true);
        searchLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onNetworkUnavailable(){
        txtNetworkError.setVisibility(View.VISIBLE);
        btnSearch.setEnabled(false);
        searchLayout.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtNetworkError = (TextView) findViewById(R.id.networkError);
        btnSearch = (ImageButton) findViewById(R.id.btnSearch);
        searchLayout = (LinearLayout) findViewById(R.id.searchLayout);
        txtQuery = (ClearableEditText) findViewById(R.id.query);
    }

    @Override
    public void onResume(){
        super.onResume();
        networkStateReceiver.addListener(this);
        registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    public void btnSearchOnClick(View view) {
        doSearch();
    }

    private void  doSearch(){
        String query = txtQuery.getText().toString();
        if(networkStateReceiver.networkOK()){
            hideKeyboard();
            if(resultFragment == null) {
                Bundle args = new Bundle();
                args.putString(ID_QUERY, query);
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                resultFragment = new ResultsFragment();
                resultFragment.setArguments(args);
                transaction
                        .add(R.id.resultsFrame, resultFragment, "RESULTS")
                        .addToBackStack(null)
                        .commit();
            }
            else {
                resultFragment.getResults(query);
            }
        }
    }

    private void hideKeyboard(){
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
    @Override
    public void onPause(){
        super.onPause();
        unregisterReceiver(networkStateReceiver);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if ((keyCode == KeyEvent.KEYCODE_BACK))
            txtQuery.setText("");
        return super.onKeyDown(keyCode, event);
    }
}
