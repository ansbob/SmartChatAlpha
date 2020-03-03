package com.example.admin.smartchatalphav1;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.admin.smartchatalphav1.Fragments.Profile;
import com.example.admin.smartchatalphav1.Fragments.Settings;
import com.example.admin.smartchatalphav1.Fragments.Tariffs;
import com.example.admin.smartchatalphav1.Interfaces.ResponseHttpAsyncTask;
import com.example.admin.smartchatalphav1.Models.User;
import com.example.admin.smartchatalphav1.Network.HttpPostAsyncTask;
import com.example.admin.smartchatalphav1.Network.RequestUrls;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ResponseHttpAsyncTask{

    private static final String TAG = "MainActivity";

    private Map<String, String> mSendData = new HashMap<>();
    private TextView mUserNameTextView;
    private TextView mUserEmailTextView;
    private ImageView mAvatarImageView;

    private Profile ProfileFragment;
    private Settings SettingsFragment;
    private Tariffs TariffsFragment;

    private FragmentManager fm;
    private JSONObject mResponseJsonData;
    private Boolean firstAddFragmentSettings = false;
    private Boolean firstAddFragmentTariffs = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Создание фрагментов
        ProfileFragment = new Profile();
        SettingsFragment = new Settings();
        TariffsFragment = new Tariffs();

        //Инициализация фрагмента
        fm = getFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.frgContainer);
        if (fragment == null) {
            fragment = ProfileFragment;
            fm.beginTransaction()
                    .add(R.id.frgContainer, fragment)
                    .commit();
        }

        mSendData.clear();
        mSendData.put("token", QueryPreferences.getStoredToken(getApplicationContext()));
        mSendData.put("method", RequestUrls.METHOD_PROFILE);
    }

    @Override
    public void onResume() {
        super.onResume();

        HttpPostAsyncTask tokenTask = new HttpPostAsyncTask(this , mSendData);
        tokenTask.execute(RequestUrls.API);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                FragmentTransaction fTrans = fm.beginTransaction();

                fTrans.detach(ProfileFragment);
                fTrans.detach(TariffsFragment);

                if (firstAddFragmentSettings) {
                    fTrans.attach(SettingsFragment);
                } else {
                    fTrans.add(R.id.frgContainer, SettingsFragment);
                    fTrans.attach(SettingsFragment);
                    firstAddFragmentSettings = true;
                }

                fTrans.commit();
                return true;
            case R.id.action_exit:
                startLoginActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        FragmentTransaction fTrans = fm.beginTransaction();
        switch (id) {
            case R.id.nav_profile:
                fTrans.detach(SettingsFragment);
                fTrans.detach(TariffsFragment);

                fTrans.attach(ProfileFragment);
                break;
            case R.id.nav_tariffs:
                fTrans.detach(ProfileFragment);
                fTrans.detach(SettingsFragment);

                if (firstAddFragmentTariffs)
                    fTrans.attach(TariffsFragment);
                else {
                    fTrans.add(R.id.frgContainer, TariffsFragment);
                    fTrans.attach(TariffsFragment);
                    firstAddFragmentTariffs = true;
                }
                break;
            case R.id.nav_exit:
                startLoginActivity();
        }

        fTrans.commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startLoginActivity() {
        QueryPreferences.resettingAuthorisedData(this);

        finish();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void afterFinishingHttpPostAsyncTask(JSONObject jsonObject) {
        try {
            //Установка данных для фрагмента Profile
            //ProfileFragment.setProfileData(jsonObject);

            mResponseJsonData = jsonObject;
            JSONObject jsonProfile = mResponseJsonData.getJSONObject("profile");
            String firstName = jsonProfile.getString("firstname");
            String lastName = jsonProfile.getString("lastname");
            String avaFileName = jsonProfile.getString("ava");
            String email = jsonProfile.getString("email");
            String level = jsonProfile.getString("lvl_name");
            String groupId = jsonProfile.getString("group_id");
            String phone = jsonProfile.getString("phone");

            //nav_menu
            mAvatarImageView = (ImageView) findViewById(R.id.userAvatarHeaderImageView);
            mUserNameTextView = (TextView) findViewById(R.id.userNameNavHeaderMain);
            mUserEmailTextView = (TextView) findViewById(R.id.emailNavHeaderMain);

            mUserNameTextView.setText(firstName + " " + lastName);

            Log.i(TAG, firstName + " " + lastName);

            if (!phone.isEmpty() && !phone.equals("null")) {
                mUserEmailTextView.setText(phone);
            }

            if (!avaFileName.isEmpty() && !avaFileName.equals("")) {
                Picasso.with(getApplicationContext()).load(RequestUrls.IMAGE + avaFileName)
                        .into(mAvatarImageView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}