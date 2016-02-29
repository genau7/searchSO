package com.kstepek.searchSO;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

public class ResultsFragment extends ListFragment implements AdapterView.OnItemClickListener {

    // ----------------------- API related -----------------------
    private final String API_URL = "http://api.stackexchange.com/2.2/search?order=desc&sort=activity&intitle=";
    private final String API_site = "stackoverflow";
    private static final String TAG_TITLE = "title";
    private static final String TAG_COUNT = "answer_count";
    private static final String TAG_USERNAME = "display_name";
    private static final String TAG_OWNER = "owner";
    private static final String TAG_PHOTO = "profile_image";
    private static final String TAG_ITEMS = "items";
    private static final String TAG_LINK = "link";
    private static final String RESULTS = "results";

    //------------------ UI elements ---------------------------
    private ProgressBar progressBar;
    private TextView txtNoResults;

    //------------------ Others --------------------------------
    private ArrayList<HashMap<String, String>> itemsList;
    private ResultsAdapter listAdapter;
    private Point screenSize;
    private String searchQuery = "";

    public ResultsFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.results_fragment, container, false);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        txtNoResults = (TextView) view.findViewById(R.id.noResults);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setOnItemClickListener(this);
        calcScreenSize();

        if(savedInstanceState != null )
            itemsList = (ArrayList<HashMap<String, String>>)
                    savedInstanceState.getSerializable(RESULTS);
        else
            itemsList = new ArrayList<HashMap<String, String>>();

        listAdapter = new ResultsAdapter(getActivity(), itemsList, R.layout.results_list,
                new String[]{TAG_TITLE, TAG_USERNAME, TAG_COUNT, TAG_PHOTO, TAG_LINK},screenSize.x);
        setListAdapter(listAdapter);

        getResults(getArguments().getString(MainActivity.ID_QUERY));
    }

    public void getResults(String query){
        txtNoResults.setVisibility(View.GONE);
        searchQuery = query;
        if (query.length() > 0)
            new RetrieveSearchResult().execute();

    }

    private void calcScreenSize(){
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        screenSize = new Point();
        display.getSize(screenSize);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String link = itemsList.get(position).get(TAG_LINK);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        startActivity(browserIntent);
    }

    private class RetrieveSearchResult extends AsyncTask<Void, Void, JSONObject> {

        @Override
        protected void onPreExecute() {
            itemsList.clear();
            listAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.VISIBLE);
            try{
                searchQuery = URLEncoder.encode(searchQuery, "UTF-8");
            }catch (Exception e){
                Log.e("ERROR", e.getMessage(), e);
            }
        }

        @Override
        protected JSONObject doInBackground(Void... args) {
            try {
                URL url = new URL(API_URL + searchQuery + "&site=" + API_site);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                Integer responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                        bufferedReader.close();
                        String responseString = stringBuilder.toString();
                        JSONObject response = new JSONObject(responseString);
                        return response;
                    } finally {
                        urlConnection.disconnect();
                    }
                } else {
                    return null;
                }
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject response) {
            if (response == null) {
                Log.e("ERROR", "RetrieveSearchResult.onPostExecute error: response is null");
            }
            else {
                progressBar.setVisibility(View.GONE);

                try {
                    JSONArray items = response.getJSONArray(TAG_ITEMS);
                    if (items.length() > 1) {
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);
                            JSONObject owner = item.getJSONObject(TAG_OWNER);

                            String title = Html.fromHtml(item.getString(TAG_TITLE)).toString();
                            String count = item.getString(TAG_COUNT);
                            String username = owner.getString(TAG_USERNAME);
                            String link = item.getString(TAG_LINK);

                            String avatarString = null;
                            if (owner.has(TAG_PHOTO))
                                avatarString = item.getJSONObject(TAG_OWNER).getString(TAG_PHOTO);

                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put(TAG_TITLE, title);
                            map.put(TAG_COUNT, count);
                            map.put(TAG_USERNAME, username);
                            map.put(TAG_PHOTO, avatarString);
                            map.put(TAG_LINK, link);
                            itemsList.add(map);
                        }

                        getListView().invalidateViews();

                    } else {
                        txtNoResults.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(RESULTS, itemsList);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int pos = getListView().getFirstVisiblePosition();

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            listAdapter = new ResultsAdapter(getActivity(), itemsList, R.layout.results_list_land,
                    new String[]{TAG_TITLE, TAG_USERNAME, TAG_COUNT, TAG_PHOTO, TAG_LINK}, screenSize.x);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            listAdapter = new ResultsAdapter(getActivity(), itemsList, R.layout.results_list,
                    new String[]{TAG_TITLE, TAG_USERNAME, TAG_COUNT, TAG_PHOTO, TAG_LINK}, screenSize.x);
        }
        setListAdapter(listAdapter);
        getListView().setSelection(pos);

    }

}
