package com.yalin.googleio2016.welcome;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.yalin.googleio2016.R;
import com.yalin.googleio2016.login.LoginAndAuthWithGoogleApi;
import com.yalin.googleio2016.util.AccountUtils;
import com.yalin.googleio2016.util.LogUtil;

/**
 * YaLin
 * 2016/11/23.
 */

public class AccountFragment extends WelcomeFragment implements View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    private static final String TAG = "AccountFragment";
    private static final int SIGN_IN_RESULT = 1;
    private View mLayout;
    private String mSelectedAccount;
    private GoogleApiClient mGoogleApiClient;

    @Override
    public boolean shouldDisplay(Context context) {
        Account account = AccountUtils.getActiveAccount(context);
        return account == null;
    }

    @Override
    protected String getPrimaryButtonText() {
        return getResourceString(R.string.skip);
    }

    @Override
    protected String getSecondaryButtonText() {
        return null;
    }

    @Override
    protected View.OnClickListener getPrimaryButtonListener() {
        return new WelcomeFragmentOnClickListener(mActivity) {
            @Override
            public void onClick(View v) {
                mSelectedAccount = "demo@gmail.com";
                AccountUtils.setActiveAccount(mActivity, mSelectedAccount);
                doNext();
            }
        };
    }

    @Override
    protected View.OnClickListener getSecondaryButtonListener() {
        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLayout.findViewById(R.id.sign_in_button).setOnClickListener(this);
        mLayout.findViewById(R.id.sign_in_button).setEnabled(true);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mLayout.findViewById(R.id.sign_in_button).setEnabled(false);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getActivity(), "Unable to connect to Google Play Services",
                Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mLayout = inflater.inflate(R.layout.welcome_account_fragment, container, false);

        mLayout.findViewById(R.id.sign_in_button).setEnabled(false);

        if (mActivity instanceof WelcomeFragmentContainer) {
//            ((WelcomeFragmentContainer) mActivity).setPrimaryButtonEnabled(false);
        }

        GoogleSignInOptions.Builder gsoBuilder =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN);

        for (String scope : LoginAndAuthWithGoogleApi.getAuthScopes()) {
            gsoBuilder.requestScopes(new Scope(scope));
        }

        GoogleSignInOptions gso = gsoBuilder.requestEmail().build();

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();

        return mLayout;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mSelectedAccount = null;
        mGoogleApiClient.disconnect();
        mGoogleApiClient = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, SIGN_IN_RESULT);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_RESULT) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(final GoogleSignInResult result) {
        LogUtil.d(TAG, "handleSignInResult: " + result.isSuccess());
        if (result.isSuccess()) {
            final GoogleSignInAccount acct = result.getSignInAccount();
            if (acct != null) {
                AccountUtils.setActiveAccount(getActivity(), acct.getEmail());
                doNext();
            }
        }
    }
}
