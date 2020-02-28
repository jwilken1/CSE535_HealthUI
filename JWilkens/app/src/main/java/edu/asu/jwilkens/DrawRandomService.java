package edu.asu.jwilkens;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Binder;
import android.os.IBinder;

import java.util.Random;

public class DrawRandomService extends Service implements SensorEventListener {
    private final IBinder binder = new LocalBinder();
    private GraphView health_graph;
    private float[] graph_values;
    private static String[] graph_horlabels = {"0", "50", "100", "150", "200"};
    private static String[] graph_verlabels = {"2000", "1500", "1000", "500", "0"};
    private float[] graph_values_array;
    private Random rand;
    private int current_location;

    /**
     * Update w/ check for interval. If passed, calls UI update.
     * @param event Accelerometer Sensor Event.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            /**
            if ((System.currentTimeMillis() - lastSaved) > ACCE_FILTER_DATA_MIN_TIME) {
                lastSaved = System.currentTimeMillis();
                sensor_x = event.values[0];
                sensor_y = event.values[1] - (float) 9.81;
                sensor_z = event.values[2];
                updateGraphViewRegistered();
            }
             **/
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public class LocalBinder  extends Binder {
        DrawRandomService getService() {
            return DrawRandomService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}