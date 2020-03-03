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
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.admin.smartchatalphav1.Interfaces.ResponseHttpAsyncTask;
import com.example.admin.smartchatalphav1.Models.User;
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

import static java.lang.String.valueOf;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, ResponseHttpAsyncTask {

    private static final String TAG = "LoginActivity";

    private ImageView google, vk, facebook, mail;
    private TextView error, countryError, country, phoneError, passwordError, forgot, registration, code;
    private EditText phone, password;
    private Button enter;

    private Map<String, String> mSendData = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Если пользователь уже авторизирован ранее
        if (QueryPreferences.getStoredSuccess(getApplicationContext())) {
            finish();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

        setContentView(R.layout.login);

        google = (ImageView) findViewById(R.id.google);
        vk = (ImageView) findViewById(R.id.vk);
        facebook = (ImageView) findViewById(R.id.facebook);
        mail = (ImageView) findViewById(R.id.mail);
        error = (TextView) findViewById(R.id.error);
        countryError = (TextView) findViewById(R.id.countryError);
        phoneError = (TextView) findViewById(R.id.phoneError);
        passwordError = (TextView) findViewById(R.id.passwordError);
        forgot = (TextView) findViewById(R.id.forgot);
        registration = (TextView) findViewById(R.id.registration);
        country = (TextView) findViewById(R.id.country);
        phone = (EditText) findViewById(R.id.phone_login);
        phone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        password = (EditText) findViewById(R.id.password);
        enter = (Button) findViewById(R.id.enter);
        code = (TextView) findViewById(R.id.code);

        registration.setOnClickListener(this);
        forgot.setOnClickListener(this);
        country.setOnClickListener(this);
        enter.setOnClickListener(this);

        mSendData.clear();
        mSendData.put("method", RequestUrls.METHOD_PROFILE);
    }

    @Override
    public void onClick(View view) {
        Intent intent;

        switch(view.getId()) {
            case R.id.registration:
                intent = new Intent(this, RegistrationActivity.class);
                finish();
                startActivity(intent);
                break;
            case R.id.forgot:
                intent = new Intent(this, RestoreActivity.class);
                finish();
                startActivity(intent);
                break;
            case R.id.country:
                intent = new Intent(this, CountriesActivity.class);
                startActivityForResult(intent, 1);
                break;
            case R.id.enter:
                 boolean validated = validate();
                if (validated) {
                    String validatedPhone = "+7" + phone.getText().toString().replaceAll("\\s+", "")
                            .replaceAll("-", "");
                    QueryPreferences.setStoredPhone(getApplicationContext(), validatedPhone);

                    mSendData.clear();
                    mSendData.put("method", RequestUrls.METHOD_LOGIN);
                    mSendData.put("password", password.getText().toString());
                    mSendData.put("phone", validatedPhone);

                    HttpPostAsyncTask task = new HttpPostAsyncTask(this, mSendData);
                    task.execute(RequestUrls.PROFILE_FIRST_INPUT);
                }
        }
    }

    private boolean validate() {
        boolean result = true;

        //По логике Жасмин 0 - Visible, 4 - Invisible
        error.setVisibility(View.INVISIBLE);
        phoneError.setVisibility(View.INVISIBLE);
        passwordError.setVisibility(View.INVISIBLE);
        phone.setBackgroundResource(R.drawable.blue_bottom_line);
        password.setBackgroundResource(R.drawable.blue_bottom_line);

        if(phone.length() == 0) {
            phoneError.setText("*Введите номер телефона");
            phone.setBackgroundResource(R.drawable.red_bottom_line);
            phoneError.setVisibility(View.VISIBLE);
            result = false;
        } else if(phone.length() < 10) {
            phoneError.setText("*Введен неверный номер");
            phone.setBackgroundResource(R.drawable.red_bottom_line);
            phoneError.setVisibility(View.VISIBLE);
            result = false;
        }

        if(password.length() == 0) {
            passwordError.setText("*Введите пароль");
            password.setBackgroundResource(R.drawable.red_bottom_line);
            passwordError.setVisibility(View.VISIBLE);
            result = false;
        } else if(password.length() < 5) {
            password.setBackgroundResource(R.drawable.red_bottom_line);
            passwordError.setText("*Пароль должен содержать минимум 5 символов");
            passwordError.setVisibility(View.VISIBLE);
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        code.setText(data.getStringExtra("code"));
        country.setText(data.getStringExtra("name"));
    }

    @Override
    public void afterFinishingHttpPostAsyncTask(JSONObject jsonObject) {
        try {
            if (jsonObject != null) {
                //Запись данных в preferences для дальнейшего использования
                QueryPreferences.setStoredPassword(getApplicationContext(), password.getText().toString());
                QueryPreferences.setStoredToken(getApplicationContext(), jsonObject.getString("password"));
                QueryPreferences.setPrefIdQuery(getApplicationContext(), jsonObject.getString("id"));
                QueryPreferences.setStoredSuccess(getApplicationContext(), true);

                finish();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            } else {
                error.setVisibility(View.VISIBLE);
                error.setText("Неверный номер или пароль!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class Authorithation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String url = "http://185.146.2.146:9797/account";
            JSONObject jsonBody;
            String requestBody;
            HttpURLConnection urlConnection;
            try {
                String p = phone.getText().toString();
                while(p.indexOf(" ") > -1) {
                    p = p.replace(" ", "");
                }
                jsonBody = new JSONObject();
                jsonBody.put("method", "login");
                jsonBody.put("phone",code.getText().toString() + p);
                jsonBody.put("password", password.getText().toString());
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
                        SharedPreferences settings = LoginActivity.this.getSharedPreferences("authorithation", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString( "phone", res.getString("phone") );
                        editor.putString("token", res.getString("password"));
                        editor.putString("password", password.getText().toString());
                        editor.commit();

//                        LoginActivity.this.finish();
//                        intent = new Intent(LoginActivity.this, Profile.class);
//                        startActivity(intent);

                        break;
                    case "401":
                        error.setText("Неверный номер или пароль");
                        error.setVisibility(View.VISIBLE);
                        break;
                    case "423":
                        error.setText("Аккаунт заблокирован");
                        error.setVisibility(View.VISIBLE);
                        break;
                    case "303":
                        String p = phone.getText().toString();
                        while(p.indexOf(" ") > -1) {
                            p = p.replace(" ", "");
                        }
                        p = code.getText().toString()+p;
                        intent = new Intent(LoginActivity.this, ActivationActivity.class);
                        intent.putExtra("phone", p);
                        LoginActivity.this.finish();
                        startActivity(intent);
                }
            } catch (JSONException e) {
                show("не получилось сделать JSONObject");
                e.printStackTrace();
            }
        }
    }
}
