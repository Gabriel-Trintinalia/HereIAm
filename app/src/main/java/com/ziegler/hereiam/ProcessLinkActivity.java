package com.ziegler.hereiam;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.net.MalformedURLException;
import java.net.URL;

public class ProcessLinkActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "PROCESSLINKACTIVITY";
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(AppInvite.API)
                .build();

        // Check if this app was launched from a deep link. Setting autoLaunchDeepLink to true
        // would automatically launch the deep link if one is found.
        boolean autoLaunchDeepLink = false;
        AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, this, autoLaunchDeepLink)
                .setResultCallback(
                        new ResultCallback<AppInviteInvitationResult>() {
                            @Override
                            public void onResult(@NonNull AppInviteInvitationResult result) {
                                if (result.getStatus().isSuccess()) {
                                    // Extract deep link from Intent
                                    Intent intent = result.getInvitationIntent();
                                    String deepLink = AppInviteReferral.getDeepLink(intent);

                                    try {
                                        URL url = new URL(deepLink);
                                        String roomKey = url.getQuery();
                                        Log.d(TAG, roomKey);

                                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                        if (user == null) {
                                            Intent welcome = new Intent(ProcessLinkActivity.this, WelcomeActivity.class);
                                            startActivity(welcome);
                                        } else {

                                            FirebaseUtil.joinRoom(user.getUid(), roomKey);
                                        }
                                    } catch (MalformedURLException e) {
                                        e.printStackTrace();
                                    }
                                    finish();

                                } else {
                                    // TODO: 16/01/2017 Create user friendly message when the link is wrong
                                    Log.d(TAG, "getInvitation: no deep link found.");
                                }
                            }
                        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, connectionResult.getErrorMessage());
    }
}
