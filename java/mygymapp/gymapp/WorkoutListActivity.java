package mygymapp.gymapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class WorkoutListActivity extends AppCompatActivity {

    final int PERMISSION_REQUEST_PICS = 1;

    /*****
     * Driver method for this class.
     * Attains workouts from the database based on user input from MainActivity.
     * Displays the workouts in a list.
     *****/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Choose Machine");

        //Attain user input from MainActivity
        Intent intent = getIntent();
        final String bodyRegion = ((Intent) intent).getStringExtra("key");

        //Query Database for Workout Results
        final ArrayList<Workout> workoutList = getWorkoutList(bodyRegion);

        //Iteratively create layout for each workout.
        setupWorkoutLayout(workoutList);

        //Setup event listener to create a new workout for the current body region
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), NewWorkoutActivity.class);
                intent.putExtra("key", bodyRegion);
                startActivity(intent);
            }
        });
    }

    /*****
     * Designs the layout for each workout in this activity. This requires:
     *      Attaining the workout picture (this needs folder permissions)
     *      Customized layout settings and events
     *
     * @param workoutList - Workouts queried from the database
     *****/
    private void setupWorkoutLayout(ArrayList<Workout> workoutList){
        //Get Parent Linear Layout in Activity
        LinearLayout ll_Machines = findViewById(R.id.ll_Machines);

        //Create the layout for each workout.
        for(int i = 0; i < workoutList.size(); i++){

            final Workout w = workoutList.get(i);

            //Create & set image for each workout
            File file = getExternalFilesDir(Environment.DIRECTORY_PICTURES  + File.separator + w.getName() + GlobalVars.PIC_EXT);
            ImageView imageView = new ImageView(this);

            //Verify picture exists. If it does, we must validate folder permissions.
            if(file.exists()){
                //First, validate permission to read/write to pictures on the device.
                validatePicPermissions();

                //Set imageview with a scaled bitmap.
                imageView.setImageBitmap(createScaledBitmap(file));
            }else{
                //default imageview if scaled bitmap cannot be attained.
                imageView.setImageResource(R.mipmap.ic_launcher);
            }

            //Set padding to better allow spacing from edge of device, and set size of the imageview.
            imageView.setPadding(20, 0, 0, 0);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(300, 300));

            //Create & Set Textbox for each workout
            final TextView textView = new TextView(this);
            textView.setText(workoutList.get(i).getName() + " - " + workoutList.get(i).getWorkoutType());
            textView.setTextSize(24);
            textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            textView.setGravity(Gravity.CENTER);

            //Layout for each workout
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setGravity(Gravity.CENTER);
            linearLayout.setBackgroundColor(Color.parseColor(workoutList.get(i).getColor()));
            linearLayout.getBackground().setAlpha(160);

            //Set Margins between each layout
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 350);
            layoutParams.setMargins(0, 10, 0, 0);

            //Add elements to the linear layout
            linearLayout.addView(imageView, 0);
            linearLayout.addView(textView, 1);

            //Add OnClickListener to the Linear Layout
            linearLayout.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    Intent intent = new Intent(view.getContext(), WorkoutActivity.class);
                    intent.putExtra("key", w);
                    startActivity(intent);
                }
            });

            //Add layout to the parent layout control
            ll_Machines.addView(linearLayout, layoutParams);
        }
    }

    /*****
     * Attains the jpg image, creates 2 bitmaps, the first is the jpg file, the second is a scaled version of the 1st bitmap.
     *
     * @param file - The .jpg file used to create the bitmap.
     * @return - a scaled bitmap version of the jpg file.
     *
     * NOTE: To save memory, recycle the 1st bitmap.
     */
    private Bitmap createScaledBitmap(File file){
        //Setup bitmap image. To reduce memory use, recycle the original bitmap image once the scaledBitmap is created.
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, false);
        bitmap.recycle();

        Matrix matrix = new Matrix();
        matrix.postRotate(90);

        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

        return rotatedBitmap;
    }

    /*****
     * Ensures we have read/write permissions to the device's sd card
     *****/
    private void validatePicPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_PICS);
        }
    }

    /*****
     * Queries the database, and assigns results to their assigned classes.
     *
     * @param bodyRegion - the user input from MainActivity. This drives what results we attain from the database.
     * @return ArrayList<Workout> - a list of workouts resulted from the query.
     */
    private ArrayList<Workout> getWorkoutList(String bodyRegion){

        ArrayList<Workout> workoutList = new ArrayList<Workout>();
        String query = "SELECT * FROM " + SQLiteDB.TABLE_WORKOUT + " WHERE " + SQLiteDB.BODY_REGION + " = '" + bodyRegion + "' ORDER BY " + SQLiteDB.NAME;

        //Connect to the database
        SQLiteDB sqlDB = new SQLiteDB(this);
        SQLiteDatabase db = sqlDB.getWritableDatabase();

        //Create an iterative cursor for the query results, and assign results to their classes.
        Cursor cursor = db.rawQuery(query, null);
        while (cursor.moveToNext()) {
            if(cursor.getString(3).equals(GlobalVars.CIRCUIT)){
                CircuitWorkout cw = new CircuitWorkout();
                cw.setId(cursor.getInt(0));
                cw.setName(cursor.getString(1));
                cw.setImage(cursor.getString(2));
                cw.setWorkoutType(cursor.getString(3));
                cw.setWeight(cursor.getInt(4));
                cw.setSets(cursor.getInt(5));
                cw.setReps(cursor.getInt(6));
                cw.setComment(cursor.getString(7));
                cw.setBodyRegion(cursor.getString(8));
                cw.setColor(cursor.getString(9));
                workoutList.add(cw);
            }else if(cursor.getString(3).equals(GlobalVars.FREEWEIGHT)){
                FreeWeightWorkout fw = new FreeWeightWorkout();
                fw.setId(cursor.getInt(0));
                fw.setName(cursor.getString(1));
                fw.setImage(cursor.getString(2));
                fw.setWorkoutType(cursor.getString(3));
                fw.setWeight(cursor.getInt(4));
                fw.setSets(cursor.getInt(5));
                fw.setReps(cursor.getInt(6));
                fw.setComment(cursor.getString(7));
                fw.setBodyRegion(cursor.getString(8));
                fw.setColor(cursor.getString(9));
                workoutList.add(fw);
            }
        }

        //Close the database connections
        db.close();
        sqlDB.close();

        return workoutList;
    }

    /*****
     * Always take the user to the Main Activity when the back button is pressed on this view.
     *****/
    @Override
    public void onBackPressed(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}
