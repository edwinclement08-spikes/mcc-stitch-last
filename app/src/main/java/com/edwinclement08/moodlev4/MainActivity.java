package com.edwinclement08.moodlev4;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.edwinclement08.moodlev4.data.userInfo.UserData;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.mongodb.stitch.android.Auth;
import com.mongodb.stitch.android.StitchClient;

import com.mongodb.stitch.android.auth.UserProfile;
import com.mongodb.stitch.android.services.mongodb.MongoClient;

import java.util.Map;

//https://github.com/koush/ion

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, StitchClientListener {

    private StitchClient _client;
    private MongoClient _mongoClient;
    private UserData userData;

    private String TAG = "MoodleV4:MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StitchClientManager.initialize(this.getApplicationContext());
        StitchClientManager.registerListener(this);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

       setUserProfileData();
    }

    public void setUserProfileData()    {
        if(_client != null) {
            if(_client.isAuthenticated())   {
                final Auth user = _client.getAuth();
                if(user != null){
                    user.getUserProfile().addOnCompleteListener(new OnCompleteListener<UserProfile>() {
                        @Override
                        public void onComplete(@NonNull Task<UserProfile> task) {
                            if(task.isSuccessful()) {
                                Log.d(TAG, "onCreate: got a user, and details");
                                UserProfile profile = task.getResult();

                                Map<String, Object> data =  profile.getData();
                                Log.d(TAG, data.toString() );;
                                Log.d(TAG, data.keySet().toString());

//                                ImageView imageView = findViewById(R.id.imageView);

                                String imageURL = profile.getData().get("picture").toString();
//                                Ion.with(imageView)
//                                        .placeholder(R.mipmap.ic_launcher_round)
//                                        .error(R.drawable.ic_menu_camera)
//                                        .load(profile.getData().get("picture").toString());
                                new DownloadImageTask((ImageView) findViewById(R.id.imageView))
                                        .execute(imageURL);

                                final String name = data.get("name").toString();
                                final String email= data.get("email").toString();


                                userData = UserData.getInstance();
                                userData.setName(name);
                                userData.setEmail(email);
                                userData.checkUserData();
//                                Task<String> userTask = userData.getUserData();
//                                userTask.continueWithTask(new Continuation<String, Task<Void>>() {
//                                    @Override
//                                    public Task<Void> then(@NonNull Task<String> task) throws Exception {
//                                        if (task.isSuccessful()) {
//                                            final String result = task.getResult();
//                                            userData.setName(name);
//                                            userData.setEmail(email);
//                                            userData.saveUserData();
//
//                                        }
//                                        return null;
//                                    }
//                                });



                                Log.d(TAG, "onComplete: "+ imageURL);
                                ((TextView)findViewById(R.id.nav_header_name)).setText(data.get("name").toString());  ;

                                ((TextView)findViewById(R.id.nav_header_email)).setText(data.get("email").toString());  ;

                            } else {
                                Log.d(TAG, "onCreate: got a user, BUFGGG");

                            }
                        }
                    });
                } else {
                    Log.e(TAG, "onCreate: User is null....HOW??");
                }
            } else {
                Log.e(TAG, "onCreate: MainActivity ::No user Logged in, how???");
            }
        } else {
            Log.e(TAG, "onCreate: MainActivity ::no stitchClient Available");
        }
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
        if (id == R.id.action_settings) {
            Log.i(TAG, "onOptionsItemSelected: Settings button pressed");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Class fragmentClass = null;

        if (id == R.id.nav_boards) {
            // Handle the Boards action
           setMainFragment(BoardsListFragment.class);

        } else if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_logout)   {

            final MainActivity referenceToContext = this;
            if(_client != null) {
                // StitchClient Exists
                Log.i(TAG, "onNavigationItemSelected: Trying Logout");
                Task<Void> logoutTask = _client.logout();

                logoutTask.addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    // go Back to the login Screen
                        Log.d(TAG, "go back to login Screen"   );
                        Intent intent = new Intent(referenceToContext, AuthActivity.class );
                        // | Intent.FLAG_ACTIVITY_NO_HISTORY
                        intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME); // Adds the FLAG_ACTIVITY_NO_HISTORY flag
                        startActivity(intent);
                        finishAffinity();
                    }
                });


            } else {
                Log.e(TAG, "onNavigationItemSelected: No StitchClient Available for Logging OUT");
            }

        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setMainFragment(Class fragmentClass)   {
        Fragment fragment = null;

        if(fragmentClass != null)   {
            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
// Insert the fragment by replacing any existing fragment
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

            setTitle(((NamedFragments)fragment).getTitle());

        }
    }


    @Override
    public void onReady(StitchClient stitchClient) {
        this._client = stitchClient;

        _mongoClient = new MongoClient(_client, "mongodb-atlas");
        Log.i(TAG, "onReady: StitchClient received in MainActivity");
//        initLogin();

        setMainFragment(BoardsListFragment.class);

    }



}
