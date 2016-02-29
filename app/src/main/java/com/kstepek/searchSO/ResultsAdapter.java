package com.kstepek.searchSO;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.HashMap;


public class ResultsAdapter extends ArrayAdapter<HashMap<String, String>> {
    private ArrayList<HashMap<String, String>> itemsList;
    private Context context;
    private LayoutInflater layoutInflater;
    private int resource;
    private String[] from;
    private ViewHolder viewHolder;
    private int avatarSize;

    public ResultsAdapter(Context context, ArrayList<HashMap<String, String>> data,
                   int resource, String[] from, int screenWidth) {
        super(context, resource, data );
        this.context=context;
        this.resource = resource;
        this.from = from;
        itemsList = data;
        avatarSize = screenWidth/7;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        viewHolder = new ViewHolder();
        View view;

        if(convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(resource, parent, false);
        }
        else
            view = convertView;

        viewHolder.txtTitle = (TextView) view.findViewById(R.id.title);
        viewHolder.txtUsername = (TextView) view.findViewById(R.id.username);
        viewHolder.txtAnswersCount = (TextView) view.findViewById(R.id.answers_count);
        viewHolder.imgAvatar = (ImageView) view.findViewById(R.id.avatar);
        viewHolder.progressBar = (ProgressBar) view.findViewById(R.id.imgProgressBar);

        HashMap<String, String> map = itemsList.get(position);
        viewHolder.txtTitle.setText(map.get(from[0]));
        viewHolder.txtUsername.setText("Asked by: " + map.get(from[1]));
        viewHolder.txtAnswersCount.setText("Answer count: " + map.get(from[2]));

        String avatarUrl = map.get(from[3]);
        if(avatarUrl!= null) {
            viewHolder.progressBar.setVisibility(View.VISIBLE);
            Picasso.with(context)
                    .load(Uri.parse(avatarUrl))
                    .error(R.mipmap.error_img)
                    .resize(avatarSize, avatarSize)
                    .centerCrop()
                    .into(viewHolder.imgAvatar, new ImageLoadedCallback(viewHolder.progressBar) {
                        @Override
                        public void onSuccess(){
                            if (progressBar != null)
                                progressBar.setVisibility(View.GONE);
                        }
                        @Override
                        public void onError() {  }
                    });
        }
        else{
            Bitmap noImgBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.no_img);
            viewHolder.imgAvatar.setImageBitmap(Bitmap.createScaledBitmap(noImgBitmap, avatarSize, avatarSize, false));
            viewHolder.progressBar.setVisibility(View.GONE);
        }

        view.setTag(viewHolder);
        return view;
    }

    static class ViewHolder {
        TextView txtTitle;
        TextView txtUsername;
        TextView txtAnswersCount;
        ImageView imgAvatar;
        ProgressBar progressBar;
    }

    private class ImageLoadedCallback implements Callback {
        ProgressBar progressBar;

        public  ImageLoadedCallback(ProgressBar progressBar){
            this.progressBar = progressBar;
        }

        @Override
        public void onSuccess() { }

        @Override
        public void onError() { }
    }
}
