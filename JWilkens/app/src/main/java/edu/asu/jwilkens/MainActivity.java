package edu.asu.jwilkens;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    // Graph related values
    private GraphView health_graph_x, health_graph_y, health_graph_z;
    private float[] graph_values;
    private static String[] graph_horlabels = new String[]{"0", "5", "10", "15", "20"};
    private static String[] graph_verlabels = {"20", "0", "-20"};
    private static String graph_title = "Health Graph UI";
    // State of data variables
    private int current_location;
    int startTime = 0;
    int size = 0;
    private float[] graph_values_y, graph_values_x, graph_values_z;
    private ArrayList<Float> graph_values_array_x, graph_values_array_y, graph_values_array_z;
    protected String patient_id, patient_age, patient_name, patient_sex;
    // Sensor Variables
    private SensorManager sensorManager;
    private Sensor sensor;
    boolean running = false;
    static int ACCE_FILTER_DATA_MIN_TIME = 1000; // 1000ms
    long lastSaved = System.currentTimeMillis();
    float sensor_x, sensor_y, sensor_z;
    // Database information
    File database_file_down;
    DatabaseHandler PATIENT_DATABASE;
    private final String SERVER_URL = "http://localserver:5000";
    public final String DATABASE_NAME = DatabaseHandler.DATABASE_NAME;
    protected String patient_table_name;
    DatabaseHandler PATIENT_DATABASE_DOWNLOADED;
    // UI Related Elements
    private RelativeLayout graph_constraint_x, graph_constraint_y, graph_constraint_z;
    private Button run_button, stop_button, upload_button, download_button;
    EditText edit_patient_name, edit_patient_age, edit_patient_id;

    /**
     * Activity on Create, initialization!
     *
     * @param savedInstanceState Activity Instance for App.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PATIENT_DATABASE = new DatabaseHandler(this);
        setContentView(R.layout.activity_main);
        run_button = (Button) findViewById(R.id.button_run);
        stop_button = (Button) findViewById(R.id.button_stop);
        upload_button = (Button) findViewById(R.id.button_upload);
        download_button = (Button) findViewById(R.id.button_download);
        edit_patient_name = (EditText) findViewById(R.id.patient_name);
        edit_patient_age = (EditText) findViewById(R.id.patient_age);
        edit_patient_id = (EditText) findViewById(R.id.patient_id);
        graph_constraint_x = (RelativeLayout) findViewById(R.id.graph_constraint_x);
        graph_constraint_y = (RelativeLayout) findViewById(R.id.graph_constraint_y);
        graph_constraint_z = (RelativeLayout) findViewById(R.id.graph_constraint_z);
        createGraphViews();
        graph_constraint_x.addView(health_graph_x);
        graph_constraint_y.addView(health_graph_y);
        graph_constraint_z.addView(health_graph_z);
        //drawRandom = null;
        graph_values_array_x = new ArrayList<Float>();
        graph_values_array_y = new ArrayList<Float>();
        graph_values_array_z = new ArrayList<Float>();
        patient_sex = null;
        patient_name = null;
        patient_age = null;
        patient_id = null;

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            if (sensor == null) {
                sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            }
        } else {
            Toast toast = Toast.makeText(this, "No Accelerometer!", Toast.LENGTH_LONG);
            toast.show();
        }
        current_location = 0;

        stop_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (running) {
                    running = false;
                    sensorManager.unregisterListener(MainActivity.this, sensor);
                    resetGraphView();
                } else {
                    Toast toast = Toast.makeText(MainActivity.this, "Not Running!", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        run_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!CheckExternalStorageAvail()) {
                    Toast toast = Toast.makeText(MainActivity.this, "No External Storage!", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                if (checkPatientData()) {
                    if (!running) {
                        try {
                            String patient_table_name_temp = patient_table_name;
                            patient_table_name = edit_patient_name.getText().toString() + "_"
                                    + edit_patient_id.getText().toString() + "_"
                                    + edit_patient_age.getText().toString() + "_"
                                    + patient_sex;
                            if (patient_table_name_temp != null && !patient_table_name_temp.equals(patient_table_name)) {
                                startTime = 0;
                                size = 0;
                                current_location = 0;
                                graph_values_array_x = new ArrayList<Float>();
                                graph_values_array_y = new ArrayList<Float>();
                                graph_values_array_z = new ArrayList<Float>();
                            }
                            PATIENT_DATABASE.addPatientTable(patient_table_name);
                            Toast toast = Toast.makeText(MainActivity.this, patient_table_name, Toast.LENGTH_SHORT);
                            toast.show();
                            running = true;
                            updateGraphView();
                        }
                        catch (Exception e) {
                            Toast toast = Toast.makeText(MainActivity.this, "Error!", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    } else {
                        Toast toast = Toast.makeText(MainActivity.this, "Already Running!", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } else {
                    Toast toast = Toast.makeText(MainActivity.this, "Enter Patient Info!", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        upload_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new UploadDatabaseFile().execute();
            }
        });

        download_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (checkPatientData()) {
                    if (!running) {
                        try {
                            patient_table_name = edit_patient_name.getText().toString() + "_"
                                    + edit_patient_id.getText().toString() + "_"
                                    + edit_patient_age.getText().toString() + "_"
                                    + patient_sex;
                            running = true;
                            new DownloadDatabaseFile().execute();
                        }
                        catch (Exception e) {
                            Toast toast = Toast.makeText(MainActivity.this, "Error!", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    } else {
                        Toast toast = Toast.makeText(MainActivity.this, "Please stop before download!", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } else {
                    Toast toast = Toast.makeText(MainActivity.this, "Enter Patient Info!", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }

    /**
     * Adopted from developer.android.com for:
     * ***** Listening for patient sex.
     *
     * @param view Currrent View
     */
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radio_female:
                if (checked)
                    patient_sex = "Female";
                break;
            case R.id.radio_male:
                if (checked)
                    patient_sex = "Male";
                break;
        }
    }

    /**
     * Update w/ check for interval. If passed, calls UI update.
     *
     * @param event Accelerometer Sensor Event.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if ((System.currentTimeMillis() - lastSaved) > ACCE_FILTER_DATA_MIN_TIME) {
                lastSaved = System.currentTimeMillis();
                sensor_x = event.values[0];
                sensor_y = event.values[1] - (float) 9.81;
                sensor_z = event.values[2];
                PATIENT_DATABASE.insertPatientData(patient_table_name, String.valueOf(current_location), sensor_x, sensor_y, sensor_z);
                new UpdateGraphs().execute();
            }
        }
    }

    /**
     * Required Override for sensor Accuracy Changes
     * ***** Empty!
     *
     * @param sensor   Accelerometer Sensor
     * @param accuracy Accuracy Value
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    protected Boolean checkPatientData() {
        return (patient_sex != null &&
                !edit_patient_name.getText().toString().equals("") &&
                !edit_patient_age.getText().toString().equals("") &&
                !edit_patient_id.getText().toString().equals(""));
    }

    /**
     * The section below handles all graphs views! This includes:
     * ***** Creating initial Graphs
     * ***** Creating a listener when run is called.
     * ***** A response for callback when sensor updates.
     * ***** A reset graph view to zero out any data from UI only.
     * ***** Redraw Graphs from Sensor Updates!
     */
    private void createGraphViews() {
        /* Data */
        graph_values = new float[0];
        health_graph_x = new GraphView(this, graph_values, graph_title + " X", graph_horlabels, graph_verlabels, GraphView.LINE);
        health_graph_x.setLayoutParams(new ConstraintLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
        health_graph_x.setBackgroundColor(Color.BLACK);
        health_graph_y = new GraphView(this, graph_values, graph_title + " Y", graph_horlabels, graph_verlabels, GraphView.LINE);
        health_graph_y.setLayoutParams(new ConstraintLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
        health_graph_y.setBackgroundColor(Color.BLACK);
        health_graph_z = new GraphView(this, graph_values, graph_title + " Z", graph_horlabels, graph_verlabels, GraphView.LINE);
        health_graph_z.setLayoutParams(new ConstraintLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
        health_graph_z.setBackgroundColor(Color.BLACK);
    }

    /**
     * Called when run button is detected.
     */
    public void updateGraphView() {
        /* Data */
        startTime = current_location;
        graph_horlabels = new String[]{Integer.toString(startTime), Integer.toString(startTime + 5),
                Integer.toString(startTime + 10), Integer.toString(startTime + 10), Integer.toString(startTime + 20)};
        size = 0;
        new ActivateSensor().execute();
    }

    /**
     * Called when Download button is detected.
     */
    public void updateGraphViewDownload() {
        /* Data */
        graph_horlabels = new String[]{Integer.toString(startTime), Integer.toString(startTime + 5),
                Integer.toString(startTime + 10), Integer.toString(startTime + 10), Integer.toString(startTime + 20)};
        int newStart = 0;
        graph_values_x = new float[size - newStart];
        graph_values_y = new float[size - newStart];
        graph_values_z = new float[size - newStart];
        for (int i = 0; i < graph_values_x.length; i++) {
            graph_values_x[i] = graph_values_array_x.get(i + newStart);
            graph_values_y[i] = graph_values_array_y.get(i + newStart);
            graph_values_z[i] = graph_values_array_z.get(i + newStart);
        }
        health_graph_x.setValues(graph_values_x, graph_horlabels);
        health_graph_x.invalidate();
        health_graph_y.setValues(graph_values_y, graph_horlabels);
        health_graph_y.invalidate();
        health_graph_z.setValues(graph_values_z, graph_horlabels);
        health_graph_z.invalidate();
    }

    /**
     * Called from update from sensor is passed.
     */
    public void updateGraphViewRegistered() {
        /* Data */
        size++;
        graph_values_array_x.add(sensor_x);
        graph_values_array_y.add(sensor_y);
        graph_values_array_z.add(sensor_z);
        int newStart = 0;
        if (size > 20) {
            double temp = Math.floorDiv(size, 20);
            newStart = (int) temp;
            newStart = (int) (temp * 20);
            graph_horlabels = new String[]{Integer.toString(newStart), Integer.toString(newStart + 5),
                    Integer.toString(newStart + 10), Integer.toString(newStart + 10), Integer.toString(newStart + 20)};
            graph_values_x = new float[size - newStart];
            graph_values_y = new float[size - newStart];
            graph_values_z = new float[size - newStart];
        } else {
            newStart = startTime;
            graph_values_x = new float[size];
            graph_values_y = new float[size];
            graph_values_z = new float[size];
        }
        for (int i = 0; i < graph_values_x.length; i++) {
            graph_values_x[i] = graph_values_array_x.get(i + newStart);
            graph_values_y[i] = graph_values_array_y.get(i + newStart);
            graph_values_z[i] = graph_values_array_z.get(i + newStart);
        }
        health_graph_x.setValues(graph_values_x, graph_horlabels);
        health_graph_x.invalidate();
        health_graph_y.setValues(graph_values_y, graph_horlabels);
        health_graph_y.invalidate();
        health_graph_z.setValues(graph_values_z, graph_horlabels);
        health_graph_z.invalidate();
        current_location++;
    }

    /**
     * Called to reset graph UI.
     */
    public void resetGraphView() {
        graph_values_x = new float[0];
        graph_values_y = new float[0];
        graph_values_z = new float[0];
        graph_horlabels = new String[]{"0", "5", "10", "15", "20"};
        health_graph_x.setValues(graph_values_x, graph_horlabels);
        health_graph_x.invalidate();
        health_graph_y.setValues(graph_values_y, graph_horlabels);
        health_graph_y.invalidate();
        health_graph_z.setValues(graph_values_z, graph_horlabels);
        health_graph_z.invalidate();
    }

    /**
     * Async Task to re-draw graphs!
     */
    private class UpdateGraphs extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                updateGraphViewRegistered();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * Async Task to start sensor!
     */
    private class ActivateSensor extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                sensorManager.registerListener(MainActivity.this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * Check External Storage
     */
    private boolean CheckExternalStorageAvail() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Download Database Async Task
     */
    public class DownloadDatabaseFile extends AsyncTask<Void, Void, Void> {
        public final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + "Android"
                + File.separator + "Data"
                + File.separator + "CSE535_ASSIGNMENT2_DOWN"
                + File.separator;
        File storage_folder = null;
        File download_db_file = null;
        private final String TAG = "Download Task";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this, "Download Started!", Toast.LENGTH_SHORT).show();
            if (CheckExternalStorageAvail()) {
                storage_folder = new File(DOWNLOAD_PATH);
            } else
                Toast.makeText(MainActivity.this, "External Storage Not Avail!", Toast.LENGTH_SHORT).show();
            if (!storage_folder.exists()) {
                storage_folder.mkdir();
            }
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                int count;
                URL url = new URL(SERVER_URL);
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.connect();
                if (c.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "Server returned HTTP " + c.getResponseCode() + " " + c.getResponseMessage());
                }
                int lenghtOfFile = c.getContentLength();
                database_file_down = new File(storage_folder, DATABASE_NAME);
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(database_file_down);
                byte data[] = new byte[1024];
                long total = 0;
                try {
                    while ((count = input.read(data)) != -1) {
                        total += count;
                        // writing data to file
                        output.write(data, 0, count);
                    }
                } catch (Exception E) {
                    Log.e(TAG, "Server buffer nto donez ");
                }

                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                download_db_file = null;
                Log.e(TAG, "Error Do in BKGRD." + e);
            }
            PATIENT_DATABASE_DOWNLOADED = new DatabaseHandler(MainActivity.this,database_file_down.getAbsolutePath());
            try {
                Cursor patient_info =  PATIENT_DATABASE_DOWNLOADED.getAllPatientData(patient_table_name);
                //Toast toast = Toast.makeText(MainActivity.this, "Patient has row count: " + patient_info.getCount(), Toast.LENGTH_SHORT);
                //toast.show();
                size = 0;
                current_location = 0;
                graph_values_array_x = new ArrayList<Float>();
                graph_values_array_y = new ArrayList<Float>();
                graph_values_array_z = new ArrayList<Float>();

                int index = 0;
                if ( patient_info.getCount() >= 10) {
                    index = patient_info.getCount() - 10;
                }

                patient_info.moveToPosition(index);
                startTime = index;
                while (!patient_info.isAfterLast()) {
                    graph_values_array_x.add(patient_info.getFloat(patient_info.getColumnIndex(DatabaseHandler.COLUMN_X)));
                    graph_values_array_y.add(patient_info.getFloat(patient_info.getColumnIndex(DatabaseHandler.COLUMN_Y)));
                    graph_values_array_z.add(patient_info.getFloat(patient_info.getColumnIndex(DatabaseHandler.COLUMN_Z)));
                    patient_info.moveToNext();
                    index++;
                    size++;
                    current_location++;
                }
                updateGraphViewDownload();
            } catch (Exception e) {
                Log.e(TAG, "Patient May not exist in DL file!.");
                //Toast toast = Toast.makeText(MainActivity.this, "Download Complete!", Toast.LENGTH_SHORT);
                //toast.show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //Parse DB


            // Notifications
            Log.e(TAG, "Reached Post Execute!.");
            Toast toast = Toast.makeText(MainActivity.this, "Download Complete!", Toast.LENGTH_SHORT);
            toast.show();
            running = false;
        }
    }

    /**
     * Upload Database Async Task
     */
    public class UploadDatabaseFile extends AsyncTask<Void, Void, Void> {
        public final String UPLOAD_PATH = DatabaseHandler.DATABASE_LOCATION;
        File storage_folder = null;
        File download_db_file = null;
        private final String TAG = "Upload Task";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this, "Upload Started!", Toast.LENGTH_SHORT).show();
            if (!CheckExternalStorageAvail()) {
                Toast.makeText(MainActivity.this, "External Storage Not Avail!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            OkHttpClient client = new OkHttpClient();
            try {
                File database = new File(DatabaseHandler.DATABASE_LOCATION);
                RequestBody fb = RequestBody.create(MediaType.parse("db"),database);

                RequestBody rb = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("type","db")
                        .addFormDataPart("uploaded_file",DATABASE_NAME, fb)
                        .build();

                Request res = new Request.Builder()
                        .url(SERVER_URL)
                        .post(rb)
                        .build();
                Response response = client.newCall(res).execute();

                if(!response.isSuccessful()){
                    throw new Exception("Error : "+response);
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.e(TAG, "Reached Post Execute Upload!.");
            Toast toast = Toast.makeText(MainActivity.this, "Upload Complete!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}


