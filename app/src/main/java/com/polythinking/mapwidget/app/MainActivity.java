package com.polythinking.mapwidget.app;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;


import model.SiriResponse;
import network.RestApis;
import org.json.JSONObject;

import java.io.IOException;


public class MainActivity extends Activity {

    RequestQueue mRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRequestQueue = Volley.newRequestQueue(this);

        String url = RestApis.Siri.stopMonitoring(
                RestApis.SAMPLE_STOP_CODE,
                RestApis.SAMPLE_LINE_REF).toString();
        Log.d("jing", url);
        JsonObjectRequest request = new JsonObjectRequest(
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            SiriResponse siriResponse = SiriResponse.read(response);
                            Log.d("jing", "pass");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.d("jing", response.toString());
                    }
                },
                null);
        mRequestQueue.add(request);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
