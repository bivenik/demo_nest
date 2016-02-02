package com.ibohdan.nest.network;

import com.ibohdan.nest.entity.EmptyBody;
import com.ibohdan.nest.entity.Token;

import retrofit.http.Body;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Query;
import rx.Observable;

public interface Api {

    String FIREBASE_NEST_URL = "https://developer-api.nest.com";

    String CLIENT_CODE_URL = "https://home.nest.com/login/oauth2?client_id=%s&state=%s";

    String TOKEN_ENDPOINT = "https://api.home.nest.com/oauth2/";

    @POST("/access_token?grant_type=authorization_code")
    @Headers("Content-type: application/x-www-form-urlencoded; charset=UTF-8")
    Observable<Token> getToken(
            @Query("client_id") String clientId,
            @Query("code") String code,
            @Query("client_secret") String clientSecret,
            @Body EmptyBody body
    );
}
