package com.ibohdan.nest.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.ibohdan.nest.App;
import com.ibohdan.nest.BuildConfig;
import com.ibohdan.nest.R;
import com.ibohdan.nest.entity.EmptyBody;
import com.ibohdan.nest.entity.Token;
import com.ibohdan.nest.network.Api;
import com.ibohdan.nest.util.Preferences;

import java.util.UUID;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class LoginActivity extends AppCompatActivity {

    @Inject
    Preferences preferences;

    @Inject
    Api api;

    WebView webView;
    FrameLayout progressLayout;

    private String code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.getGraph().inject(this);
        setContentView(R.layout.login_activity);

        if (preferences.getToken() != null) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        webView = (WebView) findViewById(R.id.web_view);
        progressLayout = (FrameLayout) findViewById(R.id.progress);

        loadWebPage();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadWebPage() {
        showProgress();

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new RedirectClient());
        webView.getSettings().setJavaScriptEnabled(true);

        final String url = String.format(Api.CLIENT_CODE_URL, BuildConfig.NEST_CLIENT_ID, UUID.randomUUID());
        webView.loadUrl(url);
    }

    private void showProgress() {
        progressLayout.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        progressLayout.setVisibility(View.GONE);
    }

    private void requestAccessToken() {
        showProgress();
        api.getToken(BuildConfig.NEST_CLIENT_ID, code, BuildConfig.NEST_CLIENT_SECRET, new EmptyBody())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onTokenSuccess, this::onTokenFailed);
    }

    private void onTokenSuccess(Token token) {
        Timber.v("onTokenSuccess: %s", token);
        if (token != null) {
            preferences.setToken(token);
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        } else {
            onTokenFailed(null);
        }
    }

    private void onTokenFailed(Throwable t) {
        String error = "Unable to get access token: %s";
        Timber.e(error, t);
        showMessage(error);
    }

    private void showMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    private class RedirectClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            hideProgress();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (!url.startsWith(BuildConfig.NEST_REDIRECT_URL)) {
                code = null;
                return false;
            }
            code = parseCode(url);
            Timber.v("Got code: %s", code);
            if (TextUtils.isEmpty(code)) {
                showMessage("Something gone wrong!");
                return true;
            }
            requestAccessToken();
            return true;
        }


        @Nullable
        private String parseCode(String url) {
            try {
                return Uri.parse(url).getQueryParameter("code");
            } catch (Exception e) {
                return null;
            }
        }
    }
}
