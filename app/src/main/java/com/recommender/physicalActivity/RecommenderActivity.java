package com.recommender.physicalActivity;

import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.PyException;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

public class RecommenderActivity extends AppCompatActivity {

    Python py;
    PyObject module;

    ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommender);

//        InputStream is = getResources().openRawResource(R.raw.steps.1001);
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
        py = Python.getInstance();
        module = py.getModule("get_activity_plan");

        try {
            //Get Current date
            Calendar calendar = Calendar.getInstance();
            String currDay;
            switch (calendar.get(Calendar.DAY_OF_WEEK)){
                case Calendar.SUNDAY:
                    // Current day is Sunday
                    currDay = "sunday";
                    break;
                case Calendar.MONDAY:
                    // Current day is Monday
                    currDay = "monday";
                    break;
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
            int currTime = calendar.get(Calendar.HOUR_OF_DAY);
            System.out.println(currTime);
            byte[] bytes = module.callAttr("get_activity_plan",0,currDay,10500).toJava(byte[].class);
            System.out.println("Python completed");
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            img = findViewById(R.id.imageView);
            img.setImageBitmap(bitmap);

            Float[] act_plan  = module.callAttr("ret_act_plan").toJava(Float[].class);
            final TextView curTar = findViewById(R.id.currTarget);
            curTar.setText(String.valueOf(act_plan[currTime].intValue()));
        }
        catch(Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            System.out.println(e.getMessage());
        }
    }
}