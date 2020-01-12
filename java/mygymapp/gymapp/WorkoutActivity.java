package mygymapp.gymapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;

public class WorkoutActivity extends AppCompatActivity implements IUpdate{

    private int prevSets;
    private int prevReps;
    private float prevWeight;

    EditText edtTxt_Reps;
    EditText edtTxt_Sets;
    EditText edtTxt_Weight;
    EditText edtTxt_Comments;

    /*****
     * Loads information of workout into the activity layout. This requires:
     *      Getting image file and resizing image
     *      Populating fields with workout results.
     *
     * This activity also removes workout from database, along with the image file from the pictures directory.
     *****/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        edtTxt_Reps = (EditText) findViewById(R.id.edtTxt_Reps);
        edtTxt_Sets = (EditText) findViewById(R.id.edtTxt_Sets);
        edtTxt_Weight = (EditText) findViewById(R.id.edtTxt_Weight);
        edtTxt_Comments = (EditText) findViewById(R.id.edtTxt_Comments);

        //Attain workout from previous activity.
        Intent intent = getIntent();
        final Workout workout = (Workout)intent.getSerializableExtra("key");

        //Set Event Listener to update workout values in database. Validate input prior.
        Button btn_SaveWorkout = findViewById(R.id.btn_SaveWorkout);
        btn_SaveWorkout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(!validateInput()){
                    return;
                }
                updateWorkout(workout);
                killActivity(workout);
            }
        });

        populateData(workout);

        //Add event listener to remove workout from the database, along with image from the picture directory of the program.
        //This also kills this activity and loads WorkoutListActivity.
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Remove Workout From Database
                String query = "DELETE FROM " + SQLiteDB.TABLE_WORKOUT + " WHERE " + SQLiteDB.NAME + " = '" + workout.getName() + "'"; // AND " + SQLiteDB.BODY_TYPE + " = '" + workout.getBodyType() + "'";

                SQLiteDB sqlDB = new SQLiteDB(view.getContext());
                SQLiteDatabase db = sqlDB.getWritableDatabase();

                db.execSQL(query);

                db.close();
                sqlDB.close();

                //Remove Image File From Pictures Directory
                File fileToDelete = getExternalFilesDir(Environment.DIRECTORY_PICTURES  + File.separator + workout.getImage() + GlobalVars.PIC_EXT);
                if(fileToDelete.exists()){
                    fileToDelete.delete();
                }

                killActivity(workout);
            }
        });
    }

    /*****
     * Kills this activity and loads WorkoutListActivity with the proper body region
     *
     * @param workout - Workout being displayed.
     *****/
    private void killActivity(Workout workout){
        //Kill This Activity, Load WorkoutListActivity
        Intent intent = new Intent(getApplicationContext(), WorkoutListActivity.class);
        intent.putExtra("key", workout.getBodyRegion());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    /*****
     * This ensures Reps, Sets, and Weight have values. Prompts user if a field is empty.
     *
     * @return boolean - Are the inputs empty or not
     */
    private boolean validateInput(){

        if(edtTxt_Reps.getText().equals("") || edtTxt_Sets.getText().equals("") || edtTxt_Weight.getText().equals("")){
            Toast.makeText(getApplicationContext(), "Please enter input for Sets, Reps, and Weight.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /*****
     * Populates the view with the workout data, along with the workout image and possible extension image.
     * Also assigns class values with previous set, rep, and weight values for comparison later.
     *
     * @param workout - The workout populating the view
     ******/
    private void populateData(Workout workout){
        //Create file reference for workout image.
        File imageFile = getExternalFilesDir(Environment.DIRECTORY_PICTURES  + File.separator + workout.getName() + GlobalVars.PIC_EXT);
        ImageView imgVw_Workout = (ImageView) findViewById(R.id.imgVw_Workout);

        //If image file exists, create a scaled bitmap to display.
        //NOTE: recycle the first bitmap to save on memory.
        if(imageFile.exists()){
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, false);
            bitmap.recycle();

            Matrix matrix = new Matrix();
            matrix.postRotate(90);

            Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

            imgVw_Workout.setImageBitmap(rotatedBitmap);
        }else{
            imgVw_Workout.setImageResource(R.mipmap.ic_launcher);
        }

        //Set title of workout.
        getSupportActionBar().setTitle(workout.getName() + " - " + workout.getWorkoutType());

        //Set Reps, Sets, Weight, and Comments
        edtTxt_Reps.setText(String.valueOf(workout.getReps()));
        edtTxt_Sets.setText(String.valueOf(workout.getSets()));
        edtTxt_Weight.setText(String.valueOf(workout.getWeight()));
        edtTxt_Comments.setText(workout.getComment());

        //Hold workout values in class vars for later comparison.
        prevReps = workout.getReps();
        prevSets = workout.getSets();
        prevWeight = workout.getWeight();
    }

    /*****
     * Updates the workout row in the database. It will first compare the previous workout values with the new values
     * to determine the new workout color. It will then update the database.
     *
     * @param workout - workout being updated in the database
     *****/
    @Override
    public void updateWorkout(Workout workout) {
        //Get the new values to update to the database
        int newSets = Integer.parseInt(edtTxt_Sets.getText().toString());
        int newReps = Integer.parseInt(edtTxt_Reps.getText().toString());
        float newWeight = Float.parseFloat(edtTxt_Weight.getText().toString());
        String newComment = edtTxt_Comments.getText().toString();
        String newColor = GlobalVars.GRAY; //default color

        //Compare old vs new values to determine workout color
        if((newSets <= prevSets && newSets >= 4) && (newReps <= prevReps && newReps >= 4) && (newWeight > prevWeight)) {
            newColor = GlobalVars.GREEN;
        }
        else if(newSets == prevSets && newReps == prevReps && newWeight == prevWeight){
            newColor = GlobalVars.YELLOW;
        }else{
            newColor = GlobalVars.RED;
        }

        //Connect to the database
        SQLiteDB sqlDB = new SQLiteDB(getApplicationContext());
        SQLiteDatabase db = sqlDB.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(SQLiteDB.SETS, newSets);
        cv.put(SQLiteDB.REPS, newReps);
        cv.put(SQLiteDB.WEIGHT, newWeight);
        cv.put(SQLiteDB.COMMENTS, newComment);
        cv.put(SQLiteDB.COLOR, newColor);

        //Update row in database
        db.update(SQLiteDB.TABLE_WORKOUT, cv,SQLiteDB.NAME + "='" + workout.getName() + "'", null);

        //Close database
        sqlDB.close();
        db.close();
    }
}
