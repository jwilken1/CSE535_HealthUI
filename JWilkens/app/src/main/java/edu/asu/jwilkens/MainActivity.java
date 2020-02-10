package edu.asu.jwilkens;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    // Graph related values
    private GraphView health_graph;
    private float[] graph_values;
    private static String[] graph_horlabels = {"0", "50", "100", "150", "200"};
    private static String[] graph_verlabels = {"2000", "1500", "1000", "500", "0"};
    private static String graph_title = "Health Graph UI";
    private AsyncTask drawRandom;

    DrawRandomService mService;
    boolean mBound = false;

    // UI Related Elements
    private RelativeLayout graph_constraint;
    private Button run_button, stop_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        run_button = (Button) findViewById(R.id.runButton);
        stop_button = (Button) findViewById(R.id.stopButton);
        graph_constraint = (RelativeLayout) findViewById(R.id.graph_constraint);
        createGraphView();
        graph_constraint.addView(health_graph);
        drawRandom = null;

        stop_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //resetGraphView();
                if (drawRandom != null) {
                    drawRandom.cancel(true);
                } else {
                    Toast toast = Toast.makeText(MainActivity.this, "Not Running!", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        run_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //updateGraphView();
                if (drawRandom == null) {
                    drawRandom = new slowTask().execute();
                } else {
                    Toast toast = Toast.makeText(MainActivity.this, "Already Running!", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

    }

    private class slowTask extends AsyncTask<String, Long, Void> {
        @Override
        protected void onPreExecute(){
            mService.setStartTime(0);
        }

        @Override
        protected Void doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                mService.updateGraphView(health_graph);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            // TODO Auto-generated method stub
            try {
                mService.resetGraphView(health_graph);
                drawRandom = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, DrawRandomService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
        mBound = false;
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            DrawRandomService.LocalBinder binder = (DrawRandomService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    //Context context, float[] values, String title, String[] horlabels, String[] verlabels, boolean type
    private void createGraphView() {
        /* Data */
        graph_values = new float[0];
        Random rand = new Random();
        for (int i = 0; i < graph_values.length; i++) {
            graph_values[i] = rand.nextInt(2001);
        }
        health_graph = new GraphView(this, graph_values, graph_title, graph_horlabels, graph_verlabels, GraphView.LINE);
        health_graph.setLayoutParams(new ConstraintLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
        health_graph.setBackgroundColor(Color.BLACK);
    }

    //Context context, float[] values, String title, String[] horlabels, String[] verlabels, boolean type
    private void resetGraphView() {
        /* Data */
        graph_values = new float[0];
        graph_horlabels = new String[] {"0", "50", "100", "150", "200"};
        health_graph.setValues(graph_values, graph_horlabels);
        health_graph.invalidate();
    }

    //Context context, float[] values, String title, String[] horlabels, String[] verlabels, boolean type
    private void updateGraphView() {
        /* Data */
        Random rand = new Random();
        int size = rand.nextInt(201);
        graph_values = new float[size];
        int startTime = rand.nextInt(201);
        graph_horlabels = new String[] {Integer.toString(startTime), Integer.toString(startTime+50),
                Integer.toString(startTime+100), Integer.toString(startTime+150), Integer.toString(startTime+200)};
        for (int i = 0; i < graph_values.length; i++) {
            graph_values[i] = rand.nextInt(2001);
        }
        health_graph.setValues(graph_values, graph_horlabels);
        health_graph.invalidate();
    }
}
