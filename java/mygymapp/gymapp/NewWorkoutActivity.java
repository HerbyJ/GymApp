package mygymapp.gymapp;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NewWorkoutActivity extends AppCompatActivity implements IValidate{

    private static final int TAKE_WORKOUT_PHOTO = 1;
    private File newImageFile;

    /*****
     * Driver method for this activity.
     * Attains previous activity's results. This is needed to know what type of data to add to the database.
     * Creates a file for the jpg photo
     * Saves the jpg photo to the newly created file
     * Validates and Adds new data record to the database.
    *****/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_workout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add New Machine");

        //Attain previous activy's results passed to this activity. This will drive what type the new workout is for the database.
        Intent intent = getIntent();
        final String bodyRegion = ((Intent) intent).getStringExtra("key");

        //Create on Event Listener to create an image file, and save a jpg image to that file.
        Button btn_TakePic = (Button) findViewById(R.id.btn_TakePic);
        btn_TakePic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                //Create an Intent and ensure that there's a camera activity to handle the intent
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

                    // Create the file where the photo should go.
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        photoFile = null;
                        Toast.makeText(getApplicationContext(), "Could not make image file.", Toast.LENGTH_SHORT).show();
                    }

                    // Save image to the file if creating the file was successful
                    if (photoFile != null) {
                        newImageFile = photoFile;
                        Uri photoURI = FileProvider.getUriForFile(view.getContext(),"mygymapp.gymapp.fileprovider", photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, TAKE_WORKOUT_PHOTO);
                    }
                }
            }
        });

        //Create an Event Listener to add a new workout to the database.
        Button btn_AddWorkout = (Button) findViewById(R.id.btn_AddWorkout);
        btn_AddWorkout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                //Attain fields holding data for database
                EditText edtTxt_WorkoutName = (EditText) findViewById(R.id.edtTxt_WorkoutName);
                RadioButton rdBtn_FreeWeight = (RadioButton) findViewById(R.id.rdBtn_FreeWeight);
                RadioButton rdBtn_Circuit = (RadioButton) findViewById(R.id.rdBtn_Circuit);

                String workoutName = edtTxt_WorkoutName.getText().toString();
                String picName = edtTxt_WorkoutName.getText().toString();

                //Validate user input.
                if(!nameVal(workoutName) || !bodyTypeVal(rdBtn_Circuit, rdBtn_FreeWeight) || !picVal(picName)){
                    return;
                }

                //Attain radio button results
                String workoutType = "";
                if(rdBtn_FreeWeight.isChecked()){
                    workoutType = "Freeweight";
                }else if(rdBtn_Circuit.isChecked()){
                    workoutType = "Circuit";
                }

                //Add new workout to the database.
                insertRecordToDB(view, workoutName, picName, workoutType, bodyRegion);

                //End this activity, go to WorkoutListActivity
                Intent intent = new Intent(view.getContext(), WorkoutListActivity.class);
                intent.putExtra("key", bodyRegion);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    /*****
     * Inserts new data record into the database.
     *****/
    private void insertRecordToDB(View view, String name, String picName, String workoutType, String bodyRegion){
        //Connect to the database.
        SQLiteDB sqlDB = new SQLiteDB(view.getContext());
        SQLiteDatabase db = sqlDB.getWritableDatabase();

        //Contact Database, Insert new values.
        String query = "INSERT INTO " + sqlDB.TABLE_WORKOUT + "(NAME, PICTURE, WORKOUT_TYPE, WEIGHT, SETS, REPS, COMMENTS, BODY_REGION, COLOR) " +
                "VALUES ('" + name + "', '" + picName + "', '" + workoutType + "', 0, 0, 0, 'No Comment', '" + bodyRegion + "', '" + GlobalVars.GRAY + "')";

        //Execute the query
        db.execSQL(query);

        //Close database connections.
        db.close();
        sqlDB.close();
    }

    /*****
     * Creates a bitmap and scaled bitmap of jpg image created from camera.
     *****/
    public void onActivityResult(int requestCode, int resultCode, Intent data){

        ImageView imgVw_NewPicture = (ImageView) findViewById(R.id.imgVw_NewPicture);

        //Validate the jpg file was saved. If it was, create the bitmap, and a scaled bitmap image.
        //LG30 rotates the scaledbitmap, use Matrix to rotate the image 90 degrees.
        if(newImageFile.exists()){
            Bitmap bitmap = BitmapFactory.decodeFile(newImageFile.getAbsolutePath());
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, false);

            Matrix matrix = new Matrix();
            matrix.postRotate(90);

            Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

            bitmap.recycle();
            imgVw_NewPicture.setImageBitmap(rotatedBitmap);
        }else{
            imgVw_NewPicture.setImageResource(R.mipmap.ic_launcher);
        }
    }

    /******
     * Creates the jpg file in the programs directory.
     *
     * @return File - the newly created file.
     ******/
    private File createImageFile() throws IOException {
        // Create an image file name
        EditText edtTxt_Name = (EditText) findViewById(R.id.edtTxt_WorkoutName);

        String name = edtTxt_Name.getText().toString();
        String imageFileName = name;

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir, imageFileName + GlobalVars.PIC_EXT);

        image.createNewFile();

        return image;
    }

    /*****
     * Attains the jpg image, creates 2 bitmaps, the first is the jpg file, the second is a scaled version of the 1st bitmap.
     *
     * @param file - The .jpg file used to create the bitmap.
     * @return - a scaled bitmap version of the jpg file.
     *
     * NOTE: To save memory, recycle the 1st bitmap.
     *****/
    private Bitmap createScaledBitmap(File file){
        //Setup bitmap image. To reduce memory use, recycle the original bitmap image once the scaledBitmap is created.
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, false);
        bitmap.recycle();

        return scaledBitmap;
    }


    /******
     * Validates name field is populated.
     * Validates name does not currently exist in the database
     *
     * @param name - Name of the workout about to be saved.
     * @return - a boolean value (True = Validation Passed, False = Validation Failed)
     *****/
    @Override
    public boolean nameVal(String name) {
        //Ensure name has a value.
        if(name.equals("")){
            Toast.makeText(getApplicationContext(), "Please enter a workout name.", Toast.LENGTH_LONG).show();
            return false;
        }

        //Connect to the database.
        SQLiteDB sqlDB = new SQLiteDB(getApplicationContext());
        SQLiteDatabase db = sqlDB.getWritableDatabase();

        //Contact Database, Insert new values.
        String query = "SELECT " + sqlDB.NAME + " FROM " + sqlDB.TABLE_WORKOUT + " WHERE " + sqlDB.NAME + " = '" + name + "'";
        Cursor cursor = db.rawQuery(query, null);

        //If a result is found, we must rename the workout and try again.
        if (cursor.moveToNext()) {
            //Close database connections.
            db.close();
            sqlDB.close();

            Toast.makeText(getApplicationContext(), "Name is being used, Please rename the workout.", Toast.LENGTH_LONG).show();
            return false;
        }

        //Close database connections.
        db.close();
        sqlDB.close();
        return true;
    }

    /*****
     * Validate a workout type has been checked.
     *
     * @param circuit - A radiobutton for Circuit Workout
     * @param freeWgt - A radiobutton for Freeweight Workout
     * @return - a boolean value (True = Validation Passed, False = Validation Failed)
     *****/
    @Override
    public boolean bodyTypeVal(RadioButton circuit, RadioButton freeWgt) {
        if(!circuit.isChecked() && !freeWgt.isChecked()){
            Toast.makeText(getApplicationContext(), "Please choose the type of workout.", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    /*****
     * Validate the picture filename is not empty.
     *
     * @param fileName - filename of the image
     * @return - a boolean value (True = Validation Passed, False = Validation Failed)
     *****/
    @Override
    public boolean picVal(String fileName) {
        if(newImageFile == null){
            Toast.makeText(getApplicationContext(), "Please take a image to continue.", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}
