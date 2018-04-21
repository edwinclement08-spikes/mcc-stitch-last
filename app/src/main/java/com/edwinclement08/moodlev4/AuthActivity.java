package com.edwinclement08.moodlev4;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.mongodb.stitch.android.AuthListener;
import com.mongodb.stitch.android.StitchClient;
import com.mongodb.stitch.android.auth.AvailableAuthProviders;
import com.mongodb.stitch.android.auth.anonymous.AnonymousAuthProvider;
import com.mongodb.stitch.android.auth.oauth2.facebook.FacebookAuthProvider;
import com.mongodb.stitch.android.auth.oauth2.google.GoogleAuthProvider;
import com.mongodb.stitch.android.services.mongodb.MongoClient;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import static com.google.android.gms.auth.api.Auth.GOOGLE_SIGN_IN_API;
import static com.google.android.gms.auth.api.Auth.GoogleSignInApi;


public class AuthActivity extends AppCompatActivity  implements StitchClientListener  {
    private static final String TAG = "MoodleV4:AuthActivity";

    private static final long REFRESH_INTERVAL_MILLIS = 1000;
    private static final int RC_SIGN_IN = 421;

    private static final int RC_MAIN_APP = 500;

    private CallbackManager _callbackManager;
    private GoogleApiClient _googleApiClient;
    private StitchClient _client;
    private MongoClient _mongoClient;

    private TodoListAdapter _itemAdapter;
    private Handler _handler;
    private Runnable _refresher;

    private boolean _fbInitOnce;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_layout);


//        _handler = new Handler();
//        _refresher = new ListRefresher(this);

        StitchClientManager.initialize(this.getApplicationContext());
        StitchClientManager.registerListener(this);


    }

    @Override
    public void onReady(StitchClient stitchClient) {
        this._client = stitchClient;
        this._client.addAuthListener(new MyAuthListener(this));

        _mongoClient = new MongoClient(_client, "mongodb-atlas");
        initLogin();
    }

    // TODO
    public void initLogin() {
        this._client.getAuthProviders().addOnCompleteListener(new OnCompleteListener<AvailableAuthProviders>() {
            @Override
            public void onComplete(Task<AvailableAuthProviders> task) {
                if (task.isSuccessful()) {
                    setupLogin(task.getResult());
                } else {
                    Log.e(TAG, "Error getting auth info", task.getException());
                    // Maybe retry here...
                }
            }
        });
    }

    public void initMoodleView() {
        Log.e(TAG, "initMoodleView: Done"   );
        Intent intent = new Intent(this, MainActivity.class);
        // | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME
        intent.setFlags(intent.getFlags()); // Adds the FLAG_ACTIVITY_NO_HISTORY flag
        startActivity(intent);
//        startActivityForResult(intent,RC_MAIN_APP);
    }


    private static class MyAuthListener implements AuthListener {

        private WeakReference<AuthActivity> _main;

        public MyAuthListener(final AuthActivity activity) {
            _main = new WeakReference<>(activity);
        }

        @Override
        public void onLogin() {
            Log.d(TAG, "Logged into Stitch");
        }

        @Override
        public void onLogout() {
            Log.i(TAG, "onLogout: logging out");
            final AuthActivity activity = _main.get();

            final List<Task<Void>> futures = new ArrayList<>();
            if (activity != null) {
                activity._handler.removeCallbacks(activity._refresher);

                if (activity._googleApiClient != null) {
                    final TaskCompletionSource<Void> future = new TaskCompletionSource<>();
                    GoogleSignInApi.signOut(
                            activity._googleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull final Status ignored) {
                            future.setResult(null);
                        }
                    });
                    futures.add(future.getTask());
                }

                if (activity._fbInitOnce) {
                    LoginManager.getInstance().logOut();
                }

                Tasks.whenAll(futures).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull final Task<Void> ignored) {
                        activity.initLogin();           // Go to First Page
                    }
                });
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            final GoogleSignInResult result = GoogleSignInApi.getSignInResultFromIntent(data);
            handleGooglSignInResult(result);
            return;
        }

//        if(requestCode == RC_MAIN_APP) {
//            finishAndRemoveTask();
//            return;
//        }

        if (_callbackManager != null) {
            _callbackManager.onActivityResult(requestCode, resultCode, data);
            return;
        }
        Log.e(TAG, "Nowhere to send activity result for ourselves");
    }


    private void handleGooglSignInResult(final GoogleSignInResult result) {
        if (result == null) {
            Log.e(TAG, "Got a null GoogleSignInResult");
            return;
        }

        Log.d(TAG, "handleGooglSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            final GoogleAuthProvider googleProvider =
                    GoogleAuthProvider.fromAuthCode(result.getSignInAccount().getServerAuthCode());
            _client.logInWithProvider(googleProvider).addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull final Task<String> task) {
                    if (task.isSuccessful()) {
//                        initTodoView();
                        initMoodleView();
                    } else {
                        Log.e(TAG, "Error logging in with Google", task.getException());
                    }
                }
            });
        }
    }


    private void setupLogin(final AvailableAuthProviders info) {

        if (_client.isAuthenticated()) {
//            initTodoView();
            initMoodleView();
            Log.i(TAG, "setupLogin: Login Completed Previously");
            return;
        }

        final List<Task<Void>> initFutures = new ArrayList<>();

        if (info.hasFacebook()) {
            FacebookSdk.setApplicationId(info.getFacebook().getConfig().getClientId());
            final TaskCompletionSource<Void> fbInitFuture = new TaskCompletionSource<>();
            FacebookSdk.sdkInitialize(getApplicationContext(), new FacebookSdk.InitializeCallback() {
                @Override
                public void onInitialized() {
                    _fbInitOnce = true;
                    fbInitFuture.setResult(null);
                }
            });
            initFutures.add(fbInitFuture.getTask());
        } else {
            FacebookSdk.setApplicationId("INVALID");
            final TaskCompletionSource<Void> fbInitFuture = new TaskCompletionSource<>();
            FacebookSdk.sdkInitialize(getApplicationContext(), new FacebookSdk.InitializeCallback() {
                @Override
                public void onInitialized() {
                    fbInitFuture.setResult(null);
                }
            });
            initFutures.add(fbInitFuture.getTask());
        }

        Tasks.whenAll(initFutures).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(final Void ignored) {
                setContentView(R.layout.activity_auth);

                if (info.hasFacebook()) {
                    findViewById(R.id.fb_login_button).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View ignored) {

                            // Check if already logged in
                            if (AccessToken.getCurrentAccessToken() != null) {
                                final FacebookAuthProvider fbProvider =
                                        FacebookAuthProvider.fromAccessToken(AccessToken.getCurrentAccessToken().getToken());
                                _client.logInWithProvider(fbProvider).addOnCompleteListener(new OnCompleteListener<String>() {
                                    @Override
                                    public void onComplete(@NonNull final Task<String> task) {
                                        if (task.isSuccessful()) {
//                                            initTodoView();
                                            initMoodleView();
                                        } else {
                                            Log.e(TAG, "Error logging in with Facebook", task.getException());
                                        }
                                    }
                                });
                                return;
                            }

                            _callbackManager = CallbackManager.Factory.create();
                            LoginManager.getInstance().registerCallback(_callbackManager,
                                    new FacebookCallback<LoginResult>() {
                                        @Override
                                        public void onSuccess(LoginResult loginResult) {
                                            final FacebookAuthProvider fbProvider =
                                                    FacebookAuthProvider.fromAccessToken(loginResult.getAccessToken().getToken());

                                            _client.logInWithProvider(fbProvider).addOnCompleteListener(new OnCompleteListener<String>() {
                                                @Override
                                                public void onComplete(@NonNull final Task<String> task) {
                                                    if (task.isSuccessful()) {
//                                                        initTodoView();
                                                        initMoodleView();
                                                    } else {
                                                        Log.e(TAG, "Error logging in with Facebook", task.getException());
                                                    }
                                                }
                                            });
                                        }

                                        @Override
                                        public void onCancel() {
                                        }

                                        @Override
                                        public void onError(final FacebookException exception) {
//                                            initTodoView(); TODO something is wrong here, error should mean no login
                                            initMoodleView();
                                        }
                                    });
                            LoginManager.getInstance().logInWithReadPermissions(
                                    AuthActivity.this,
                                    Arrays.asList("public_profile", "email"));
                        }
                    });
                    findViewById(R.id.fb_login_button_frame).setVisibility(View.VISIBLE);
                }

                if (info.hasGoogle()) {
                    final GoogleSignInOptions.Builder gsoBuilder = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestScopes(new Scope(Scopes.EMAIL))
                            .requestServerAuthCode(info.getGoogle().getConfig().getClientId(), false);
                    final GoogleSignInOptions gso = gsoBuilder.build();

                    if (_googleApiClient != null) {
                        _googleApiClient.stopAutoManage(AuthActivity.this);
                        _googleApiClient.disconnect();
                    }

                    _googleApiClient = new GoogleApiClient.Builder(AuthActivity.this)
                            .enableAutoManage(AuthActivity.this, new GoogleApiClient.OnConnectionFailedListener() {
                                @Override
                                public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                                    Log.e(TAG, "Error connecting to google: " + connectionResult.getErrorMessage());
                                }
                            })
                            .addApi(GOOGLE_SIGN_IN_API, gso)
                            .build();

                    findViewById(R.id.google_login_button).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View ignored) {
                            final Intent signInIntent =
                                    GoogleSignInApi.getSignInIntent(_googleApiClient);
                            startActivityForResult(signInIntent, RC_SIGN_IN);
                        }
                    });
                    findViewById(R.id.google_login_button).setVisibility(View.VISIBLE);
                }

                if (info.hasAnonymous()) {
                    findViewById(R.id.anonymous_login_button).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View ignored) {
                            _client.logInWithProvider(new AnonymousAuthProvider()).addOnCompleteListener(new OnCompleteListener<String>() {
                                @Override
                                public void onComplete(@NonNull final Task<String> task) {
                                    if (task.isSuccessful()) {
//                                        initTodoView();
                                        initMoodleView();
                                    } else {
                                        Log.e(TAG, "Error logging in anonymously", task.getException());
                                    }
                                }
                            });
                        }
                    });
                    findViewById(R.id.anonymous_login_button).setVisibility(View.VISIBLE);
                }
            }
        });
    }

}
