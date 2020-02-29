package edu.asu.jwilkens;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import java.util.Arrays;

public class DrawService extends Service implements SensorEventListener {
    private final IBinder binder = new LocalBinder();
    private GraphView health_graph_x, health_graph_y, health_graph_z;
    private float[] graph_values_y, graph_values_x, graph_values_z;
    private static String[] graph_horlabels = {"0", "50", "100", "150", "200"};
    private float[] graph_values_array_x, graph_values_array_y,graph_values_array_z;
    // private Random rand;
    private int current_location;
    private SensorManager sensorManager;
    private Sensor sensor;
    float sensor_x, sensor_y, sensor_z;
    int arrayloc = 0;



    public class LocalBinder  extends Binder {
        DrawService getService() {
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                sensorManager.registerListener(DrawService.this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Toast toast = Toast.makeText(DrawService.this, "No Accelerometer!", Toast.LENGTH_LONG);
                toast.show();
            }
            graph_values_array_x = new float[3000];
            graph_values_array_y = new float[3000];
            graph_values_array_z = new float[3000];
            /** Commenting out random generator from part one to replace with sensor!
             rand = new Random();
             graph_values_array = new float[3000];
             for (int i = 0; i < graph_values_array.length; i++) {
             graph_values_array[i] = rand.nextInt(2001);
             }**/
            current_location = 0;
            // Return this instance of LocalService so clients can call public methods
            return DrawService.this;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            sensor_x = event.values[0];
            sensor_y = event.values[1];
            sensor_z = event.values[2];
        }
    }

    protected void onResume(){
        this.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause(){
        this.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return binder;
    }

    public void resetGraphView(GraphView health_graph_x, GraphView health_graph_y, GraphView health_graph_z) {
        this.health_graph_x = health_graph_x;
        this.health_graph_y = health_graph_y;
        this.health_graph_z = health_graph_z;
        graph_values_x = new float[0];
        graph_values_y = new float[0];
        graph_values_z = new float[0];
        graph_horlabels = new String[] {"0", "50", "100", "150", "200"};
        health_graph_x.setValues(graph_values_x, graph_horlabels);
        health_graph_x.invalidate();
        health_graph_y.setValues(graph_values_y, graph_horlabels);
        health_graph_y.invalidate();
        health_graph_z.setValues(graph_values_z, graph_horlabels);
        health_graph_z.invalidate();
        Thread.interrupted();
    }

    /**
     public void resetGraphView(GraphView health_graph) {
     this.health_graph = health_graph;
     graph_values = new float[0];
     graph_horlabels = new String[] {"0", "50", "100", "150", "200"};
     health_graph.setValues(graph_values, graph_horlabels);
     health_graph.invalidate();
     Thread.interrupted();
     }**/

    public void setStartTime(int time) {
        current_location = time;
    }

    /**
     //Context context, float[] values, String title, String[] horlabels, String[] verlabels, boolean type
     public void updateGraphView(GraphView health_graph) {
     this.health_graph = health_graph;
     final GraphView healthGraph = this.health_graph;
     int size = 0;
     int startTime = current_location;
     graph_horlabels = new String[]{Integer.toString(startTime), Integer.toString(startTime + 2),
     Integer.toString(startTime + 4), Integer.toString(startTime + 6), Integer.toString(startTime + 8), Integer.toString(startTime + 10)};
     graph_values = new float[200];
     while (startTime + size < graph_values_array.length - 1 && !Thread.currentThread().isInterrupted()) {
     for (int i = 0; i < graph_values.length; i++) {
     graph_values[i] = graph_values_array[i + startTime];
     }
     healthGraph.setValues(graph_values, graph_horlabels);
     healthGraph.invalidate();
     size++;
     current_location++;
     try {
     Thread.sleep(1000);                 //1000 milliseconds is one second.
     graph_values_array[size] = sensor_x;
     graph_values_array[size + 1] = sensor_y;
     graph_values_array[size + 2] = sensor_z;
     size += 3;
     } catch (InterruptedException ex) {
     Thread.currentThread().interrupt();
     }
     }
     } **/

    public void updateGraphView(GraphView health_graph_x, GraphView health_graph_y, GraphView health_graph_z) {
        /* Data */
        this.health_graph_x = health_graph_x;
        this.health_graph_y = health_graph_y;
        this.health_graph_z = health_graph_z;
        int size = 0;
        int startTime = current_location;
        graph_horlabels = new String[]{Integer.toString(startTime), Integer.toString(startTime + 2),
                Integer.toString(startTime + 4), Integer.toString(startTime + 6), Integer.toString(startTime + 8), Integer.toString(startTime + 10)};
        while (startTime + size < graph_values_array_x.length - 1 && !Thread.currentThread().isInterrupted()) {
            graph_values_x = new float[size];
            graph_values_y = new float[size];
            graph_values_z = new float[size];
            for (int i = 0; i < graph_values_x.length; i++) {
                graph_values_x[i] = graph_values_array_x[i + startTime];
                graph_values_y[i] = graph_values_array_y[i + startTime];
                graph_values_z[i] = graph_values_array_z[i + startTime];
            }
            health_graph_x.setValues(graph_values_x, graph_horlabels);
            health_graph_x.invalidate();
            health_graph_y.setValues(graph_values_y, graph_horlabels);
            health_graph_y.invalidate();
            health_graph_z.setValues(graph_values_z, graph_horlabels);
            health_graph_z.invalidate();
            size++;
            current_location++;
            try {
                Thread.sleep(1000);                 //1000 milliseconds is one second.
                graph_values_array_x[size] = sensor_x;
                graph_values_array_y[size] = sensor_y;
                graph_values_array_z[size] = sensor_z;
                size ++;
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

        }
    }
}