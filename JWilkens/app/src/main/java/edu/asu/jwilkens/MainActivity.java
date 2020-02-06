package edu.asu.jwilkens;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {
    // Graph related values
    private GraphView health_graph;
    private Paint graph_paint;
    private float[] graph_values;
    //private static String[] graph_horlabels = {"2700", "2750", "2800", "2850", "2900",
            //"2950", "3000", "3050", "3100"};
    //private static String[] graph_verlabels = {"500", "1000", "1500", "2000"};
    private static String[] graph_horlabels = {"0", "1", "2", "3", "4", "5", "6", "7"};
    private static String[] graph_verlabels = {"0", "1", "2", "3"};
    private static String graph_title = "Health Graph UI";

    // UI Related Elements
    private RelativeLayout graph_constraint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        graph_constraint = findViewById(R.id.graph_constraint);
        createGraphView();
        graph_constraint.addView(health_graph);
    }

    //Context context, float[] values, String title, String[] horlabels, String[] verlabels, boolean type
    private void createGraphView() {
        /* Data */
        graph_values = new float[5];
        for (int i = 0; i < 5; i++) {
            graph_values[i] = i+550;
        }
        health_graph = new GraphView(this, graph_values, graph_title, graph_horlabels, graph_verlabels, GraphView.LINE);
        health_graph.setLayoutParams(new ConstraintLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
        health_graph.setBackgroundColor(Color.BLACK);
    }
}
