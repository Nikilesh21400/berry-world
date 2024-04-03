package com.mobile_computing;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FavouriteDisplayActivity extends AppCompatActivity {

    private final String LOG_TAG = "MOBILE COMPUTING";

    private RecyclerView m_recView;
    private RecyclerView.Adapter m_adapter;
    private RecyclerView.LayoutManager m_layout;
    private TextView NoBooks;

    private ProgressBar progressBar;

    private JSONArray resultJsonArray;

    private ImageButton Back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite_display);

        NoBooks = findViewById(R.id.FavSearchNoBooks);
        progressBar = findViewById(R.id.FavSearchProgressBar);
        Back=findViewById(R.id.fav_back_button);


        // Setup the different views
        m_recView = (RecyclerView) findViewById(R.id.Favrecycler_view);
        m_layout  = new LinearLayoutManager(this);
        m_adapter = new FavDatumAdapter(this);

        m_recView.setHasFixedSize(true);
        m_recView.setLayoutManager(m_layout);
        m_recView.setAdapter(m_adapter);

        final Context self = this;
        ((FavDatumAdapter) m_adapter).setOnItemClickListener(new FavDatumAdapter.DatumClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                //String msg = "Item with ID: " + ((FavDatumAdapter) m_adapter).getItem(position).id();
                //Toast toast = Toast.makeText(self, msg, Toast.LENGTH_SHORT);
                //toast.show();

                try {
                    //Toast.makeText(FavouriteDisplayActivity.this, position+"-"+((FavDatumAdapter) m_adapter).getItem(position).id(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(FavouriteDisplayActivity.this, ResultDisplayActivity.class);
                    //intent.putExtra("BookResult", resultJsonArray.getJSONObject(((FavDatumAdapter) m_adapter).getItem(position).id()).toString());
                    intent.putExtra("BookResult", resultJsonArray.getJSONObject(position).toString());
                    startActivity(intent);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        });

        displayFavourites();

        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                SharedPreferences sharedPreferences = getSharedPreferences("Books", MODE_PRIVATE);
                Intent intent = new Intent(FavouriteDisplayActivity.this, API_Search.class);
                intent.putExtra("BookResult", sharedPreferences.getString("Books","[]"));
                intent.putExtra("Search", sharedPreferences.getString("Search",""));
                startActivity(intent);
            }
        };
        getOnBackPressedDispatcher().addCallback(this,onBackPressedCallback);

        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void displayFavourites() {

        ((FavDatumAdapter) m_adapter).clear();
        m_recView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        NoBooks.setVisibility(View.INVISIBLE);



        try {

            m_recView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);

            SharedPreferences SPFavourites = getSharedPreferences("Favourites", MODE_PRIVATE);

            resultJsonArray = new JSONArray(SPFavourites.getString("Favourites","[]"));

            SharedPreferences sharedPreferences = getSharedPreferences("Books", MODE_PRIVATE);
            sharedPreferences.edit().putString("Books", resultJsonArray.toString()).apply();

            // Iterate over all of the JSON responses
            JSONObject obj = null;

            if(resultJsonArray.length()==0){
                NoBooks.setVisibility(View.VISIBLE);
            }
            for (int i = 0; i < resultJsonArray.length(); ++i) {
                // Get the properties
                obj = resultJsonArray.getJSONObject(i);
                Log.d(LOG_TAG,obj.getString("image"));
                Datum datum = new Datum(obj.getInt("id"), obj.getString("title"),
                        obj.getString("date"), obj.getString("text"), obj.getString("image"));

                // Add the item to the view
                ((FavDatumAdapter) m_adapter).addItem(datum, m_adapter.getItemCount());
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }



    }

}