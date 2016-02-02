package com.ibohdan.nest.network;

import com.ibohdan.nest.entity.EmptyBody;
import com.ibohdan.nest.entity.Token;
import com.ibohdan.nest.network.logansquare.LoganSquareConverter;
import com.squareup.okhttp.OkHttpClient;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import rx.Observable;

public class ApiAdapter implements Api {

    public ApiAdapter() {

    }

    private RestAdapter getAdapter(String host) {
        return new RestAdapter.Builder()
                .setEndpoint(host)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setClient(new OkClient(new OkHttpClient()))
                .setConverter(new LoganSquareConverter())
                .build();
    }

    @Override
    public Observable<Token> getToken(String code, String clientId, String clientSecret, EmptyBody body) {
        return getAdapter(Api.TOKEN_ENDPOINT)
                .create(Api.class)
                .getToken(code, clientId, clientSecret, body);
    }
}
