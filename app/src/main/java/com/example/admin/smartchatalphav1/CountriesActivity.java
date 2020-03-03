package com.example.admin.smartchatalphav1;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.admin.smartchatalphav1.Interfaces.ResponseHttpAsyncTask;
import com.example.admin.smartchatalphav1.Network.HttpPostAsyncTask;
import com.example.admin.smartchatalphav1.Network.RequestUrls;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

public class CountriesActivity extends AppCompatActivity implements ResponseHttpAsyncTask {
    LinearLayout countries;
    public float scale;

    private Map<String, String> mSendData = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.countries);
        scale = getResources().getDisplayMetrics().density;

        countries = (LinearLayout) findViewById (R.id.countries);


        //Работа с сетью
        mSendData.clear();
        mSendData.put("method", RequestUrls.METHOD_COUNTRIES);
        HttpPostAsyncTask task = new HttpPostAsyncTask(this, mSendData);
        task.execute(RequestUrls.PROFILE_FIRST_INPUT);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Intent intent = new Intent();
        intent.putExtra("name", "Kazakhstan");
        intent.putExtra("code", "+7");
        setResult(RESULT_OK, intent);
        finish();
    }

    public void addCountry(final JSONObject obj) throws JSONException {
        RelativeLayout rel = new RelativeLayout(this);
        RelativeLayout.LayoutParams relPar = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        relPar.topMargin = getPixels(10,scale);
        rel.setPadding(0,getPixels(15,scale),0,getPixels(15,scale));
        rel.setBackgroundResource(R.drawable.gray_bottom_line);
        rel.setLayoutParams(relPar);

        TextView textView = new TextView(this);
        RelativeLayout.LayoutParams textPar = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        textView.setText(obj.getString("name"));
        textPar.leftMargin = getPixels(7, scale);
        textView.setTextSize(getPixels(10, scale));
        textView.setLayoutParams(textPar);
        rel.addView(textView);

        TextView textView2 = new TextView(this);
        RelativeLayout.LayoutParams textPar2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        textPar2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        textView2.setText(obj.getString("code"));
        textView2.setTextSize(getPixels(10,scale));
        textPar2.rightMargin = getPixels(15,scale);
        textView2.setLayoutParams(textPar2);
        rel.addView(textView2);

        rel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                try {

                    intent.putExtra("name", obj.getString("name"));
                    intent.putExtra("code", obj.getString("code"));
                    setResult(RESULT_OK, intent);
                    finish();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        countries.addView(rel);
    }
    public int getPixels(int dp,  float scale) {
        int pixels = (int) (dp*scale + 0.5f);
        return pixels;
    }

    @Override
    public void afterFinishingHttpPostAsyncTask(JSONObject jsonObject) {
        try {
            int status = Integer.parseInt(jsonObject.getString("status"));

            if (status == 200) {
                JSONArray array = jsonObject.getJSONArray("codes");

                for(int i=0; i<array.length(); i++) {
                    JSONObject o = new JSONObject(array.getString(i));
                    addCountry(o);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private class getCountries extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String url = "http://185.146.2.146:9797/account";
            JSONObject jsonBody;
            String requestBody;
            HttpURLConnection urlConnection;
            try {
                jsonBody = new JSONObject();
                jsonBody.put("method", "countries");
                requestBody = Utils.buildPostParameters(jsonBody);
                urlConnection = (HttpURLConnection) Utils.makeRequest("POST", url, null, "application/json", requestBody);

                InputStream inputStream;
                // get stream
                if (urlConnection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
                    inputStream = urlConnection.getInputStream();
                } else {
                    inputStream = urlConnection.getErrorStream();
                }
                // parse stream
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String temp, response = "";
                while ((temp = bufferedReader.readLine()) != null) {
                    response += temp;
                }

                return response;
            } catch (JSONException | IOException e) {
                e.printStackTrace();
                return e.toString();
            } finally {

            }
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            try {
                JSONObject resp = new JSONObject(response);
                JSONArray array = resp.getJSONArray("codes");

                for(int i=0; i<array.length(); i++) {
                    JSONObject o = new JSONObject(array.getString(i));
                    addCountry(o);
                }

            } catch (JSONException e) {
                show("не получилось сделать JSONObject");
                e.printStackTrace();
            }
        }
    }
    private void show(String s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Данные: ")
                .setMessage(s)
                .setCancelable(false)
                .setNegativeButton("ОК",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}