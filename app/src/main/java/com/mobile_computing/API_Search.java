package com.mobile_computing;

import org.json.*;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.RequestQueue;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import androidx.activity.OnBackPressedCallback;

public class API_Search extends Activity {

    //private final String API_URL = "http://192.168.1.81:8080/api/s/";

    private final String API_URL = "https://c573-2600-381-cb60-3bea-71d5-92f-4374-235b.ngrok-free.app/api/s/";

    private final String LOG_TAG = "MOBILE COMPUTING";

    private RecyclerView m_recView;
    private RecyclerView.Adapter m_adapter;
    private RecyclerView.LayoutManager m_layout;
    private RequestQueue requestQ;

    private TextView NoBooks;

    private ProgressBar progressBar;

    private EditText keywordEditText;

    private JSONArray resultJsonArray;

    private Button displayFavourites;


    // This is run when the intent is created
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        NoBooks = findViewById(R.id.APISearchNoBooks);
        progressBar = findViewById(R.id.APISearchProgressBar);
        keywordEditText = (EditText) findViewById(R.id.keyWordEditText);
        displayFavourites = findViewById(R.id.favButton);

        if(getIntent().hasExtra("Search")) {
            keywordEditText.setText(getIntent().getExtras().getString("Search", ""));
        }else{
            SharedPreferences SPBooks = getSharedPreferences("Books", MODE_PRIVATE);
            SharedPreferences SPFavourites = getSharedPreferences("Favourites", MODE_PRIVATE);
            SPBooks.edit().putString("Books", "[]").apply();
            SPFavourites.edit().putString("Favourites", "[]").apply();
        }

        // Setup the different views
        m_recView = (RecyclerView) findViewById(R.id.recycler_view);
        m_layout  = new LinearLayoutManager(this);
        m_adapter = new DatumAdapter(this);

        m_recView.setHasFixedSize(true);
        m_recView.setLayoutManager(m_layout);
        m_recView.setAdapter(m_adapter);

        keywordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    keywordEditText.clearFocus();
                    InputMethodManager in = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(keywordEditText.getWindowToken(), 0);
                    search();
                    return true;
                }
                return false;
            }
        });


        displayFavourites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(API_Search.this,FavouriteDisplayActivity.class));
            }
        });


        final Context self = this;
        ((DatumAdapter) m_adapter).setOnItemClickListener(new DatumAdapter.DatumClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                //String msg = "Item with ID: " + ((DatumAdapter) m_adapter).getItem(position).id();
                //Toast toast = Toast.makeText(self, msg, Toast.LENGTH_SHORT);
                //toast.show();

                try {
                    Intent intent = new Intent(API_Search.this, ResultDisplayActivity.class);
                    //intent.putExtra("BookResult", resultJsonArray.getJSONObject(((DatumAdapter) m_adapter).getItem(position).id()).toString());
                    intent.putExtra("BookResult", resultJsonArray.getJSONObject(position).toString());
                    startActivity(intent);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // Setup the button
        final Button searchButton = (Button) findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((DatumAdapter) m_adapter).clear();
                search();
            }
        });

        // Setup the request queue for network requests (using Volley)
        requestQ = VolleySingleton.getInstance(this.getApplicationContext()).getRequestQueue();
        search();


    }

    // Initiate a search
    private void search() {
        // Make a request
        ((DatumAdapter) m_adapter).clear();
        m_recView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        NoBooks.setVisibility(View.INVISIBLE);

        final Context self = this;
        StringRequest res = new StringRequest(Request.Method.GET, API_URL + keywordEditText.getText(),
            new Response.Listener<String>() {
                private ImageLoader imgLoad = VolleySingleton.getInstance(self).getImageLoader();

                // On response
                @Override
                public void onResponse(String response) {
                    try {
                        // Get the response and convert it to JSON
                        m_recView.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);

                        resultJsonArray = (new JSONObject(response)).getJSONArray("results");

                        //Toast.makeText(self, ""+resultJsonArray.length(), Toast.LENGTH_SHORT).show();

                        for(int i=0;i<resultJsonArray.length();i++) {
                            System.out.println(resultJsonArray.getJSONObject(i).toString());
                        }

                        SharedPreferences sharedPreferences = getSharedPreferences("Books", MODE_PRIVATE);
                        sharedPreferences.edit().putString("Books", resultJsonArray.toString()).apply();
                        sharedPreferences.edit().putString("Search", keywordEditText.getText().toString()).apply();



                        if(resultJsonArray.length()==0){
                            NoBooks.setVisibility(View.VISIBLE);
                        }

                        // Adding Books to Recycler View
                        addBookstoRecyclerView(resultJsonArray, (DatumAdapter) m_adapter);


                    } catch (Exception e) {
                        // Print to console on error
                        //Toast.makeText(self, ""+e, LENGTH_SHORT).show();
                        Log.d(LOG_TAG,e+"");
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                // Print to console on Error
                @Override
                public void onErrorResponse(VolleyError err) {

                    m_recView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                    NoBooks.setVisibility(View.VISIBLE);
                    NoBooks.setText("Unexpected Error! Please try again");
                    err.printStackTrace();
                }
            }
        );

        // Start the request
        requestQ.add(res);
    }


    public void addBookstoRecyclerView(JSONArray resultJsonArray, DatumAdapter m_adapter){
        ((DatumAdapter) m_adapter).clear();
        try {
            for (int i = 0; i < resultJsonArray.length(); ++i) {
                // Get the properties
                // Iterate over all of the JSON responses
                JSONObject obj = null;
                obj = resultJsonArray.getJSONObject(i);
                Log.d(LOG_TAG, obj.getString("image"));
                Datum datum = new Datum(obj.getInt("id"), obj.getString("title"),
                        obj.getString("date"), obj.getString("text"), obj.getString("image"));

                // Add the item to the view
                ((DatumAdapter) m_adapter).addItem(datum, m_adapter.getItemCount());

            }
        }catch (Exception e){
            System.out.println(e);
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        //((DatumAdapter) m_adapter).notifyDataSetChanged();
        //Toast.makeText(this, "onResume Called", Toast.LENGTH_SHORT).show();

        // Update Favourites in SharedPreferences
        //SharedPreferences SPFavourites = getSharedPreferences("Favourites", MODE_PRIVATE);
        //((DatumAdapter) m_adapter).updateSPFavourites(SPFavourites);

        //((DatumAdapter) m_adapter).clear();

        //progressBar.setVisibility(View.VISIBLE);
        //addBookstoRecyclerView(resultJsonArray, (DatumAdapter) m_adapter);
        //progressBar.setVisibility(View.INVISIBLE);

        //Intent intent = new Intent(API_Search.this, API_Search.class);
        //intent.putExtra("BookResult", resultJsonArray.toString());
        //startActivity(intent);

        //for(int i=0;i<resultJsonArray.length();i++) {
        //    View view = ((DatumAdapter) m_adapter).getItem(i);
        //
        //}

        //((DatumAdapter) m_adapter).
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
        finish();
    }
}