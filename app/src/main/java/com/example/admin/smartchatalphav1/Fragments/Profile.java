package com.example.admin.smartchatalphav1.Fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.admin.smartchatalphav1.Interfaces.ResponseHttpAsyncTask;
import com.example.admin.smartchatalphav1.LoginActivity;
import com.example.admin.smartchatalphav1.MainActivity;
import com.example.admin.smartchatalphav1.Models.User;
import com.example.admin.smartchatalphav1.Network.HttpPostAsyncTask;
import com.example.admin.smartchatalphav1.QueryPreferences;
import com.example.admin.smartchatalphav1.R;
import com.example.admin.smartchatalphav1.Network.RequestUrls;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Profile extends Fragment implements ResponseHttpAsyncTask{

    private ImageView mAvatarProfileImageView;
    private TextView mFullNameTextView;
    private TextView mLevelTextView;
    private TextView mAgeTextView;
    private TextView mPhoneTextView;
    private TextView mEmailTextView;
    private Button mJoinToGroup;

    private Map<String, String> mSendData = new HashMap<>();

    final String TAG = "ProfileFragment";

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView()");
        View v = inflater.inflate(R.layout.profile_fragment, null);

        mFullNameTextView = (TextView) v.findViewById(R.id.fullNameProfileTextView);
        mLevelTextView = (TextView) v.findViewById(R.id.levelUserProfileTextView);
        //mAgeTextView = (TextView) v.findViewById(R.id.ageUserProfileTextView);
        mPhoneTextView = (TextView) v.findViewById(R.id.phoneUserProfileTextView);
        mEmailTextView = (TextView) v.findViewById(R.id.emailUserProfileTextView);
        mJoinToGroup = (Button) v.findViewById(R.id.joinToGroupProfileButton);
        mAvatarProfileImageView = (ImageView) v.findViewById(R.id.userAvatarProfileImageView);

        mSendData.clear();
        mSendData.put("token", QueryPreferences.getStoredToken(getActivity()));
        mSendData.put("method", RequestUrls.METHOD_PROFILE);

        HttpPostAsyncTask tokenTask = new HttpPostAsyncTask(this , mSendData);
        tokenTask.execute(RequestUrls.API);
//
//        if (LoginActivity.mUser != null) {
//            if (LoginActivity.mUser.getPhotoFile() != null && !LoginActivity.mUser.getPhotoFile().equals("null")) {
//                Picasso.with(getActivity()).load(RequestUrls.IMAGE + LoginActivity.mUser.getPhotoFile())
//                        .into(mAvatarProfileImageView);
//                Log.i(TAG, "getPhotoFile is not null");
//            }
//            if (LoginActivity.mUser.getFirstName() != null && !LoginActivity.mUser.getFirstName().equals("null"))
//                mFullNameTextView.setText(LoginActivity.mUser.getFirstName() + " " + LoginActivity.mUser.getLastName());
//            if (LoginActivity.mUser.getLevel() != null && !LoginActivity.mUser.getLevel().equals("null"))
//                mLevelTextView.setText(LoginActivity.mUser.getLevel());
//            //mAgeTextView.setText(MainActivity.mUser.getAge() + "");
//            if (LoginActivity.mUser.getPhone() != null && !LoginActivity.mUser.getPhone().equals("null"))
//                mPhoneTextView.setText(LoginActivity.mUser.getPhone());
//            if (LoginActivity.mUser.getEmail() != null && !LoginActivity.mUser.getEmail().equals("null"))
//                mEmailTextView.setText(LoginActivity.mUser.getEmail());
//            if (LoginActivity.mUser.getGroupId() != null && LoginActivity.mUser.getGroupId().equals("0"))
//                mJoinToGroup.setVisibility(View.VISIBLE);
//        }

        return v;
    }

    public void setProfileData(JSONObject setData) {
        try {
            Log.i(TAG, "setProfileData");

            JSONObject jsonProfile = setData.getJSONObject("profile");
            String firstName = jsonProfile.getString("firstname");
            String lastName = jsonProfile.getString("lastname");
            String avaFileName = jsonProfile.getString("ava");
            String email = jsonProfile.getString("email");
            String level = jsonProfile.getString("lvl_name");
            String groupId = jsonProfile.getString("group_id");
            String phone = jsonProfile.getString("phone");

            mFullNameTextView.setText(firstName + " " + lastName);
            mLevelTextView.setText(level);
            mPhoneTextView.setText(phone);
            mEmailTextView.setText(email);

            Log.i(TAG, firstName);

//            Picasso.with(getActivity()).load(RequestUrls.IMAGE + avaFileName)
//                    .into(mAvatarProfileImageView);
//            if (groupId.equals("0")) mJoinToGroup.setVisibility(View.VISIBLE);
        } catch (Exception e ) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterFinishingHttpPostAsyncTask(JSONObject responseData) {
        try {
            Log.i(TAG, "afterFinishingHttpPostAsyncTask");
            Log.i(TAG, responseData.toString());

            JSONObject jsonProfile = responseData.getJSONObject("profile");
            String firstName = jsonProfile.getString("firstname");
            String lastName = jsonProfile.getString("lastname");
            String avaFileName = jsonProfile.getString("ava");
            String email = jsonProfile.getString("email");
            String level = jsonProfile.getString("lvl_name");
            String groupId = jsonProfile.getString("group_id");
            String phone = jsonProfile.getString("phone");

            mFullNameTextView.setText(firstName + " " + lastName);
            mLevelTextView.setText(level);
            mPhoneTextView.setText(phone);
            mEmailTextView.setText(email);

            if (!avaFileName.isEmpty() && !avaFileName.equals("null")) {
                Picasso.with(getActivity()).load(RequestUrls.IMAGE + avaFileName)
                        .into(mAvatarProfileImageView);
            }
            if (groupId.equals("0")) {
                mJoinToGroup.setVisibility(View.VISIBLE);
            }
        } catch (Exception e ) {
            e.printStackTrace();
        }
    }
}
