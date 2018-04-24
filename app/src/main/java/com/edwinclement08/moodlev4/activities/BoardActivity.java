package com.edwinclement08.moodlev4.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;


import com.edwinclement08.moodlev4.R;
import com.edwinclement08.moodlev4.data.selectedBoard.SelectedBoardFragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class BoardActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);




        Fragment fragment = null;
        Class fragmentClass = SelectedBoardFragment.class;

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frameSelectedBoard, fragment).commit();

//        setTitle(((NamedFragments) fragment).getTitle());

//        ((FrameLayout) findViewById(R.id.frameSelectedBoard));


        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");

        df.setTimeZone(tz);
        String nowAsISO = df.format(new Date());
        df.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));
        String nowAsISO2 = df.format(new Date());

//        ((TextView)findViewById(R.id.textView7)).setText(nowAsISO);
//        ((TextView)findViewById(R.id.textView8)).setText(nowAsISO2);





    }

}
