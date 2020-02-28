package edu.asu.jwilkens;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "jwilkens.db";

    // Database Variables
    private static String TABLE_LABEL = "Name_ID_Age_Sex";
    private static final String COLUMN_T = "Time Stamp";
    private static final String COLUMN_X = "X Values";
    private static final String COLUMN_Y = "Y Values";
    private static final String COLUMN_Z = "Z Values";

    // Database folder name will be “Android/Data/CSE535_ASSIGNMENT2” in the SDCARD
    public DatabaseHandler(Context context) {
        //super(context, Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Android"+ File.separator+ "Data"+File.separator+ "CSE535_ASSIGNMENT2"+File.separator +DB_name, null,2);
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PATIENT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void addPatientTable(String name, int id, int age, String sex) {
        SQLiteDatabase db = this.getWritableDatabase();
        //SQL Query for creation
        DatabaseHandler.TABLE_LABEL = name + "_" + id + "_" + age + "_" + sex;
        db.execSQL(SQL_CREATE_PATIENT_TABLE);
    }

    public boolean insertPatientData(String patientTableName, String time, double x, double y, double z) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_T, time);
        contentValues.put(COLUMN_X, x);
        contentValues.put(COLUMN_Y, y);
        contentValues.put(COLUMN_Z, z);
        long result = db.insert(patientTableName,null,contentValues);
        if (result == -1) {
            return false;
        }
        else {
            return true;
        }
    }

    // SQL Statements
    String SQL_CREATE_PATIENT_TABLE =
            "CREATE TABLE " + DatabaseHandler.TABLE_LABEL + " (" +
            DatabaseHandler.COLUMN_T + " INTEGER NOT NULL," +
            DatabaseHandler.COLUMN_X + " FLOAT NOT NULL," +
            DatabaseHandler.COLUMN_Y + " FLOAT NOT NULL," +
            DatabaseHandler.COLUMN_Z +  " FLOAT NOT NULL)";
    String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + DatabaseHandler.TABLE_LABEL;
}
