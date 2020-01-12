package mygymapp.gymapp;

import android.app.ActionBar;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    /******
     * Beginning of the entire application.
     * Sets up button event listeners driving the rest of the application.
     *****/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("The Gym Application");

        Button btn_UpperBody = findViewById(R.id.btn_UpperBody);
        Button btn_LowerBody = findViewById(R.id.btn_LowerBody);

        btn_LowerBody.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setupIntent(v, GlobalVars.LOWER_BODY);
            }
        });

        btn_UpperBody.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setupIntent(v, GlobalVars.UPPER_BODY);
            }
        });


        //FOR TESTING REASONS, RESETS DATABASE WITH NO DATA.
        //SQLiteDB sqLiteDB = new SQLiteDB(this);
        //SQLiteDatabase db = sqLiteDB.getWritableDatabase();
        //sqLiteDB.onCreate(db);


    }

    /******
     * Sets up the intent for WorkoutListActivity
     *
     * @param v - Current View of the Application
     * @param bodyRegion - What part of the body user selected. This result is queried from the database in the next activity.
     *****/
    private void setupIntent(View v, String bodyRegion){
        Intent intent = new Intent(v.getContext(), WorkoutListActivity.class);
        intent.putExtra("key", bodyRegion);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
