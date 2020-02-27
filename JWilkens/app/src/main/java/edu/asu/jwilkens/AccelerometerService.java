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

import java.util.Random;

public class AccelerometerService extends Service implements SensorEventListener {
    private final IBinder binder = new LocalBinder();
    private GraphView health_graph;
    private float[] graph_values;
    private static String[] graph_horlabels = {"0", "50", "100", "150", "200"};
    private static String[] graph_verlabels = {"2000", "1500", "1000", "500", "0"};
    private float[] graph_values_array;
    // private Random rand;
    private int current_location;
    private SensorManager sensorManager;
    private Sensor sensor;
    public float sensor_x, sensor_y, sensor_z;


    public class LocalBinder  extends Binder {
        AccelerometerService getService() {
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            /** Commenting out random generator from part one to replace with sensor!
            rand = new Random();
            graph_values_array = new float[3000];
            for (int i = 0; i < graph_values_array.length; i++) {
                graph_values_array[i] = rand.nextInt(2001);
            }**/
            current_location = 0;
            // Return this instance of LocalService so clients can call public methods
            return AccelerometerService.this;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            sensor_x = sensorEvent.values[0];
            sensor_y = sensorEvent.values[1];
            sensor_z = sensorEvent.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return binder;
    }

    public void resetGraphView(GraphView health_graph) {
        /* Data */
        this.health_graph = health_graph;
        graph_values = new float[0];
        graph_horlabels = new String[] {"0", "50", "100", "150", "200"};
        health_graph.setValues(graph_values, graph_horlabels);
        health_graph.invalidate();
    }

    public void setStartTime(int time) {
        current_location = time;
    }

    //Context context, float[] values, String title, String[] horlabels, String[] verlabels, boolean type
    public void updateGraphView(GraphView health_graph) {
        /* Data */
        //int startTime = rand.nextInt(201);
        this.health_graph = health_graph;
        int startTime = current_location;
        graph_horlabels = new String[] {Integer.toString(startTime), Integer.toString(startTime+2),
                Integer.toString(startTime+4), Integer.toString(startTime+6), Integer.toString(startTime+8), Integer.toString(startTime+10)};
        int size = 1;
        while (startTime+size < graph_values_array.length-1) {
            graph_values = new float[size];
            for (int i = 0; i < graph_values.length; i++) {
                graph_values[i] = graph_values_array[i+startTime];
            }
            health_graph.setValues(graph_values, graph_horlabels);
            health_graph.invalidate();
            size++;
            current_location++;
            try {
                Thread.sleep(500);                 //1000 milliseconds is one second.
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
