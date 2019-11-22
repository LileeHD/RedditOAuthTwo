package lilee.hd.redditoauthtwo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static lilee.hd.redditoauthtwo.Constants.Authcode;
import static lilee.hd.redditoauthtwo.Constants.BASE_URL;
import static lilee.hd.redditoauthtwo.Constants.CLIENT_ID;
import static lilee.hd.redditoauthtwo.Constants.CLIENT_ID_KEY;
import static lilee.hd.redditoauthtwo.Constants.DURATION;
import static lilee.hd.redditoauthtwo.Constants.DURATION_KEY;
import static lilee.hd.redditoauthtwo.Constants.Expiresin;
import static lilee.hd.redditoauthtwo.Constants.GRANT_TYPE;
import static lilee.hd.redditoauthtwo.Constants.OAUTH_BASE_URL;
import static lilee.hd.redditoauthtwo.Constants.REDIRECT_URI;
import static lilee.hd.redditoauthtwo.Constants.REDIRECT_URI_KEY;
import static lilee.hd.redditoauthtwo.Constants.RESPONSE_TYPE;
import static lilee.hd.redditoauthtwo.Constants.RESPONSE_TYPE_KEY;
import static lilee.hd.redditoauthtwo.Constants.Refreshtoken;
import static lilee.hd.redditoauthtwo.Constants.SCOPE;
import static lilee.hd.redditoauthtwo.Constants.SCOPE_KEY;
import static lilee.hd.redditoauthtwo.Constants.STATE;
import static lilee.hd.redditoauthtwo.Constants.STATE_KEY;
import static lilee.hd.redditoauthtwo.Constants.Scope;
import static lilee.hd.redditoauthtwo.Constants.State;
import static lilee.hd.redditoauthtwo.Constants.Tokentype;

public class MainActivity extends AppCompatActivity {
    public static final MediaType CONTENT_TYPE = MediaType.get("application/x-www-form-urlencoded");
    private static final String TAG_TOKEN = "OTTER";
    public OkHttpClient client;
    private TextView textView;
    private Button btn;
    private Button login;
    private EditText mFeedName;
    private String mCurrentFeed;
    private String sort = "new";
    private FirebaseAuth mAuth;
    private String code;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        login = findViewById(R.id.btnLogin);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSignIn();
            }
        });
    }

    private void startSignIn() {

        Uri baseUri = Uri.parse(OAUTH_BASE_URL);
        Uri.Builder builder = baseUri.buildUpon();
        builder.appendQueryParameter(CLIENT_ID_KEY, CLIENT_ID);
        builder.appendQueryParameter(RESPONSE_TYPE_KEY, RESPONSE_TYPE);
        builder.appendQueryParameter(STATE_KEY, STATE);
        builder.appendQueryParameter(REDIRECT_URI_KEY, REDIRECT_URI);
        builder.appendQueryParameter(DURATION_KEY, DURATION);
        builder.appendQueryParameter(SCOPE_KEY, SCOPE);
        String url = builder.toString();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
        Log.d(TAG_TOKEN, "startSignIn: URL " + url);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        catchRedirectUri();
        catchAccessToken();
    }

    private void catchRedirectUri(){
        Uri uri = getIntent().getData();
        if (uri != null && uri.toString().startsWith(REDIRECT_URI)) {

            String code = uri.getQueryParameter("code");
            String state = uri.getQueryParameter("state");
            String error = uri.getQueryParameter("access_denied");
            Log.v(TAG_TOKEN, "onResume: URI received " + uri.toString());
            if (code != null) {
                Log.v(TAG_TOKEN, "onResume: STATE: " + state + " And CODE: " + code);
//                catchAccessToken();
            } else if (uri.getQueryParameter("error") != null) {
                Log.e(TAG_TOKEN, "onResume: TOKEN NOT FOUND" + error);
            }
        }
    }
    private void catchAccessToken() {
        final Uri uri = getIntent().getData();
        if (uri != null && uri.toString().startsWith(REDIRECT_URI)) {

            final String code = uri.getQueryParameter("code");
            String authString = CLIENT_ID + ":"+"";
            final String encodedAuthString = "Basic" + Base64.encodeToString(authString.getBytes(), Base64.NO_WRAP);
            String postData = "grant_type=authorization_code&code="+code+"&redirect_uri="+uri;
            Log.v(TAG_TOKEN, "onResume: URI received, just move on");


            client = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @NotNull
                        @Override
                        public okhttp3.Response intercept(@NotNull Chain chain) throws IOException {
                            Request request = chain.request()
                                    .newBuilder()
                                    .addHeader("Authorization", encodedAuthString)
                                    .build();
                            return chain.proceed(request);
                        }
                    })
                    .addNetworkInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .build();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            RedditApi redditAPI = retrofit.create(RedditApi.class);
            final Call<TokenResponse> responseCall = redditAPI.getAccessToken(
                    GRANT_TYPE,code,REDIRECT_URI
            );

            responseCall.enqueue(new Callback<TokenResponse>() {
                @Override
                public void onResponse(@NotNull Call<TokenResponse> call, @NotNull Response<TokenResponse> response) {
                    if (response.body() !=null){
                        Authcode = response.body().getAccessToken();
                        Tokentype = response.body().getTokenType();
                        Expiresin = String.valueOf(response.body().getExpiresIn());
                        Scope = response.body().getScope();
                        Refreshtoken = response.body().getRefreshToken();
                        Log.d(TAG_TOKEN, "onResponse: "+ response.body().toString());
                    }
                }

                @Override
                public void onFailure(@NotNull Call<TokenResponse> call, @NotNull Throwable t) {
                    Log.e(TAG_TOKEN, "onFailure: token error:" + t.getMessage() );
                }
            });

        }
    }
}
