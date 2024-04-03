package com.mobile_computing;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import org.json.JSONException;
import org.json.JSONObject;

public class ResultDisplayActivity extends AppCompatActivity {

    private NetworkImageView Image;

    private TextView Title, Date, Description;

    private JSONObject Result;

    private ImageButton Back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_display);

        Image=findViewById(R.id.res_img);
        Title=findViewById(R.id.res_title);
        Date=findViewById(R.id.res_date);
        Description=findViewById(R.id.res_description);
        Back=findViewById(R.id.res_back_button);

        try {
            ImageLoader imgLoad = VolleySingleton.getInstance(this).getImageLoader();
            Result = new JSONObject(getIntent().getExtras().getString("BookResult"));
            Image.setImageUrl(Result.getString("image"),imgLoad);
            Title.setText(Result.getString("title"));
            Date.setText(Result.getString("date"));
            Description.setText(Result.getString("text"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }


}