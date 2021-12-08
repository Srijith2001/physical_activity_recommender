package com.recommender.physicalActivity;

import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.PyException;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.models.BarModel;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.ComboLineColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ComboLineColumnChartView;
import lecho.lib.hellocharts.listener.ComboLineColumnChartOnValueSelectListener;

public class RecommenderActivity extends AppCompatActivity implements SensorEventListener {

    Python py;
    PyObject module;

    ImageView img;
    private SensorManager sensorManager;
    private boolean running = false;
    private float totalSteps = 0f;
    private float previousTotalSteps = 0f;

    private TextView tv_stepTaken;
    private TextView tv_target;
    private TextView hr_stepTaken;
    private TextView hr_target;
    private CircularProgressBar hr_progress_circular;
    private CircularProgressBar tv_progress_circular;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommender);

        loadData();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        Intent intent = getIntent();

        // receive the value by getStringExtra() method
        // and key must be same which is send by first activity
        float h = Float.parseFloat(intent.getStringExtra("height"));
        int w = Integer.parseInt(intent.getStringExtra("weight"));
        String g = intent.getStringExtra("gender");
        int goal = Integer.parseInt(intent.getStringExtra("goal"));
        String id = intent.getStringExtra("id");

//        InputStream is = getResources().openRawResource(R.raw.steps.1001);
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
        py = Python.getInstance();
        module = py.getModule("get_activity_plan");
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        try {
            //Get Current date
            Calendar calendar = Calendar.getInstance();
            String currDay;
            switch (calendar.get(Calendar.DAY_OF_WEEK)){
                case Calendar.SUNDAY:
                    // Current day is Sunday
                    currDay = "sunday";
                    break;// Current day is Monday
                case Calendar.TUESDAY:
                    currDay = "tuesday";
                    break;
                case Calendar.WEDNESDAY:
                    currDay = "wednesday";
                    break;
                case Calendar.THURSDAY:
                    currDay = "thursday";
                    break;
                case Calendar.FRIDAY:
                    currDay = "friday";
                    break;
                case Calendar.SATURDAY:
                    currDay = "saturday";
                    break;
                default: currDay = "monday"; break;
            }

            byte[] bytes = module.callAttr("get_activity_plan",0,currDay,goal).toJava(byte[].class);
            System.out.println("Python completed");
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);


            Float[] act_plan  = module.callAttr("ret_act_plan").toJava(Float[].class);


            Timer timer = new Timer ();
            TimerTask hourlyTask = new TimerTask () {
                @Override
                public void run () {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            BarChart barChart = findViewById(R.id.bargraph);
//                            barChart.clearChart();
//                            for (int i=4;i<22;i++) {
//                                BarModel bm = new BarModel(String.valueOf(i), act_plan[i].intValue(),0xFF123456);
//                                barChart.addBar(bm);
//                            }
//                            barChart.startAnimation();


                            ComboLineColumnChartView chartView = findViewById(R.id.chart);
                            chartView.setOnValueTouchListener(new ValueTouchListener());

                            List<Column> columns = new ArrayList<>();
                            for (int i=4;i<22;i++) {
                                List<SubcolumnValue> values = new ArrayList<SubcolumnValue>();
                                values.add(new SubcolumnValue(act_plan[i].intValue(),Color.parseColor("#123456")));
                                columns.add(new Column(values));
                            }
                            ColumnChartData columnChartData = new ColumnChartData(columns);

                            List<Line> lines = new ArrayList<Line>();
                            List<PointValue> values_line = new ArrayList<PointValue>();
                            for (int i = 4; i <22; ++i) {
                                values_line.add(new PointValue(i-4,act_plan[i].intValue()));

                                Line line = new Line(values_line);
                                line.setColor(ChartUtils.COLOR_GREEN);
                                line.setCubic(true);
                                line.setHasPoints(true);
                                lines.add(line);
                            }

                            LineChartData lineChartData = new LineChartData(lines);

                            ComboLineColumnChartData data = new ComboLineColumnChartData(columnChartData,lineChartData);
                            data.setAxisXBottom(null);
                            data.setAxisYLeft(null);

                            Axis axisX = new Axis();
                            Axis axisY = new Axis().setHasLines(true);
                            data.setAxisXBottom(axisX);
                            data.setAxisYLeft(axisY);
                            chartView.setComboLineColumnChartData(data);

                            // Stuff that updates the UI
                            System.out.println(currDay);
                            int currTime = calendar.get(Calendar.HOUR_OF_DAY);

                            hr_target = findViewById(R.id.hr_totalMax);
                            hr_target.setText(String.valueOf(act_plan[currTime].intValue()));

                            tv_target = findViewById(R.id.tv_totalMax);
                            tv_target.setText("10000");

                            System.out.println("done");
                            resetSteps(currTime,currDay,id);
                        }
                    });
                }
            };

            // schedule the task to run starting now and then every hour...
            timer.schedule (hourlyTask, 0l, 1000*30);
        }
        catch(Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            System.out.println(e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        running = true;
        Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepSensor==null){
            Toast.makeText(this,"No sensor detected on this device",Toast.LENGTH_SHORT);
        }
        else{
            sensorManager.registerListener((SensorEventListener) this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onAccuracyChanged(final Sensor sensor, int accuracy){
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
        if(running){
            totalSteps = event.values[0];
            int currentSteps = ((int) totalSteps) - ((int) previousTotalSteps);
            hr_stepTaken = findViewById(R.id.hr_stepsTaken);
            hr_stepTaken.setText(currentSteps);

            hr_progress_circular = findViewById(R.id.hr_progress_circular);
            hr_progress_circular.setProgressWithAnimation(((float) currentSteps));

            tv_stepTaken = findViewById(R.id.tv_stepsTaken);
            tv_stepTaken.setText((int) totalSteps);
            tv_progress_circular = findViewById(R.id.tv_progress_circular);
            tv_progress_circular.setProgressWithAnimation((float) totalSteps);

        }
    }

    public void resetSteps(int currHr, String currDay, String id) {
        previousTotalSteps = totalSteps;
        tv_stepTaken = findViewById(R.id.tv_stepsTaken);
        tv_stepTaken.setText("0");
        saveData(currHr,currDay,id);
    }

    public void saveData(int currHr, String currDay, String id)
    {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> steps = new HashMap<>();
        steps.put(String.valueOf(currHr), (int)previousTotalSteps);
        db.collection("users").document(id).collection("Steps").document(currDay).set(steps);

        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("key1",previousTotalSteps);
        editor.apply();
    }

    private void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        float savedNumber = sharedPreferences.getFloat("key1",0f);
        Log.d("recommender", String.valueOf(savedNumber));
        previousTotalSteps = savedNumber;
    }

    private class ValueTouchListener implements ComboLineColumnChartOnValueSelectListener {

        @Override
        public void onValueDeselected() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onColumnValueSelected(int columnIndex, int subcolumnIndex, SubcolumnValue value) {
            Toast.makeText(RecommenderActivity.this, "Selected column: " + value, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPointValueSelected(int lineIndex, int pointIndex, PointValue value) {
            Toast.makeText(RecommenderActivity.this, "Selected line point: " + value, Toast.LENGTH_SHORT).show();
        }

    }
}