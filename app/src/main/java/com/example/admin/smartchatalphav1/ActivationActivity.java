package com.example.admin.smartchatalphav1;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.admin.smartchatalphav1.Interfaces.ResponseHttpAsyncTask;
import com.example.admin.smartchatalphav1.Network.HttpPostAsyncTask;
import com.example.admin.smartchatalphav1.Network.RequestUrls;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

public class ActivationActivity extends AppCompatActivity implements View.OnClickListener, ResponseHttpAsyncTask {
    TextView error, codeError, repeat, title;
    EditText code;
    Button confirm;
    String phone;
    boolean timerIsFinished;

    private static final String TAG = "ActivationActivity";

    private Map<String, String> mSendData = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activation);

        phone = QueryPreferences.getStoredPhone(getApplicationContext());

        error = (TextView) findViewById(R.id.error);
        codeError = (TextView) findViewById(R.id.codeError);
        repeat = (TextView) findViewById(R.id.repeat);
        title = (TextView) findViewById(R.id.title);
        code = (EditText) findViewById(R.id.phone_activation);
        confirm = (Button) findViewById(R.id.confirm);

        mSendData.clear();

        title.setText("Мы отправили код активации на номер " + phone);

        try {
            startTimer();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean validate() {
        boolean result = true;

        code.setBackgroundResource(R.drawable.blue_bottom_line);
        codeError.setVisibility(View.INVISIBLE);
        error.setVisibility(View.INVISIBLE);

        if(code.length() == 0){
            result = false;
            code.setBackgroundResource(R.drawable.red_bottom_line);
            codeError.setText("Введите код");
            codeError.setVisibility(View.VISIBLE);
        }else if(code.length()<4) {
            result = false;
            code.setBackgroundResource(R.drawable.red_bottom_line);
            codeError.setText("Некорректный код");
            codeError.setVisibility(View.VISIBLE);
        }
        return result;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, LoginActivity.class);
        this.finish();
        startActivity(intent);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.confirm:
                boolean validated = validate();
                if(validated) {
                    mSendData.clear();
                    mSendData.put("phone", "+" + phone.substring(1));
                    mSendData.put("code", code.getText().toString());
                    mSendData.put("method", RequestUrls.METHOD_VERIFY);
                    HttpPostAsyncTask task = new HttpPostAsyncTask(this, mSendData);
                    task.execute(RequestUrls.PROFILE_FIRST_INPUT);
                }
                break;
            case R.id.repeat:
                if(timerIsFinished || true) {
                    Repeat repeat = new Repeat();
                    repeat.execute();
                }
                break;
        }
    }
    public void startTimer() throws InterruptedException {
        timerIsFinished = false;
//        try {
//
//
//            Timer timer = new Timer();
//            timer.schedule(new TimerTask() {
//                @Override
//                public void run() {
//                    show("asdfsadfasd");
//                }
//            }, 1000);
//
//        } catch (Exception e) {
//            show(e.toString());
//        }
        repeat.setText("Отправить повторно");
        timerIsFinished = true;
    }

    @Override
    public void afterFinishingHttpPostAsyncTask(JSONObject jsonObject) {
        if (jsonObject != null) {
            Log.i(TAG, jsonObject.toString());

            Intent intent = new Intent(this, MainActivity.class);
            finish();
            startActivity(intent);
        } else {
            error.setVisibility(View.VISIBLE);
            error.setText("Неверный код");
        }
    }

    private class Activate extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String url = "/account";
            JSONObject jsonBody;
            String requestBody;
            HttpURLConnection urlConnection;
            try {
                jsonBody = new JSONObject();
                jsonBody.put("method", "verify");
                jsonBody.put("phone",phone);
                jsonBody.put("code", code.getText().toString());
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
                JSONObject res = new JSONObject(response);
                Intent intent;
                switch (res.getString("status")) {
                    case "200":
//                        ActivationActivity.this.finish();
//                        intent = new Intent(ActivationActivity.this, Profile.class);
//                        startActivity(intent);
                        break;
                    case "417":
                        error.setText("Неверный код");
                        error.setVisibility(View.VISIBLE);
                        break;
                }
            } catch (JSONException e) {
                show("не получилось сделать JSONObject");
                e.printStackTrace();
            }
        }
    }
    private class Repeat extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String url =  "/account";
            JSONObject jsonBody;
            String requestBody;
            HttpURLConnection urlConnection;
            try {
                jsonBody = new JSONObject();
                jsonBody.put("method", RequestUrls.METHOD_SMS);
                jsonBody.put("phone", phone);
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

                Log.i(TAG, response);
                return response;
            } catch (JSONException | IOException e) {
                e.printStackTrace();
                Log.e(TAG, e.toString());
                return e.toString();
            } finally {

            }
        }

        @SuppressLint("ResourceAsColor")
        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            try {
                JSONObject res = new JSONObject(response);
                Intent intent;
                switch (res.getString("status")) {
                    case "200":
                        repeat.setText("Смс успешно отправлено");
                        repeat.setTextColor(R.color.green);
                        ActivationActivity.this.finish();
                        intent = new Intent(ActivationActivity.this, MainActivity.class);
                        startActivity(intent);
                        break;
                    case "403":
                        show("Невозможно отправить смс.");
                        break;
                }
            } catch (JSONException e) {
                show("не получилось сделать JSONObject");
                e.printStackTrace();
            }
        }
    }
}