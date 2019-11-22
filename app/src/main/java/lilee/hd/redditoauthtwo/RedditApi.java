package lilee.hd.redditoauthtwo;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface RedditApi {
//    @Headers("Content-Type: application/json")
    @FormUrlEncoded
    @POST("api/v1/access_token")
    Call<TokenResponse> getAccessToken(
            @Field("grant_type") String grantType,
            @Field("code") String code,
            @Field("redirect_uri") String redirect_uri
    );
}
