package com.example.admin.smartchatalphav1;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberFormattingTextWatcher;
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

public class RestoreActivity extends AppCompatActivity implements View.OnClickListener, ResponseHttpAsyncTask {
    TextView error,country, countryError, phoneError, code;
    EditText phone;
    Button send;

    private Map<String, String> mSendData = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.restore);

        error = (TextView) findViewById(R.id.error);
        country = (TextView) findViewById(R.id.country);
        countryError = (TextView) findViewById(R.id.countryError);
        phoneError = (TextView) findViewById(R.id.phoneError);
        phone = (EditText) findViewById(R.id.phone_restore);
        phone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        code = (TextView) findViewById(R.id.code);
        send = (Button) findViewById(R.id.send);
        send.setOnClickListener(this);
        country.setOnClickListener(this);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(this, LoginActivity.class);
        finish();
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.send:
                boolean validated = validate();
                if(validated) {
                    QueryPreferences.setStoredPhone(getApplicationContext(), "+7" +
                            phone.getText().toString().replaceAll("\\s+", "")
                                    .replaceAll("-", ""));

                    mSendData.clear();
                    mSendData.put("method", RequestUrls.METHOD_SMS);
                    mSendData.put("phone", QueryPreferences.getStoredPhone(getApplicationContext()));

                    HttpPostAsyncTask task = new HttpPostAsyncTask(this, mSendData);
                    task.execute(RequestUrls.PROFILE_FIRST_INPUT);
                }
                break;
            case R.id.country:
               Intent intent2 = new Intent(this, CountriesActivity.class);
                startActivityForResult(intent2, 1);
                return;
        }
    }
    private boolean validate() {
        boolean result = true;

        phone.setBackgroundResource(R.drawable.blue_bottom_line);
        phoneError.setVisibility(View.INVISIBLE);
        error.setVisibility(View.INVISIBLE);

        if(phone.getText().toString().length() == 0) {
            phoneError.setText("*Введите номер телефона");
            phone.setBackgroundResource(R.drawable.red_bottom_line);
            phoneError.setVisibility(View.VISIBLE);
            result = false;
        } else if(phone.getText().toString().length() <= 9) {
            phoneError.setText("*Введен неверный номер");
            phone.setBackgroundResource(R.drawable.red_bottom_line);
            phoneError.setVisibility(View.VISIBLE);
            result = false;
        }

        return result;
    }

    @Override
    public void afterFinishingHttpPostAsyncTask(JSONObject jsonObject) {
        if (jsonObject != null) {
            finish();
            Intent intent = new Intent(this, RestoreSecondActivity.class);
            startActivity(intent);
        } else {
            error.setVisibility(View.VISIBLE);
            error.setText("Неверный номер!");
        }
    }

    private class Call extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String url = "/account";
            JSONObject jsonBody;
            String requestBody;
            HttpURLConnection urlConnection;

            try {
                String p = phone.getText().toString();
                while(p.indexOf(" ") >= 0) {
                    p = p.replace(" ", "");
                }
                jsonBody = new JSONObject();
                jsonBody.put("method", "sms");
                jsonBody.put("phone", code.getText().toString()+p);
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

        @SuppressLint("ResourceAsColor")
        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
//            show(response);
            try {
                JSONObject res = new JSONObject(response);
                Intent intent;
                switch (res.getString("status")) {
                    case "200":
//                        RestoreActivity.this.finish();
//                        intent = new Intent(RestoreActivity.this, RestoreSecondActivity.class);
//                        startActivity(intent);

                        String p = phone.getText().toString();
                        while(p.indexOf(" ") > -1) {
                            p = p.replace(" ", "");
                        }
                        p = code.getText().toString()+p;
                        intent = new Intent(RestoreActivity.this, RestoreSecondActivity.class);
                        intent.putExtra("phone", p);
                        RestoreActivity.this.finish();
                        startActivity(intent);


//                        Intent intent2 = new Intent();
//                        intent.putExtra("phone", phone.getText().toString());
//                        setResult(RESULT_OK, intent2);
//                        finish();

                        break;
                    case "403":
                        error.setText("Невозможно отправить смс с кодом. Попробуйте позжу ");
                        error.setVisibility(View.VISIBLE);
                        break;
                    case "404":
                        error.setText("Пользователя с данным номером не существует");
                        error.setVisibility(View.VISIBLE);
                        break;
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