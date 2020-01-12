package mygymapp.gymapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteDB extends SQLiteOpenHelper {

    //Create the database in SQLite
    private static final String DB_NAME = "GymApp.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_WORKOUT = "TABLEWORKOUT";
    public static final String ID = "ID";
    public static final String NAME = "NAME";
    public static final String PICTURE = "PICTURE";
    public static final String WORKOUT_TYPE = "WORKOUT_TYPE";
    public static final String WEIGHT = "WEIGHT";
    public static final String SETS = "SETS";
    public static final String REPS = "REPS";
    public static final String COMMENTS = "COMMENTS";
    public static final String BODY_REGION = "BODY_REGION";
    public static final String COLOR = "COLOR";

    public SQLiteDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    //Create Workout Table in the database
    private static final String CREATE_WORKOUT_TABLE =
            "CREATE TABLE " + TABLE_WORKOUT + "(" +
                    ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    NAME + " TEXT, " +
                    PICTURE + " TEXT, " +
                    WORKOUT_TYPE + " TEXT, " +
                    WEIGHT + " INTEGER, " +
                    SETS + " INTEGER, " +
                    REPS + " INTEGER, " +
                    COMMENTS + " TEXT, " +
                    BODY_REGION + " TEXT, " +
                    COLOR + " TEXT)";

    public void updateDB(SQLiteDatabase db, String query){
        //ContentValues cv = new ContentValues();
        //cv.put("Field1","Bob"); //These Fields should be your String values of actual column names
        //cv.put("Field2","19");
        //cv.put("Field2","Male");

        //db.update(TABLE_WORKOUT, cv,"_id="+id, null);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORKOUT);
        db.execSQL(CREATE_WORKOUT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORKOUT);
        onCreate(db);
    }
}
