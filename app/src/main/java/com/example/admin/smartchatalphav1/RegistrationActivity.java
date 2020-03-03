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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener, ResponseHttpAsyncTask {

    ImageView google, vk, facebook, mail;
    TextView country, error, countryError, nameError, phoneError, passwordError, confirmError, login, code;
    EditText phone, name, password, confirm;
    Button registration;

    private Map<String, String> mSendData = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);

        google = (ImageView) findViewById(R.id.google);
        vk = (ImageView) findViewById(R.id.vk);
        facebook = (ImageView) findViewById(R.id.facebook);
        mail = (ImageView) findViewById(R.id.mail);
        error = (TextView) findViewById(R.id.error);
        countryError = (TextView) findViewById(R.id.countryError);
        phoneError = (TextView) findViewById(R.id.phoneError);
        passwordError = (TextView) findViewById(R.id.passwordError);
        registration = (Button) findViewById(R.id.registration);
        country = (TextView) findViewById(R.id.country);
        phone = (EditText) findViewById(R.id.phone_registration);
        phone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        password = (EditText) findViewById(R.id.password);
        nameError = (TextView) findViewById(R.id.nameError);
        confirmError = (TextView) findViewById(R.id.confirmError);
        login = (TextView) findViewById(R.id.login);
        name = (EditText) findViewById(R.id.name);
        confirm = (EditText) findViewById(R.id.confirm);
        code = (TextView) findViewById(R.id.code);

        registration.setOnClickListener(this);
        country.setOnClickListener(this);
        login.setOnClickListener(this);

        mSendData.clear();
        mSendData.put("method", RequestUrls.METHOD_REGISTRATION);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(this, LoginActivity.class);
        this.finish();
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        Intent intent;

        switch (view.getId()) {
            case R.id.login:
                intent = new Intent(this, LoginActivity.class);
                this.finish();
                startActivity(intent);
                return;
            case R.id.registration:
                    boolean validated = validate();
                    if(validated) {
                        mSendData.clear();
                        mSendData.put("method", RequestUrls.METHOD_REGISTRATION);
                        mSendData.put("phone", "+7" + phone.getText().toString().replaceAll("\\s+", "")
                        .replaceAll("-", ""));
                        mSendData.put("firstname", name.getText().toString());
                        mSendData.put("password", password.getText().toString());

                        QueryPreferences.setStoredPhone(getApplicationContext(),"+7" + phone.getText().toString());
                        QueryPreferences.setStoredPassword(getApplicationContext(), password.getText().toString());

                        HttpPostAsyncTask task = new HttpPostAsyncTask(this, mSendData);
                        task.execute(RequestUrls.PROFILE_FIRST_INPUT);

                        //Sending sms
                        mSendData.clear();
                        mSendData.put("method", RequestUrls.METHOD_SMS);
                        mSendData.put("phone", "+7" + phone.getText().toString());

                        HttpPostAsyncTask taskSms = new HttpPostAsyncTask(this, mSendData);
                        taskSms.execute(RequestUrls.PROFILE_FIRST_INPUT);
                    }
                return;
            case R.id.country:
                intent = new Intent(this, CountriesActivity.class);
                startActivityForResult(intent, 1);
                return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        country.setText(data.getStringExtra("name"));
        code.setText(data.getStringExtra("code"));


    }

    private boolean validate() {
        boolean result = true;

        name.setText(name.getText().toString().trim());

        error.setVisibility(View.INVISIBLE);
        phoneError.setVisibility(View.INVISIBLE);
        passwordError.setVisibility(View.INVISIBLE);
        nameError.setVisibility(View.INVISIBLE);
        confirmError.setVisibility(View.INVISIBLE);
        phone.setBackgroundResource(R.drawable.blue_bottom_line);
        name.setBackgroundResource(R.drawable.blue_bottom_line);
        password.setBackgroundResource(R.drawable.blue_bottom_line);
        confirm.setBackgroundResource(R.drawable.blue_bottom_line);

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

        if(name.length() == 0) {
            nameError.setVisibility(View.VISIBLE);
            name.setBackgroundResource(R.drawable.red_bottom_line);
            nameError.setText("Введите имя и фамилию");
            result = false;
        }

        if(!name.getText().toString().contains(" ")) {
           nameError.setVisibility(View.VISIBLE);
           nameError.setText("Введите фамилию");
           name.setBackgroundResource(R.drawable.red_bottom_line);
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

        if(confirm.length() == 0) {
            confirmError.setText("*Повторите пароль");
            confirm.setBackgroundResource(R.drawable.red_bottom_line);
            confirmError.setVisibility(View.VISIBLE);
            result = false;
        }

        if(!confirm.getText().toString().equals(password.getText().toString()) && password.length() > 0) {
            confirmError.setText("Пароли не совпадают");
            confirm.setBackgroundResource(R.drawable.red_bottom_line);
            confirmError.setVisibility(View.VISIBLE);
        }
        return result;
    }

    @Override
    public void afterFinishingHttpPostAsyncTask(JSONObject jsonObject) {
        try {
            if (jsonObject != null) {
                QueryPreferences.setStoredToken(getApplicationContext(), jsonObject.getString("password"));

                Intent intent = new Intent(RegistrationActivity.this, ActivationActivity.class);

                RegistrationActivity.this.finish();
                startActivity(intent);
            }
            else {
                error.setText("Пользователь с данным номером уже зарегестрирован!");
                error.setVisibility(View.VISIBLE);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private class Registartion extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String url = RequestUrls.PROFILE_FIRST_INPUT;
            JSONObject jsonBody;
            String requestBody;
            HttpURLConnection urlConnection;

                String p = phone.getText().toString();
                while(p.indexOf(" ") >= 0) {
                    p = p.replace(" ", "");
                }
            try {
                jsonBody = new JSONObject();
                jsonBody.put("method", "registration");
                jsonBody.put("phone", code.getText().toString() + p);
                jsonBody.put("firstname", name.getText().toString());
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
                        String p = phone.getText().toString();
                        while(p.indexOf(" ") >= 0) {
                            p = p.replace(" ", "");
                        }
                        p = code.getText().toString() + p;

                        SharedPreferences settings = RegistrationActivity.this.getSharedPreferences("authorithation", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString( "phone", res.getString("phone") );
                        editor.putString("token", res.getString("password"));
                        editor.putString("password", password.getText().toString());
                        editor.commit();

                        intent = new Intent(RegistrationActivity.this, ActivationActivity.class);
                        intent.putExtra("phone", p);
                        RegistrationActivity.this.finish();
                        startActivity(intent);
                    case "409":
                        error.setText("Пользователь с данным номером уже зарегестрирован");
                        error.setVisibility(View.VISIBLE);
                        break;
                }
            } catch (JSONException e) {
//                show(e.toString());
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