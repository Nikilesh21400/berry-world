package com.mobile_computing;



import static android.content.Context.MODE_PRIVATE;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.Proxy;
import java.util.ArrayList;

/**
 * Refer to http://www.truiton.com/2015/03/android-cardview-example/ for more details
 */

public class DatumAdapter extends RecyclerView.Adapter<DatumAdapter.DataObjectHolder> {
    private static String LOG_TAG = "DATUM ADAPTER";
    private ArrayList<Datum> m_data;
    private ImageLoader imgLoad;
    private static DatumClickListener m_clickListener;
    private static Context m_context;

    private  SharedPreferences SPFavourites;
    private SharedPreferences SPBooks;

    // Constructors
    public DatumAdapter(Context con) {
        m_context = con;
        m_data    = new ArrayList<Datum>();

        imgLoad = VolleySingleton.getInstance(con).getImageLoader();
    }

    public DatumAdapter(Context con, ArrayList<Datum> data) {
        m_context = con;
        m_data    = data;

        imgLoad = VolleySingleton.getInstance(con).getImageLoader();
    }

    // Responsible for creating the individual view per Datum
    public static class DataObjectHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title;
        TextView date;
        NetworkImageView img;

        TextView ind;

        ImageButton star;

        public DataObjectHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            date  = (TextView) itemView.findViewById(R.id.date);
            img   = (NetworkImageView) itemView.findViewById(R.id.img);
            ind = (TextView) itemView.findViewById(R.id.ind);
            star = (ImageButton) itemView.findViewById(R.id.starButton);
            Log.i(LOG_TAG, "Adding Listener");
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            m_clickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    // Sets the callback function to handle clicking on individual datum
    public void setOnItemClickListener(DatumClickListener clickListener) {
        this.m_clickListener = clickListener;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card, parent, false);
        DataObjectHolder dataObjectHolder = new DataObjectHolder(view);

        ImageButton starButton=view.findViewById(R.id.starButton);
        SharedPreferences SPFavourites = m_context.getSharedPreferences("Favourites", MODE_PRIVATE);


        /*// Highlight Favourites during Startup
        try {
            //starButton.setBackgroundResource(R.drawable.favourites_blue_star);
            JSONArray Favourites = new JSONArray(SPFavourites.getString("Favourites","[]"));
            for(int i=0;i<Favourites.length();i++){
                //System.out.println("Fav ID - "+Favourites.getJSONObject(i).getString("id")+" Data ID - "+dataObjectHolder.ind.getText());
                if(Favourites.getJSONObject(i).getString("id").equals(dataObjectHolder.ind.getText().toString())){
                    starButton.setBackgroundResource(R.drawable.favourites_blue_star);
                    System.out.println(Favourites.getJSONObject(i).toString());
                    break;
                }
            }
        } catch (JSONException e) {
            System.out.println(e);
        }*/

        starButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(m_context, ""+dataObjectHolder.JsonObjPosition.getText(), Toast.LENGTH_SHORT).show();
                addFavourites(starButton, dataObjectHolder.ind.getText()+"");
            }
        });

        //System.out.println("Data ID - "+ dataObjectHolder.title.getText());
        return dataObjectHolder;
    }

    private void addFavourites(ImageButton starButton, String index) {

        SPFavourites = m_context.getSharedPreferences("Favourites", MODE_PRIVATE);
        SPBooks = m_context.getSharedPreferences("Books", MODE_PRIVATE);

        try {
            //System.out.println(SPBooks.getString("Books","[]"));
            //System.out.println(SPFavourites.getString("Favourites","[]"));
            JSONArray Books = new JSONArray(SPBooks.getString("Books","[]"));
            JSONArray Favourites = new JSONArray(SPFavourites.getString("Favourites","[]"));

            //JSONArray Books = new JSONArray((new Gson()).fromJson(SPBooks.getString("Books",""), JSONArray.class));
            //JSONArray Favourites = new JSONArray((new Gson()).fromJson(SPBooks.getString("Favourites",""), JSONArray.class));

            boolean AlreadyFav=false;
            for(int i=0;i<Favourites.length();i++){
                if(Favourites.getJSONObject(i).getString("id").equals(index)){
                    AlreadyFav=true;
                    break;
                }
            }
            if(!AlreadyFav) {
                AlertDialog.Builder builder = new AlertDialog.Builder(m_context);
                builder.setMessage("Add to Favourites?")
                        .setCancelable(false)
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                try {
                                    starButton.setBackgroundResource(R.drawable.favourites_blue_star);
                                    System.out.println("Index - "+index);

                                    for(int i=0;i<Books.length();i++){
                                        if(Books.getJSONObject(i).getString("id").equals(index)){
                                            JSONObject Fav = new JSONObject(Books.getString(i));
                                            Favourites.put(Fav);
                                            SPFavourites.edit().putString("Favourites", Favourites.toString()).apply();
                                            Toast.makeText(m_context, "Added to Favourites", Toast.LENGTH_SHORT).show();
                                            break;
                                        }
                                    }

                                    System.out.println(SPBooks.getString("Books","[]"));
                                    System.out.println(SPFavourites.getString("Favourites","[]"));
                                    dialog.dismiss();
                                }catch (Exception e){
                                    System.out.println(e);
                                }
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        } catch (JSONException e) {
            System.out.println(e);
        }


    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {
        holder.title.setText(m_data.get(position).title());
        holder.date.setText(m_data.get(position).date());
        holder.img.setImageUrl(m_data.get(position).imageUrl(), imgLoad);
        holder.ind.setText(m_data.get(position).id()+"");

        // Highlight Favourites during Startup
        SharedPreferences SPFavourites = m_context.getSharedPreferences("Favourites", MODE_PRIVATE);
        try {
            //starButton.setBackgroundResource(R.drawable.favourites_blue_star);
            JSONArray Favourites = new JSONArray(SPFavourites.getString("Favourites","[]"));
            for(int i=0;i<Favourites.length();i++){
                //System.out.println("Fav ID - "+Favourites.getJSONObject(i).getString("id")+" Data ID - "+dataObjectHolder.ind.getText());
                if(Favourites.getJSONObject(i).getString("id").equals(m_data.get(position).id()+"")){
                    holder.star.setBackgroundResource(R.drawable.favourites_blue_star);
                    System.out.println(Favourites.getJSONObject(i).toString());
                    break;
                }else{
                    holder.star.setBackgroundResource(R.drawable.favourites_transparent_star);
                }
            }
        } catch (JSONException e) {
            System.out.println(e);
        }

    }

    public void addItem(Datum datum, int index) {
        m_data.add(index, datum);
        notifyItemInserted(index);
    }

    public void deleteItem(int index) {
        m_data.remove(index);
        notifyItemRemoved(index);
    }


    public void clear() {
        m_data.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return m_data.size();
    }

    // Get a specific datum
    public Datum getItem(int position) {
        if (position > -1 && position < m_data.size())
            return m_data.get(position);
        else
            return null;
    }

    public interface DatumClickListener {
        public void onItemClick(int position, View v);
    }

    public void updateSPFavourites(SharedPreferences SPFavourites){
        this.SPFavourites=SPFavourites;
    }
}