package edu.asu.jwilkens;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class DatabaseHandler extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "jwilkens.db";
    // Database folder name will be “Android/Data/CSE535_ASSIGNMENT2” in the SDCARD
    public static final String DATABASE_LOCATION = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator + "Android"
            + File.separator + "Data"
            + File.separator + "CSE535_ASSIGNMENT2"
            +File.separator + DATABASE_NAME;
    private final Context myContext;

    // Database Variables
    private static String TABLE_LABEL = "TableName";
    public static final String COLUMN_T = "TimeStamp";
    public static final String COLUMN_X = "xValues";
    public static final String COLUMN_Y = "yValues";
    public static final String COLUMN_Z = "zValues";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_LOCATION, null, DATABASE_VERSION);
        this.myContext = context;
    }

    public DatabaseHandler(Context context, String database_location) {
        super(context, database_location, null, DATABASE_VERSION);
        this.myContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //db.execSQL(SQL_CREATE_PATIENT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + this.TABLE_LABEL;
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void addPatientTable(String patient_info) {
        this.TABLE_LABEL = patient_info;
        SQLiteDatabase db = this.getWritableDatabase();
        String SQL_CREATE_PATIENT_TABLE =
                "CREATE TABLE IF NOT EXISTS " + patient_info + "(" +
                        this.COLUMN_T + " TEXT NOT NULL," +
                        this.COLUMN_X + " FLOAT NOT NULL," +
                        this.COLUMN_Y + " FLOAT NOT NULL," +
                        this.COLUMN_Z +  " FLOAT NOT NULL)";
        db.execSQL(SQL_CREATE_PATIENT_TABLE);
    }

    public boolean insertPatientData(String patient_info, String time, float x, float y, float z) {
        this.TABLE_LABEL = patient_info;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_T, time);
        contentValues.put(COLUMN_X, x);
        contentValues.put(COLUMN_Y, y);
        contentValues.put(COLUMN_Z, z);
        long result = db.insert(patient_info,null,contentValues);
        if (result == -1) {
            return false;
        }
        else {
            return true;
        }
    }

    public Cursor getAllPatientData(String patient_info)
    {
        TABLE_LABEL = patient_info;
        SQLiteDatabase db =this.getWritableDatabase();
        String SQL_GET_ALL_DATA = "select * from " + patient_info;
        Cursor result = db.rawQuery(SQL_GET_ALL_DATA,null);
        return result;
    }

    /**
    // SQL Statements
    String SQL_CREATE_PATIENT_TABLE =
            "CREATE TABLE IF NOT EXISTS " + this.TABLE_LABEL + " (" +
            DatabaseHandler.COLUMN_T + " TEXT NOT NULL," +
            DatabaseHandler.COLUMN_X + " FLOAT NOT NULL," +
            DatabaseHandler.COLUMN_Y + " FLOAT NOT NULL," +
            DatabaseHandler.COLUMN_Z +  " FLOAT NOT NULL)";
    String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + this.TABLE_LABEL;
    String SQL_GET_ALL_DATA = "select * from " + TABLE_LABEL;
     **/
}
