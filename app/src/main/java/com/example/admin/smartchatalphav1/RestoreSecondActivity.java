package com.example.admin.smartchatalphav1;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

public class RestoreSecondActivity extends AppCompatActivity implements View.OnClickListener, ResponseHttpAsyncTask {
    EditText enterCode, newPass,repeatPass;
    TextView error, errorCode, errorPass, errorRepeatPass, title;
    Button restore;
    String phone;
    private Map<String, String> mSendData = new HashMap<>();

    private static final String TAG = "RestoreSecondActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.restore_second);
        enterCode = (EditText) findViewById(R.id.enterCode);
        newPass = (EditText) findViewById(R.id.newPass);
        repeatPass = (EditText) findViewById(R.id.repeatPass);
        error = (TextView) findViewById(R.id.error);
        errorCode = (TextView) findViewById(R.id.errorCode);
        title = (TextView) findViewById(R.id.title);
        errorPass = (TextView) findViewById(R.id.errorPass);
        errorRepeatPass = (TextView) findViewById(R.id.errorRepeatPass);
        restore = (Button) findViewById(R.id.restore);
        restore.setOnClickListener(this);

        Bundle arguments = getIntent().getExtras();
        phone = QueryPreferences.getStoredPhone(getApplicationContext());

        title.setText("На номер "+ phone + " было отправлено смс с кодом");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(this, Restore.class);
        this.finish();
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.restore:
                boolean validated = validate();
                if(validated) {
                    QueryPreferences.setStoredPassword(getApplicationContext(), newPass.getText().toString());

                    mSendData.clear();
                    mSendData.put("method", RequestUrls.METHOD_PASSWORD);
                    mSendData.put("code", enterCode.getText().toString());
                    mSendData.put("phone", QueryPreferences.getStoredPhone(getApplicationContext()));
                    mSendData.put("password", newPass.getText().toString());

                    HttpPostAsyncTask task = new HttpPostAsyncTask(this, mSendData);
                    task.execute(RequestUrls.PROFILE_FIRST_INPUT);
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//     data.getStringExtra("phone");

    }
    private boolean validate() {
        boolean result = true;

        enterCode.setBackgroundResource(R.drawable.blue_bottom_line);
        newPass.setBackgroundResource(R.drawable.blue_bottom_line);
        repeatPass.setBackgroundResource(R.drawable.blue_bottom_line);

        error.setVisibility(View.INVISIBLE);
        errorRepeatPass.setVisibility(View.INVISIBLE);
        errorPass.setVisibility(View.INVISIBLE);
        errorCode.setVisibility(View.INVISIBLE);

        if(enterCode.length() ==  0){
            errorCode.setText("Введите код");
            errorCode.setVisibility(View.VISIBLE);
        }
        if(newPass.length() == 0) {
            errorPass.setText("*Введите пароль");
            newPass.setBackgroundResource(R.drawable.red_bottom_line);
            errorPass.setVisibility(View.VISIBLE);
            result = false;
        } else if(newPass.length() < 5) {
            newPass.setBackgroundResource(R.drawable.red_bottom_line);
            errorPass.setText("*Пароль должен содержать минимум 5 символов");
            errorPass.setVisibility(View.VISIBLE);
            result = false;
        }
        if(repeatPass.length() == 0) {
            errorRepeatPass.setText("*Повторите пароль");
            repeatPass.setBackgroundResource(R.drawable.red_bottom_line);
            errorRepeatPass.setVisibility(View.VISIBLE);
            result = false;
        }

        if(!repeatPass.getText().toString().equals(newPass.getText().toString()) && newPass.length() > 0) {
            errorRepeatPass.setText("Пароли не совпадают");
            repeatPass.setBackgroundResource(R.drawable.red_bottom_line);
            errorRepeatPass.setVisibility(View.VISIBLE);
            result = false;
        }

        if (enterCode.length() == 0) {
            errorCode.setVisibility(View.VISIBLE);
            errorCode.setText("Введите код");
            result = false;
        }else if(enterCode.length()<4) {
            errorCode.setText("заполните поле");
            errorCode.setVisibility(View.VISIBLE);
            result = false;
        }

        return result;
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
    public void afterFinishingHttpPostAsyncTask(JSONObject jsonObject) {
        try {
            int status = Integer.parseInt(jsonObject.getString("status"));

            if (status == 200) {
                String responseToken = jsonObject.getString("password");
                QueryPreferences.setStoredToken(getApplicationContext(), responseToken);

                finish();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            } else if (status == 400) {
                error.setVisibility(View.VISIBLE);
                error.setText("Не повторяйте старый пароль!");
            } else if (status == 417) {
                error.setText("Неверный код");
                error.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class Restore extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String url = "http://185.146.2.146:9797/account";
            JSONObject jsonBody;
            String requestBody;
            HttpURLConnection urlConnection;
//            try {
//                String p = error.getText().toString();
//                while(p.indexOf(" ") > -1) {
//                    p = p.replace(" ", "");
//                }
            try{
                jsonBody = new JSONObject();
                jsonBody.put("method", "password");
                jsonBody.put("code", enterCode.getText().toString());
                jsonBody.put("phone", phone);
                jsonBody.put("password", newPass.getText().toString());
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
//            show(response);
            try {
                JSONObject res = new JSONObject(response);
                Intent intent;
                switch (res.getString("status")) {
                    case "200":

                        SharedPreferences settings = RestoreSecondActivity.this.getSharedPreferences("authorithation", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString( "phone", phone );
                        editor.putString("token", res.getString("password"));
                        editor.putString("password", newPass.getText().toString());
                        editor.commit();
//                        RestoreSecondActivity.this.finish();
//                        intent = new Intent(RestorSecond.this, SmartChat.class);
//                        startActivity(intent);

                        break;
                    case "417":
                        error.setText("Неверный код");
                        error.setVisibility(View.VISIBLE);
                        break;
                    case "400":
                        error.setText("Не повторяйте старый пароль");
                        error.setVisibility(View.VISIBLE);
                        break;

                }
            } catch (JSONException e) {
                show("не получилось сделать JSONObject");
                e.printStackTrace();
            }
        }
    }
}