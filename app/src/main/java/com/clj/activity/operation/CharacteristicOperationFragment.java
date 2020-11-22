package com.clj.activity.operation;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import java.lang.Math;

import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import com.clj.activity.R;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleIndicateCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleReadCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.database.DatabaseReference;

import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.quickstart.database.databinding.ActivityNewPostBinding;
//import com.google.firebase.quickstart.database.java.models.Post;
//import com.google.firebase.quickstart.database.java.models.User;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static com.clj.activity.MainActivity.NOTIFICATION_SERVICE;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class CharacteristicOperationFragment extends Fragment {

    public static final int PROPERTY_READ = 1;
    public static final int PROPERTY_WRITE = 2;
    public static final int PROPERTY_WRITE_NO_RESPONSE = 3;
    public static final int PROPERTY_NOTIFY = 4;
    public static final int PROPERTY_INDICATE = 5;

    private LinearLayout layout_container;
    private List<String> childList = new ArrayList<>();
    private final int itemcount = 1;
    public int count = 1;
    public float activity = 0;
    public int offset;
    public float steps = 1;
    private final String CHANNEL_ID = "Temperature Notification";
    private final int NOTIFICATION_ID = 001;
    public int flag =0;
    public float absoluteValue;
    //private Context this_context = this;

    public FirebaseDatabase database = FirebaseDatabase.getInstance();
    public DatabaseReference myRef = database.getReference();


    public String selected_location;
    public String selected_activity;
    public String user = "Parker";
    public int window = 3;
    public int windowCounter = 0;
    public float filter[] = new float[window+1];
    public float average = 0;
    public String ascii;
    public String[] values = new String[3];
    public String converter;
    //public float absoluteValue;
    public float threshold = 11.75f;
    public float floatValue, floatValue2, floatValue3;
    public String label1 = "Steps";
    public String finalString;
    public StringBuffer buff = new StringBuffer();
    public float sum;
    public String label2 = "Hypotenuse";
    public String label3 = "Threshold";
    public Double pythagoreanTheorem;
    public String ts;
    public SimpleDateFormat s;
    public DatabaseReference dataRef = myRef.child(user);


    // Create a storage reference from our app

// ...
    //mDatabase = FirebaseDatabase.getInstance().getReference();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_characteric_operation, null);
        initView(v);

        return v;
    }


    public void displayNotification(Context context)
    {
        String long_notif_text = "Be advised that your temperature is high.  Please test for COVID-19";
        createNotificationChannel(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(long_notif_text));

        
        builder.setContentTitle("TRIDENT Temperature Notification");
        builder.setContentText(long_notif_text);
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setSmallIcon(R.drawable.ic_temperature_notification);


        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel(Context context)
    {

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            CharSequence name = "TRIDENT Notification";
            String description = "Be advised that your temperature is high.  Please test for COVID-19";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,name,importance);
            notificationChannel.setDescription(description);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }


    }



    private void initView(View v) {
        layout_container = (LinearLayout) v.findViewById(R.id.layout_container);

    }

    private void initGraph3(LineChart line_chart1, String Plot_title){

        line_chart1.getDescription().setEnabled(true);
        line_chart1.getDescription().setText(Plot_title);

        line_chart1.setTouchEnabled(false);
        line_chart1.setDragEnabled(true);
        line_chart1.setScaleEnabled(true);
        line_chart1.setDrawGridBackground(false);
        line_chart1.setBackgroundColor(Color.WHITE);

        //LineData sensor_data = new LineData();
        //sensor_data.setValueTextColor(Color.WHITE);


        //String[] xAxis = new String[] {"0"};

        //line_chart.setData(new LineData(xAxis, lines));
        String label = "Majority Voting Mean Minus Ambient Temperature";

        LineData m1lineData=generateLineData8(label);


        //mlineData.addDataSet(generateLineData4().getDataSetByIndex(0));
        //mlineData.addDataSet(generateLineData5().getDataSetByIndex(0));

        line_chart1.setData(m1lineData);



        // get the legend (only possible after setting data)
        Legend l = line_chart1.getLegend();


        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.BLACK);
        XAxis.XAxisPosition position =  XAxis.XAxisPosition.BOTTOM;


        XAxis xl = line_chart1.getXAxis();
        xl.setTextColor(Color.BLACK);
        xl.setPosition(position);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);
        //xl.setDrawLabels(true);

        YAxis rightAxis = line_chart1.getAxisRight();
        rightAxis.setTextColor(Color.BLACK);
        rightAxis.setDrawGridLines(false);
//        rightAxis.setAxisMaximum(5f);
//        rightAxis.setAxisMinimum(0f);
        rightAxis.setDrawGridLines(true);
        rightAxis.setDrawLabels(true);

    }

    private void initGraph2(LineChart line_chart1, String Plot_title){

        line_chart1.getDescription().setEnabled(true);
        line_chart1.getDescription().setText(Plot_title);

        line_chart1.setTouchEnabled(false);
        line_chart1.setDragEnabled(true);
        line_chart1.setScaleEnabled(true);
        line_chart1.setDrawGridBackground(false);
        line_chart1.setBackgroundColor(Color.WHITE);

        //LineData sensor_data = new LineData();
        //sensor_data.setValueTextColor(Color.WHITE);


        //String[] xAxis = new String[] {"0"};

        //line_chart.setData(new LineData(xAxis, lines));
        String label = "x-axis";
        String label1 = "y-axis";
        String label2 = "z-axis";

        LineData m1lineData=generateLineData5(label);
        m1lineData.addDataSet(generateLineData6(label1).getDataSetByIndex(0));
        m1lineData.addDataSet(generateLineData7(label2).getDataSetByIndex(0));

        //mlineData.addDataSet(generateLineData4().getDataSetByIndex(0));
        //mlineData.addDataSet(generateLineData5().getDataSetByIndex(0));

        line_chart1.setData(m1lineData);



        // get the legend (only possible after setting data)
        Legend l = line_chart1.getLegend();


        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.BLACK);
        XAxis.XAxisPosition position =  XAxis.XAxisPosition.BOTTOM;


        XAxis xl = line_chart1.getXAxis();
        xl.setTextColor(Color.BLACK);
        xl.setPosition(position);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);
        //xl.setDrawLabels(true);

        YAxis rightAxis = line_chart1.getAxisRight();
        rightAxis.setTextColor(Color.BLACK);
        rightAxis.setDrawGridLines(false);
//        rightAxis.setAxisMaximum(5f);
//        rightAxis.setAxisMinimum(0f);
        rightAxis.setDrawGridLines(true);
        rightAxis.setDrawLabels(true);

    }

    private void initGraph(LineChart line_chart, String Plot_title){

        line_chart.getDescription().setEnabled(true);
        line_chart.getDescription().setText(Plot_title);

        line_chart.setTouchEnabled(false);
        line_chart.setDragEnabled(true);
        line_chart.setScaleEnabled(true);
        line_chart.setDrawGridBackground(false);
        line_chart.setBackgroundColor(Color.WHITE);

        LineData sensor_data = new LineData();
        sensor_data.setValueTextColor(Color.WHITE);


        //String[] xAxis = new String[] {"0"};

        //line_chart.setData(new LineData(xAxis, lines));
        String label1 = "Steps";
        String label2 = "Hypotenuse";
        String label3 = "Threshold";
        //String label4 = "SkinTemp 4";
        //String label5 = "AmbientTemp";

        LineData mlineData=generateLineData(label1);
        mlineData.addDataSet(generateLineData1(label2).getDataSetByIndex(0));
        mlineData.addDataSet(generateLineData2(label3).getDataSetByIndex(0));
        //mlineData.addDataSet(generateLineData3(label4).getDataSetByIndex(0));
        //mlineData.addDataSet(generateLineData4(label5).getDataSetByIndex(0));
        //mlineData.addDataSet(generateLineData4().getDataSetByIndex(0));
        //mlineData.addDataSet(generateLineData5().getDataSetByIndex(0));

        line_chart.setData(mlineData);



        // get the legend (only possible after setting data)
        Legend l = line_chart.getLegend();


        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.BLACK);
        XAxis.XAxisPosition position =  XAxis.XAxisPosition.BOTTOM;


        XAxis xl = line_chart.getXAxis();
        xl.setTextColor(Color.BLACK);
        xl.setPosition(position);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);
        //xl.setDrawLabels(true);

        YAxis rightAxis = line_chart.getAxisRight();
        rightAxis.setTextColor(Color.BLACK);
        rightAxis.setDrawGridLines(false);
//        rightAxis.setAxisMaximum(5f);
//        rightAxis.setAxisMinimum(0f);
        rightAxis.setDrawGridLines(true);
        rightAxis.setDrawLabels(true);

    }
    private LineData generateLineData(String label) {

        LineData d = new LineData();

        ArrayList<Entry> entries = new ArrayList<Entry>();

        for (int index = 0; index < itemcount; index++)
            entries.add(new Entry(0, Float.NaN));

        LineDataSet set = new LineDataSet(entries, label);
        set.setColor(Color.rgb(255, 102, 0)); //orange
        set.setLineWidth(2f);
        set.setDrawCircles(false);
        set.setCircleColor(Color.rgb(255, 102, 0)); //orange

        // set.setFillColor(Color.rgb(240, 238, 70));
        //set.setDrawCubic(true);
        set.setDrawValues(false);
        //set.setValueTextSize(10f);
        //set.setValueTextColor(Color.rgb(240, 238, 70));
        set.setHighLightColor(Color.MAGENTA);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        d.addDataSet(set);

        return d;
    }

    private LineData generateLineData1(String label) {

        LineData d = new LineData();

        ArrayList<Entry> entries = new ArrayList<Entry>();

        for (int index = 0; index < itemcount; index++)
            entries.add(new Entry(0, Float.NaN));


        LineDataSet set = new LineDataSet(entries, label);
        set.setColor(Color.rgb(255, 153, 0));  //Light Orange
        set.setLineWidth(2f);

        set.setDrawCircles(false);
        set.setCircleColor(Color.rgb(255, 153, 0));  //Light Orange

        // set.setFillColor(Color.rgb(240, 238, 70));
        //set.setDrawCubic(true);
        set.setDrawValues(false);
        //set.setValueTextSize(10f);
        //set.setValueTextColor(Color.rgb(240, 238, 70));
        set.setHighLightColor(Color.MAGENTA);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        d.addDataSet(set);

        return d;
    }

    private LineData generateLineData2(String label) {

        LineData d = new LineData();

        ArrayList<Entry> entries = new ArrayList<Entry>();

        for (int index = 0; index < itemcount; index++)
            entries.add(new Entry(0, Float.NaN));


        LineDataSet set = new LineDataSet(entries, label);
        set.setColor(Color.rgb(51, 204, 255)); //very light blue
        set.setLineWidth(2f);
        set.setDrawCircles(false);
        set.setCircleColor(Color.rgb(51, 204, 255)); //very light blue
        //set.setDrawCircles(false);

        // set.setFillColor(Color.rgb(240, 238, 70));
        //set.setDrawCubic(true);
        set.setDrawValues(false);
        //set.setValueTextSize(10f);
        //set.setValueTextColor(Color.rgb(240, 238, 70));
        set.setHighLightColor(Color.MAGENTA);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        d.addDataSet(set);

        return d;
    }

    private LineData generateLineData3(String label) {

        LineData d = new LineData();

        ArrayList<Entry> entries = new ArrayList<Entry>();

        for (int index = 0; index < itemcount; index++)
            entries.add(new Entry(0, Float.NaN));


        LineDataSet set = new LineDataSet(entries, label);

        set.setLineWidth(2f);
        set.setDrawCircles(false);
        set.setCircleColor(Color.rgb(204, 204, 204)); //light grey
        set.setColor(Color.rgb(79,121,66)); // dark green
        // set.setFillColor(Color.rgb(0, 0, 153));
        //set.setDrawCubic(true);
        set.setDrawValues(false);
        //set.setValueTextSize(10f);
        //set.setValueTextColor(Color.rgb(240, 238, 70));
        set.setHighLightColor(Color.MAGENTA);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        d.addDataSet(set);

        return d;
    }

    private LineData generateLineData4(String label) {

        LineData d = new LineData();

        ArrayList<Entry> entries = new ArrayList<Entry>();

        for (int index = 0; index < itemcount; index++)
            entries.add(new Entry(0, Float.NaN));


        LineDataSet set = new LineDataSet(entries, label);
        set.setColor(Color.rgb(204, 204, 204)); //light grey

        set.setLineWidth(2f);
        //set.setCircleSize(.1f);
        set.setCircleColor(Color.rgb(255,215,0));

        set.setDrawCircles(false);
        set.setDrawValues(false);

        set.setHighLightColor(Color.MAGENTA);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        d.addDataSet(set);

        return d;
    }
    private LineData generateLineData5(String label) {

        LineData d = new LineData();

        ArrayList<Entry> entries = new ArrayList<Entry>();

        for (int index = 0; index < itemcount; index++)
            entries.add(new Entry(0, 0f));

        LineDataSet set = new LineDataSet(entries, label);
        set.setColor(Color.rgb(255, 102, 0)); //orange
        set.setLineWidth(2f);
        set.setDrawCircles(false);
        set.setCircleColor(Color.rgb(255, 102, 0)); //orange

        // set.setFillColor(Color.rgb(240, 238, 70));
        //set.setDrawCubic(true);
        set.setDrawValues(false);
        //set.setValueTextSize(10f);
        //set.setValueTextColor(Color.rgb(240, 238, 70));
        set.setHighLightColor(Color.MAGENTA);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        d.addDataSet(set);

        return d;
    }
    private LineData generateLineData6(String label) {

        LineData d = new LineData();

        ArrayList<Entry> entries = new ArrayList<Entry>();

        for (int index = 0; index < itemcount; index++)
            entries.add(new Entry(0, 0f));

        LineDataSet set = new LineDataSet(entries, label);
        set.setColor(Color.rgb(255, 153, 0));  //Light Orange
        set.setLineWidth(2f);

        set.setDrawCircles(false);
        set.setCircleColor(Color.rgb(255, 153, 0));  //Light Orange


        // set.setFillColor(Color.rgb(240, 238, 70));
        //set.setDrawCubic(true);
        set.setDrawValues(false);
        //set.setValueTextSize(10f);
        //set.setValueTextColor(Color.rgb(240, 238, 70));
        set.setHighLightColor(Color.MAGENTA);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        d.addDataSet(set);

        return d;
    }
    private LineData generateLineData7(String label) {

        LineData d = new LineData();

        ArrayList<Entry> entries = new ArrayList<Entry>();

        for (int index = 0; index < itemcount; index++)
            entries.add(new Entry(0, 0f));

        LineDataSet set = new LineDataSet(entries, label);
        set.setColor(Color.rgb(51, 204, 255)); //very light blue
        set.setLineWidth(2f);
        set.setDrawCircles(false);
        set.setCircleColor(Color.rgb(51, 204, 255)); //very light blue

        // set.setFillColor(Color.rgb(240, 238, 70));
        //set.setDrawCubic(true);
        set.setDrawValues(false);
        //set.setValueTextSize(10f);
        //set.setValueTextColor(Color.rgb(240, 238, 70));
        set.setHighLightColor(Color.MAGENTA);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        d.addDataSet(set);

        return d;
    }
    private LineData generateLineData8(String label) {

        LineData d = new LineData();

        ArrayList<Entry> entries = new ArrayList<Entry>();

        for (int index = 0; index < itemcount; index++)
            entries.add(new Entry(0, Float.NaN));

        LineDataSet set = new LineDataSet(entries, label);
        set.setColor(Color.rgb(255, 102, 0)); //orange
        set.setLineWidth(2f);
        set.setDrawCircles(false);
        set.setCircleColor(Color.rgb(255, 102, 0)); //orange

        // set.setFillColor(Color.rgb(240, 238, 70));
        //set.setDrawCubic(true);
        set.setDrawValues(false);
        //set.setValueTextSize(10f);
        //set.setValueTextColor(Color.rgb(240, 238, 70));
        set.setHighLightColor(Color.MAGENTA);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        d.addDataSet(set);

        return d;
    }
    private void updateGraph(LineChart line_chart, Float new_data, Float new_data2, Float new_data3, String data_label){



        LineData data = line_chart.getData();
        //LineData data2 = line_chart.getData();

        //System.out.print("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        //System.out.print(data.getDataSetByIndex(0));
        //System.out.print("*******************************************");
        //System.out.print(data.getDataSetByIndex(1));

        if(data != null){
            ILineDataSet set = data.getDataSetByIndex(0);
            ILineDataSet set2 = data.getDataSetByIndex(1);
            ILineDataSet set3 = data.getDataSetByIndex(2);
            ILineDataSet set4 = data.getDataSetByIndex(3);
            ILineDataSet set5 = data.getDataSetByIndex(4);
            //ILineDataSet set2 = data2.getDataSetByIndex(0);




            if(set == null){

                set = createSet(data_label);
                data.addDataSet(set);
                //set.addEntry(new_data2,data_label_2);

                set2 = createSet2("Data set 2");
                data.addDataSet(set2);

                set3 = createSet3("Data set 3");
                data.addDataSet(set3);

                set4 = createSet4("Data set 4");
                data.addDataSet(set4);

                set5 = createSet5("Data set 5");
                data.addDataSet(set5);

                //set2 = createSet(data_label_2);
                //data.addDataSet(set2);
                //ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
                //dataSets.add(data);
                //dataSets.add(set2);

            }

            if (count <=1 ) {
                Entry e = set.getEntryForXValue(set.getEntryCount() - 1, Float.NaN);
                data.removeEntry(e, 0);
                data.notifyDataChanged();
                line_chart.notifyDataSetChanged();
                line_chart.invalidate();;
                Entry e1 = set2.getEntryForXValue(set2.getEntryCount() - 1, Float.NaN);
                data.removeEntry(e1, 1);
                data.notifyDataChanged();
                line_chart.notifyDataSetChanged();
                line_chart.invalidate();;
                Entry e2 = set3.getEntryForXValue(set3.getEntryCount() - 1, Float.NaN);
                data.removeEntry(e2, 2);
                data.notifyDataChanged();
                line_chart.notifyDataSetChanged();
                line_chart.invalidate();;
//                Entry e3 = set4.getEntryForXValue(set4.getEntryCount() - 1, Float.NaN);
//                data.removeEntry(e3, 3);
//                Entry e4 = set5.getEntryForXValue(set5.getEntryCount() - 1, Float.NaN);
//                data.removeEntry(e4, 4);
                // or remove by index
                // mData.removeEntryByXValue(xIndex, dataSetIndex);
                data.notifyDataChanged();
                line_chart.notifyDataSetChanged();
                line_chart.invalidate();;
            }
            count = count + 1;
            //ArrayList<LineDataSet> lines = new ArrayList<LineDataSet>(); // for adding multiple plots
            //lines.add(set);

            data.addEntry( new Entry(set.getEntryCount(), new_data), 0);
            data.addEntry( new Entry(set2.getEntryCount(), new_data2), 1);
            data.addEntry( new Entry(set3.getEntryCount(), new_data3), 2);
            //data.addEntry( new Entry(set4.getEntryCount(), new_data4), 3);
            //data.addEntry( new Entry(set5.getEntryCount(), new_data5), 4);
            //data.addEntry( new Entry(set2.getEntryCount(), new_data2), 0);
            //String[] xAxis = new String[] {data_label};
            //line_chart.setData(new LineData(data_label, data));

            /*if (count >=151 ) {
                Entry e = set.getEntryForXValue(0, Float.NaN);
                data.removeEntry(e, 0);
                //data.notifyDataChanged();
                //line_chart.notifyDataSetChanged();
                //line_chart.invalidate();;
                Entry e1 = set2.getEntryForXValue(0, Float.NaN);
                data.removeEntry(e1, 1);
                //data.notifyDataChanged();
                //line_chart.notifyDataSetChanged();
                //line_chart.invalidate();;
                Entry e2 = set3.getEntryForXValue(0, Float.NaN);
                data.removeEntry(e2, 2);
                //data.notifyDataChanged();
                //line_chart.notifyDataSetChanged();
                //line_chart.invalidate();;
//                Entry e3 = set4.getEntryForXValue(set4.getEntryCount() - 1, Float.NaN);
//                data.removeEntry(e3, 3);
//                Entry e4 = set5.getEntryForXValue(set5.getEntryCount() - 1, Float.NaN);
//                data.removeEntry(e4, 4);
                // or remove by index
                // mData.removeEntryByXValue(xIndex, dataSetIndex);

            }*/

            data.notifyDataChanged();

            line_chart.notifyDataSetChanged();
            line_chart.setVisibleXRangeMaximum(150);
            line_chart.setDragEnabled(true);
            line_chart.setFocusable(true);
            line_chart.moveViewToX(data.getEntryCount());


        }

    }

    private void updateGraph2(LineChart line_chart1, float new_data, float new_data2, float new_data3, String data_label){



        LineData data2 = line_chart1.getData();
        //LineData data2 = line_chart.getData();

        //System.out.print("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        //System.out.print(data.getDataSetByIndex(0));
        //System.out.print("*******************************************");
        //System.out.print(data.getDataSetByIndex(1));

        if(data2 != null){
            ILineDataSet set6 = data2.getDataSetByIndex(0);
            ILineDataSet set7 = data2.getDataSetByIndex(1);
            ILineDataSet set8 = data2.getDataSetByIndex(2);

            //ILineDataSet set2 = data2.getDataSetByIndex(0);




            if(set6 == null){

                set6 = createSet6(data_label);
                data2.addDataSet(set6);
                //set.addEntry(new_data2,data_label_2);

                set7 = createSet7("Data2");
                data2.addDataSet(set7);

                set8 = createSet8("Data3");
                data2.addDataSet(set8);



                //set2 = createSet(data_label_2);
                //data.addDataSet(set2);
                //ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
                //dataSets.add(data);
                //dataSets.add(set2);

            }

            if (count <=1 ) {
                Entry e = set6.getEntryForXValue(set6.getEntryCount() - 1, Float.NaN);
                data2.removeEntry(e, 0);
                data2.notifyDataChanged();
                line_chart1.notifyDataSetChanged();
                line_chart1.invalidate();;
                Entry e1 = set7.getEntryForXValue(set7.getEntryCount() - 1, Float.NaN);
                data2.removeEntry(e1, 1);
                data2.notifyDataChanged();
                line_chart1.notifyDataSetChanged();
                line_chart1.invalidate();;
                Entry e2 = set8.getEntryForXValue(set8.getEntryCount() - 1, Float.NaN);
                data2.removeEntry(e2, 2);
                data2.notifyDataChanged();
                line_chart1.notifyDataSetChanged();
                line_chart1.invalidate();;

                // or remove by index
                // mData.removeEntryByXValue(xIndex, dataSetIndex);
                data2.notifyDataChanged();
                line_chart1.notifyDataSetChanged();
                line_chart1.invalidate();;
            }
            //count = count + 1;
            //ArrayList<LineDataSet> lines = new ArrayList<LineDataSet>(); // for adding multiple plots
            //lines.add(set);

            data2.addEntry( new Entry(set6.getEntryCount(), new_data), 0);
            data2.addEntry( new Entry(set7.getEntryCount(), new_data2), 1);
            data2.addEntry( new Entry(set8.getEntryCount(), new_data3), 2);

            //data.addEntry( new Entry(set2.getEntryCount(), new_data2), 0);
            //String[] xAxis = new String[] {data_label};
            //line_chart.setData(new LineData(data_label, data));
            /*if (count > 151) {
               // if (count % 2 == 0) {
                    Entry e = set6.getEntryForXValue(0, Float.NaN);
                    data2.removeEntry(e, 0);
                    //data2.notifyDataChanged();
                    //line_chart1.notifyDataSetChanged();
                    //line_chart1.invalidate();
                    ;
                    Entry e1 = set7.getEntryForXValue(0, Float.NaN);
                    data2.removeEntry(e1, 1);
                    //data2.notifyDataChanged();
                    //line_chart1.notifyDataSetChanged();
                    //line_chart1.invalidate();
                    ;
                    Entry e2 = set8.getEntryForXValue(0, Float.NaN);
                    data2.removeEntry(e2, 2);
                    //data2.notifyDataChanged();
                    //line_chart1.notifyDataSetChanged();
                    //line_chart1.invalidate();
                    ;

                    // or remove by index
                    // mData.removeEntryByXValue(xIndex, dataSetIndex);
                    //data2.notifyDataChanged();
                    //line_chart1.notifyDataSetChanged();
                    //line_chart1.invalidate();;
                //}
            }*/

            data2.notifyDataChanged();

            line_chart1.notifyDataSetChanged();
            line_chart1.setVisibleXRangeMaximum(150);
            //line_chart1.setVisibleXRange(0,150);
            line_chart1.setDragEnabled(true);
            line_chart1.moveViewToX(data2.getEntryCount());
            line_chart1.setFocusable(true);


        }

    }

    private void updateGraph3(LineChart line_chart2, float new_data, String data_label){



        LineData data3 = line_chart2.getData();
        //LineData data2 = line_chart.getData();

        //System.out.print("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        //System.out.print(data.getDataSetByIndex(0));
        //System.out.print("*******************************************");
        //System.out.print(data.getDataSetByIndex(1));

        if(data3 != null){
            ILineDataSet set9 = data3.getDataSetByIndex(0);


            //ILineDataSet set2 = data2.getDataSetByIndex(0);




            if(set9 == null){

                set9 = createSet9(data_label);
                data3.addDataSet(set9);
                //set.addEntry(new_data2,data_label_2);





                //set2 = createSet(data_label_2);
                //data.addDataSet(set2);
                //ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
                //dataSets.add(data);
                //dataSets.add(set2);

            }

            if (count <=2 ) {
                Entry e = set9.getEntryForXValue(set9.getEntryCount() - 2, Float.NaN);
                data3.removeEntry(e, 0);
                data3.notifyDataChanged();
                line_chart2.notifyDataSetChanged();
                line_chart2.invalidate();;


                // or remove by index
                // mData.removeEntryByXValue(xIndex, dataSetIndex);
                data3.notifyDataChanged();
                line_chart2.notifyDataSetChanged();
                line_chart2.invalidate();;
            }
            count = count + 1;
            //ArrayList<LineDataSet> lines = new ArrayList<LineDataSet>(); // for adding multiple plots
            //lines.add(set);

            data3.addEntry( new Entry(set9.getEntryCount(), new_data), 0);


            //data.addEntry( new Entry(set2.getEntryCount(), new_data2), 0);
            //String[] xAxis = new String[] {data_label};
            //line_chart.setData(new LineData(data_label, data));

            data3.notifyDataChanged();

            line_chart2.notifyDataSetChanged();
            line_chart2.setVisibleXRangeMaximum(150);
            line_chart2.setDragEnabled(true);
            line_chart2.moveViewToX(data3.getEntryCount());


        }

    }

    private LineDataSet createSet(String legend){
        LineDataSet set = new LineDataSet(null, legend);

        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(2f);
        set.setColor(Color.rgb(255, 102, 0)); //orange
        set.setCircleColors(Color.rgb(255, 102, 0)); //orange
        //set.setCircleRadius(.1f);
        set.setDrawCircles(false);

        set.setMode(LineDataSet.Mode.LINEAR);
        //set.setCubicIntensity(2f);
        set.setDrawValues(false);

        return (set);

   }
   private LineDataSet createSet2(String legend) {

       LineDataSet set2 = new LineDataSet(null, legend);
       set2.setAxisDependency(YAxis.AxisDependency.LEFT);
       set2.setLineWidth(2f);
       set2.setColor(Color.rgb(255, 153, 0));  //Light Orange
       set2.setCircleColors(Color.rgb(255, 153, 0));  //Light Orange
       //set2.setCircleRadius(.1f);
       set2.setDrawCircles(false);

       set2.setMode(LineDataSet.Mode.LINEAR);
       //set.setCubicIntensity(2f);
       set2.setDrawValues(false);
       return(set2);
   }

    private LineDataSet createSet3(String legend) {

        LineDataSet set2 = new LineDataSet(null, legend);
        set2.setAxisDependency(YAxis.AxisDependency.LEFT);
        set2.setLineWidth(2f);
        set2.setColor(Color.rgb(51, 204, 255));  //very light blue
        set2.setCircleColors(Color.rgb(51, 204, 255));  //very light blue
        //set2.setCircleRadius(.1f);
        set2.setDrawCircles(false);

        set2.setMode(LineDataSet.Mode.LINEAR);
        //set.setCubicIntensity(2f);
        set2.setDrawValues(false);
        return(set2);
    }

    private LineDataSet createSet4(String legend) {

        LineDataSet set2 = new LineDataSet(null, legend);
        set2.setAxisDependency(YAxis.AxisDependency.LEFT);
        set2.setLineWidth(2f);
        set2.setColor(Color.rgb(79,121,66));

        set2.setCircleColors(Color.rgb(204, 204, 204)); //light grey
        //set2.setCircleRadius(.1f);
        set2.setDrawCircles(false);

        set2.setMode(LineDataSet.Mode.LINEAR);
        //set.setCubicIntensity(2f);
        set2.setDrawValues(false);
        return(set2);
    }

    private LineDataSet createSet5(String legend) {

        LineDataSet set2 = new LineDataSet(null, legend);
        set2.setAxisDependency(YAxis.AxisDependency.LEFT);
        set2.setLineWidth(2f);
        set2.setColor(Color.rgb(204, 204, 204)); //light grey
        set2.setCircleColors(Color.rgb(255,215,0));
        //set2.setCircleRadius(.1f);
        set2.setDrawCircles(false);

        set2.setMode(LineDataSet.Mode.LINEAR);
        //set.setCubicIntensity(2f);
        set2.setDrawValues(false);
        return(set2);
    }

    private LineDataSet createSet6(String legend){
        LineDataSet set = new LineDataSet(null, legend);

        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(2f);
        set.setColor(Color.rgb(255, 102, 0)); //orange
        set.setCircleColors(Color.rgb(255, 102, 0)); //orange
        //set.setCircleRadius(.1f);
        set.setDrawCircles(false);

        set.setMode(LineDataSet.Mode.LINEAR);
        //set.setCubicIntensity(2f);
        set.setDrawValues(false);

        return (set);

    }
    private LineDataSet createSet7(String legend) {

        LineDataSet set2 = new LineDataSet(null, legend);
        set2.setAxisDependency(YAxis.AxisDependency.LEFT);
        set2.setLineWidth(2f);
        set2.setColor(Color.rgb(255, 153, 0));  //Light Orange
        set2.setCircleColors(Color.rgb(255, 153, 0));  //Light Orange
        //set2.setCircleRadius(.1f);
        set2.setDrawCircles(false);

        set2.setMode(LineDataSet.Mode.LINEAR);
        //set.setCubicIntensity(2f);
        set2.setDrawValues(false);
        return(set2);
    }

    private LineDataSet createSet8(String legend) {

        LineDataSet set2 = new LineDataSet(null, legend);
        set2.setAxisDependency(YAxis.AxisDependency.LEFT);
        set2.setLineWidth(2f);
        set2.setColor(Color.rgb(51, 204, 255));  //very light blue
        set2.setCircleColors(Color.rgb(51, 204, 255));  //very light blue
        //set2.setCircleRadius(.1f);
        set2.setDrawCircles(false);

        set2.setMode(LineDataSet.Mode.LINEAR);
        //set.setCubicIntensity(2f);
        set2.setDrawValues(false);
        return(set2);
    }

    private LineDataSet createSet9(String legend) {

        LineDataSet set2 = new LineDataSet(null, legend);
        set2.setAxisDependency(YAxis.AxisDependency.LEFT);
        set2.setLineWidth(2f);
        set2.setColor(Color.rgb(255, 102, 0));  //orange
        set2.setCircleColors(Color.rgb(255, 102, 0));  //orange
        //set2.setCircleRadius(.1f);
        set2.setDrawCircles(false);

        set2.setMode(LineDataSet.Mode.LINEAR);
        //set.setCubicIntensity(2f);
        set2.setDrawValues(false);
        return(set2);
    }








    public void showData() {
        final BleDevice bleDevice = ((OperationActivity) getActivity()).getBleDevice();
        final BluetoothGattCharacteristic characteristic = ((OperationActivity) getActivity()).getCharacteristic();
        final int charaProp = ((OperationActivity) getActivity()).getCharaProp();
        String child = characteristic.getUuid().toString() + String.valueOf(charaProp);





        for (int i = 0; i < layout_container.getChildCount(); i++) {
            layout_container.getChildAt(i).setVisibility(View.GONE);
        }
        if (childList.contains(child)) {
            layout_container.findViewWithTag(bleDevice.getKey() + characteristic.getUuid().toString() + charaProp).setVisibility(View.VISIBLE);
        } else {
            childList.add(child);

            View view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteric_operation, null);
            view.setTag(bleDevice.getKey() + characteristic.getUuid().toString() + charaProp);
            LinearLayout layout_add = (LinearLayout) view.findViewById(R.id.layout_add);
            LinearLayout layout_add2 = (LinearLayout) view.findViewById(R.id.layout_add2);
            LinearLayout layout_add3 = (LinearLayout) view.findViewById(R.id.layout_add3);
            final TextView txt_title = (TextView) view.findViewById(R.id.txt_title);
            txt_title.setText(String.valueOf(characteristic.getUuid().toString() + getActivity().getString(R.string.data_changed)));
            final TextView txt = (TextView) view.findViewById(R.id.txt);
            txt.setMovementMethod(ScrollingMovementMethod.getInstance());

            final TextView senseText1 = (TextView) view.findViewById(R.id.senseText1);
            senseText1.setMovementMethod(ScrollingMovementMethod.getInstance());
            final TextView senseText2 = (TextView) view.findViewById(R.id.senseText2);
            senseText2.setMovementMethod(ScrollingMovementMethod.getInstance());
            final TextView senseText3 = (TextView) view.findViewById(R.id.senseText3);
            senseText3.setMovementMethod(ScrollingMovementMethod.getInstance());
            /*final TextView senseText4 = (TextView) view.findViewById(R.id.senseText4);
            senseText4.setMovementMethod(ScrollingMovementMethod.getInstance());
            final TextView senseText5 = (TextView) view.findViewById(R.id.senseText5);
            senseText5.setMovementMethod(ScrollingMovementMethod.getInstance());

            final TextView senseText6 = (TextView) view.findViewById(R.id.senseText6);
            senseText6.setMovementMethod(ScrollingMovementMethod.getInstance());
            final TextView senseText7 = (TextView) view.findViewById(R.id.senseText7);
            senseText7.setMovementMethod(ScrollingMovementMethod.getInstance());
            final TextView senseText8 = (TextView) view.findViewById(R.id.senseText8);
            senseText8.setMovementMethod(ScrollingMovementMethod.getInstance());
*/
            /*final TextView senseText9 = (TextView) view.findViewById(R.id.senseText9);
            senseText9.setMovementMethod(ScrollingMovementMethod.getInstance());
            final TextView senseText10 = (TextView) view.findViewById(R.id.senseText10);
            senseText10.setMovementMethod(ScrollingMovementMethod.getInstance());
            final TextView senseText11 = (TextView) view.findViewById(R.id.senseText11);
            senseText11.setMovementMethod(ScrollingMovementMethod.getInstance());

            final TextView senseText12 = (TextView) view.findViewById(R.id.senseText12);
            senseText12.setMovementMethod(ScrollingMovementMethod.getInstance());*/


            final TextView txt0 = (TextView) view.findViewById(R.id.txt0);
            txt0.setMovementMethod(ScrollingMovementMethod.getInstance());
            //final TextView txt1 = (TextView) view.findViewById(R.id.txt1);
            //txt1.setMovementMethod(ScrollingMovementMethod.getInstance());
            //final TextView txt2 = (TextView) view.findViewById(R.id.txt2);
            //txt2.setMovementMethod(ScrollingMovementMethod.getInstance());
            //final TextView txt3 = (TextView) view.findViewById(R.id.txt3);
            //txt3.setMovementMethod(ScrollingMovementMethod.getInstance());
            //final TextView spinText = (TextView) view.findViewById(R.id.spinText);
            //spinText.setMovementMethod(ScrollingMovementMethod.getInstance());
            //final TextView txt2 = (TextView) view.findViewById(R.id.txt2);
            //txt2.setMovementMethod(ScrollingMovementMethod.getInstance());
            //final TextView txt3 = (TextView) view.findViewById(R.id.txt3);
            //txt3.setMovementMethod(ScrollingMovementMethod.getInstance());
            //final TextView txt4 = (TextView) view.findViewById(R.id.txt4);
            //txt4.setMovementMethod(ScrollingMovementMethod.getInstance());
            //final TextView txt5 = (TextView) view.findViewById(R.id.txt5);
            //txt5.setMovementMethod(ScrollingMovementMethod.getInstance());

            final LineChart temp1_sensor_graph = view.findViewById(R.id.temp1_sensor_graph);
            initGraph(temp1_sensor_graph, "Step Counter");
            /*final LineChart IMU1_sensor_graph = view.findViewById(R.id.IMU1_sensor_graph);
            initGraph2(IMU1_sensor_graph, "Accelerometer Readings");*/
            //final LineChart IMU2_sensor_graph = view.findViewById(R.id.IMU2_sensor_graph);
            //initGraph2(IMU2_sensor_graph, "Gyroscope Readings");
            //final LineChart difference_temp_sensor_graph = view.findViewById(R.id.difference_temp_sensor_graph);
            //initGraph3(difference_temp_sensor_graph, "Majority Voting Mean Minus Ambient Temperature");
            //final LineChart temp2_sensor_graph =view.findViewById(R.id.temp2_sensor_graph);
            //initGraph(temp2_sensor_graph, "Skin Temperature 2");
            //final LineChart temp3_sensor_graph =view.findViewById(R.id.temp3_sensor_graph);
            //initGraph(temp3_sensor_graph, "temp3");
            //final LineChart temp4_sensor_graph =view.findViewById(R.id.temp4_sensor_graph);
            //initGraph(temp4_sensor_graph, "temp4");
            //final LineChart ambient_sensor_graph =view.findViewById(R.id.ambient_sensor_graph);
            //initGraph(ambient_sensor_graph, "Ambient Sensor Graph");



            //final LineChart combined_graph =view.findViewById(R.id.ambient_sensor_graph);
            //initGraph2(combined_graph, "Combinecd Graph");



            switch (charaProp) {
                case PROPERTY_READ: {
                    View view_add = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteric_operation_button, null);
                    Button btn = (Button) view_add.findViewById(R.id.btn);
                    btn.setText("Capture Data");
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            BleManager.getInstance().read(
                                    bleDevice,
                                    characteristic.getService().getUuid().toString(),
                                    characteristic.getUuid().toString(),
                                    new BleReadCallback() {

                                        @Override
                                        public void onReadSuccess(final byte[] data) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    addText(txt, HexUtil.formatHexString(data, true));
                                                    addText(txt, "thanky you");

                                                }
                                            });
                                        }

                                        @Override
                                        public void onReadFailure(final BleException exception) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    addText(txt, exception.toString());
                                                }
                                            });
                                        }
                                    });
                        }
                    });
                    layout_add.addView(view_add);

                }
                break;

                case PROPERTY_WRITE: {
                    View view_add = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteric_operation_et, null);
                    final EditText et = (EditText) view_add.findViewById(R.id.et);
                    Button btn = (Button) view_add.findViewById(R.id.btn);
                    btn.setText(getActivity().getString(R.string.write));
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String hex = et.getText().toString();
                            if (TextUtils.isEmpty(hex)) {
                                return;
                            }
                            BleManager.getInstance().write(
                                    bleDevice,
                                    characteristic.getService().getUuid().toString(),
                                    characteristic.getUuid().toString(),
                                    HexUtil.hexStringToBytes(hex),
                                    new BleWriteCallback() {

                                        @Override
                                        public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    addText(txt, "write success, current: " + current
                                                            + " total: " + total
                                                            + " justWrite: " + HexUtil.formatHexString(justWrite, true));
                                                }
                                            });
                                        }

                                        @Override
                                        public void onWriteFailure(final BleException exception) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    addText(txt, exception.toString());
                                                }
                                            });
                                        }
                                    });
                        }
                    });
                    layout_add.addView(view_add);
                }
                break;

                case PROPERTY_WRITE_NO_RESPONSE: {
                    View view_add = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteric_operation_et, null);
                    final EditText et = (EditText) view_add.findViewById(R.id.et);
                    Button btn = (Button) view_add.findViewById(R.id.btn);
                    btn.setText(getActivity().getString(R.string.write));
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String hex = et.getText().toString();
                            if (TextUtils.isEmpty(hex)) {
                                return;
                            }
                            BleManager.getInstance().write(
                                    bleDevice,
                                    characteristic.getService().getUuid().toString(),
                                    characteristic.getUuid().toString(),
                                    HexUtil.hexStringToBytes(hex),
                                    new BleWriteCallback() {

                                        @Override
                                        public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    addText(txt, "write success, current: " + current
                                                            + " total: " + total
                                                            + " justWrite: " + HexUtil.formatHexString(justWrite, true));
                                                }
                                            });
                                        }

                                        @Override
                                        public void onWriteFailure(final BleException exception) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    addText(txt, exception.toString());
                                                }
                                            });
                                        }
                                    });
                        }
                    });
                    layout_add.addView(view_add);
                }
                break;

                case PROPERTY_NOTIFY: {

                    System.out.print("\n \n");
                    System.out.print("\n \n");
                    System.out.print("##########################THE CONTEXT IS################################################");
                    System.out.print(getContext());
                    View view_add = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteric_operation_button, null);
                    final Button btn = (Button) view_add.findViewById(R.id.btn);
                    btn.setBackgroundColor(Color.rgb(255,255,255));
                    btn.setText("Capture Data");


                    View view_add2 = LayoutInflater.from(getActivity()).inflate(R.layout.spinner_button, null);
                    final Spinner spinner_btn = (Spinner) view_add2.findViewById(R.id.spinner_btn);


                    final String categories[] = { "Car", "Home", "Lab", "Office", "Restaurant"};
                    //categories.add("Home");
                    //categories.add("Driving");
                    //categories.add("Lab");
                    //categories.add("Sleeping");
                    //categories.add("Outdoors");
                    //categories.add("Gym");
                    btn.setBackgroundColor(Color.rgb(255,255,255));

                    //spinner_btn.setText("Select Location");
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_button, R.id.spinText, categories);
                    //adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

                    //final String[] selected_location = new String[1];

                    spinner_btn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            Toast.makeText(getContext(), "Selected Location: "+categories[i] ,Toast.LENGTH_SHORT).show();
                            selected_location = spinner_btn.getSelectedItem().toString();
                            System.out.print("\n \n");
                            System.out.print("\n \n");
                            System.out.print("#########################The selected Item is###############################################");
                            System.out.print(selected_location);
                            System.out.print("\n \n");
                            System.out.print("\n \n");
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                    spinner_btn.setAdapter(adapter);

                    View view_add3 = LayoutInflater.from(getActivity()).inflate(R.layout.spinner_button2, null);
                    final Spinner spinner_btn2 = (Spinner) view_add3.findViewById(R.id.spinner_btn2);

                    final String activities[] = {"Cycling", "Driving", "Eating", "Jumping", "Lying Down", "Sitting", "Stretching", "Standing", "Walking", "Weightlifting"};
                    //categories.add("Home");
                    //categories.add("Driving");
                    //categories.add("Lab");
                    //categories.add("Sleeping");
                    //categories.add("Outdoors");
                    //categories.add("Gym");


                    //spinner_btn.setText("Select Location");
                    ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getActivity(), R.layout.spinner_button2, R.id.spinText2, activities);
                    //adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

                    //final String[] selected_location = new String[1];

                    spinner_btn2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            Toast.makeText(getContext(), "Selected Activity: "+activities[i] ,Toast.LENGTH_SHORT).show();
                            selected_activity = spinner_btn2.getSelectedItem().toString();
                            System.out.print("\n \n");
                            System.out.print("\n \n");
                            System.out.print("#########################The selected Item is###############################################");
                            System.out.print(selected_activity);
                            System.out.print("\n \n");
                            System.out.print("\n \n");
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });


                    spinner_btn2.setAdapter(adapter2);

                    //dataRef = myRef.child(user).child(selected_location).child(selected_activity);


                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View view) {
                            if (btn.getText().toString().equals("Capture Data")) {
                            //if (spinner_btn.equals("Select Location")) {
                                btn.setBackgroundColor(Color.rgb(255,255,255));
                                //btn.setTextColor(Color.rgb(255, 255, 255));
                                btn.setText("Stop");
                                BleManager.getInstance().notify(
                                        bleDevice,
                                        characteristic.getService().getUuid().toString(),
                                        characteristic.getUuid().toString(),
                                        new BleNotifyCallback() {

                                            @Override
                                            public void onNotifySuccess() {
                                                txt0.setTextSize(28);
                                                txt0.setTextColor(Color.rgb(255, 102, 0));
                                                addText(txt0, "Capturing Activity Data");

                                                //txt1.setTextSize(28);
                                                //txt1.setTextColor(Color.rgb(255, 102, 0));

                                                //addText(txt1, "Accelerometer Data");
                                                //runOnUiThread(new Runnable() {
                                                //    @Override
                                                  //  public void run() {




                                                        //txt2.setTextSize(28);
                                                        //txt2.setTextColor(Color.rgb(255, 102, 0));

                                                        //addText(txt2, "Gyroscope Data");

                                                        //txt3.setTextSize(28);
                                                        //txt3.setTextColor(Color.rgb(255, 102, 0));

                                                        //addText(txt3, "Skin Temperature Minus Ambient Temperature");

                                                        //txt.setTextSize(24);
                                                        //addText(txt, "Skin Sensor Temperature Readings");
                                                       // txt2.setTextSize(24);
                                                       // txt2.setTextColor(Color.rgb(255, 102, 0));
                                                       // addText(txt2, "Skin Temperature 2");
                                                       // txt3.setTextSize(24);
                                                       // txt3.setTextColor(Color.rgb(255, 102, 0));
                                                       // addText(txt3, "Skin Temperature 3");
                                                       // txt4.setTextSize(24);
                                                       // txt4.setTextColor(Color.rgb(255, 102, 0));
                                                       // addText(txt4, "Skin Temperature 4");
                                                       // txt5.setTextColor(Color.rgb(255, 102, 0));
                                                       // txt5.setTextSize(24);
                                                       // addText(txt5, "Ambient Temperature");




                                                    //}
                                                //});

                                            }

                                            @Override
                                            public void onNotifyFailure(final BleException exception) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        addText(txt, exception.toString());
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCharacteristicChanged(byte[] data) {
                                                converter = HexUtil.formatHexString(characteristic.getValue(), false);

                                                //String intBits = String.valueOf(converter).toString();
                                                //ascii = Float.valueOf(intBits);
                                                //System.out.printf("%f ", floatValue);
                                                        /*System.out.print("\n \n");
                                                        System.out.print("**********************\n");
                                                        System.out.print(magic);
                                                        System.out.print("\n \n");*/
                                                ascii = fromHexString(converter);
                                                values = ascii.split(",");
                                                //float floatValue4 = Float.parseFloat(ascii);
                                                       /* System.out.print("\n \n");
                                                        System.out.print("**********************\n");
                                                        System.out.print(values);
                                                        System.out.print("\n \n");*/

                                                //magic = HexUtil.formatHexString(characteristic.getValue(), false).substring(0,10);
                                                       /* System.out.print("\n \n");
                                                        System.out.print("**********************\n");
                                                        System.out.print(values[0]);
                                                        System.out.print("\n \n");*/
                                                //ascii = fromHexString(values[0]);
                                                floatValue = Float.parseFloat(values[0]);
                                                        /*System.out.print("\n \n");
                                                        System.out.print("**********************\n");
                                                        //System.out.println(ascii);
                                                        System.out.print(floatValue);
                                                        System.out.print("\n \n");*/
                                                //System.out.println(new String(bytes, "UTF-8"));

                                                //magic = HexUtil.formatHexString(characteristic.getValue(), false).substring(12,22);
                                                        /*System.out.print("\n \n");
                                                        System.out.print("**********************\n");
                                                        System.out.print(values[1]);
                                                        System.out.print("\n \n");*/
                                                //ascii = fromHexString(values[1]);
                                                floatValue2 = Float.parseFloat(values[1]);
                                                        /*System.out.print("\n \n");
                                                        System.out.print("**********************\n");
                                                        System.out.print(floatValue2);
                                                        System.out.print("\n \n");
                                                        System.out.println(characteristic.getValue().length);*/

                                                //magic = HexUtil.formatHexString(characteristic.getValue(), false).substring(24,36);
                                                        /*System.out.print("\n \n");
                                                        System.out.print("**********************\n");
                                                        System.out.print(values[2]);
                                                        System.out.print("\n \n");*/
                                                //ascii = fromHexString(values[2]);
                                                floatValue3 = Float.parseFloat(values[2]);
                                                        /*System.out.print("\n \n");
                                                        System.out.print("**********************\n");
                                                        System.out.print(floatValue3);
                                                        System.out.print("\n \n");*/
                                               // label1 = "Steps";
                                                //String label2 = "Hypotenuse";
                                                //String label3 = "Threshold";
                                                s = new SimpleDateFormat("ddMMyyyyhhmmssSS");
                                                ts = s.format(new Date());
                                                dataRef.child("Accelerometer_x").child(ts).setValue(floatValue);
                                                dataRef.child("Accelerometer_y").child(ts).setValue(floatValue2);
                                                dataRef.child("Accelerometer_z").child(ts).setValue(floatValue3);

                                                pythagoreanTheorem = Math.sqrt(Math.pow(floatValue,2) + Math.pow(floatValue2,2) + Math.pow(floatValue3,2));
                                                //threshold = 11.78;

                                                absoluteValue = pythagoreanTheorem.floatValue();
                                                        /*System.out.print("**********************\n");
                                                        System.out.print("Creating the array");
                                                        System.out.print("\n \n");
                                                        filter[windowCounter] = absoluteValue;
                                                        System.out.print("**********************\n");
                                                        System.out.print("Done Creating the Array");
                                                        System.out.print("\n \n");*/


                                                filter[windowCounter] = absoluteValue;



                                                if (windowCounter == window){
                                                    sum = 0;
                                                    while (windowCounter > 0){

                                                        sum = sum + filter[windowCounter];
                                                        windowCounter = windowCounter - 1;
                                                    }
                                                    average = sum/window;
                                                    if (average > threshold){
                                                        flag = flag + 1;
                                                        if (flag == 1){
                                                            steps = steps + 1;

                                                            showSenorValue(senseText1, steps, label1);


                                                            dataRef.child("Steps").child(ts).setValue(steps);
                                                            //dataRef.child("Hypotenuse").child(ts).setValue(average);
                                                            //dataRef.child("Hypotenuse").child(ts).setValue(average);

                                                        }
                                                    }


                                                    if (average < threshold){
                                                        flag = 0;
                                                    }

                                                    updateGraph(temp1_sensor_graph, steps, average, threshold, "Step counter Algorithm");
                                                    average = 0;
                                                    windowCounter = 0;
                                                }




                                                windowCounter = windowCounter + 1;
                                                //runOnUiThread(new Runnable() {
                                                  //  @SuppressLint("DefaultLocale")
                                                   // @Override
                                                    //public void run() {
                                                        //addText(txt, HexUtil.formatHexString(characteristic.getValue(), true));
                                                        //addText(txt, "thanky you");
                                                       /* for(int i = 0; i < 11; i++){
                                                            String sensor = HexUtil.formatHexString(characteristic.getValue(), false).substring(i*8, i*8+8);

                                                            int intBits = Long.valueOf(sensor, 16).intValue();
                                                            float floatValue = Float.intBitsToFloat(intBits);
                                                            System.out.printf("%f ", floatValue);
                                                        }*/
                                                        //System.out.println();

                                                        /*String sensor = HexUtil.formatHexString(characteristic.getValue(), false).substring(0,8);
                                                        int intBits = Long.valueOf(sensor, 16).intValue();
                                                        float floatValue = Float.intBitsToFloat(intBits);*/
                                                        //System.out.print("\n \n");
                                                        //System.out.print("**********************\n");
                                                        //System.out.print(absoluteValue);
                                                        //System.out.print("\n \n");

                                                        //showSenorValue(senseText1, steps, label1);
                                                        //showSenorValue(senseText2, average, label2);
                                                        //showSenorValue(senseText3, threshold, label3);


                                                       /* int intBit = Long.valueOf(magic, 16).intValue();
                                                        //String magical = String.intBitstoString(intBits);

                                                        String sensor2 = HexUtil.formatHexString(characteristic.getValue(), false).substring(0, 8);
                                                        intBits = Long.valueOf(sensor2, 16).intValue();
                                                        //float floatValue2 = Float.intBitsToFloat(intBits);

                                                        String sensor3 = HexUtil.formatHexString(characteristic.getValue(), false).substring(0, 8);
                                                        intBits = Long.valueOf(sensor3, 16).intValue();
                                                        floatValue3 = Float.intBitsToFloat(intBits);

                                                        String sensor4 = HexUtil.formatHexString(characteristic.getValue(), false).substring(0, 8);
                                                        intBits = Long.valueOf(sensor4, 16).intValue();
                                                        float floatValue4 = Float.intBitsToFloat(intBits);

                                                        String sensor5 = HexUtil.formatHexString(characteristic.getValue(), false).substring(0, 8);
                                                        intBits = Long.valueOf(sensor5, 16).intValue();
                                                        float floatValue5 = Float.intBitsToFloat(intBits);


                                                        System.out.print("\n \n");
                                                        System.out.print("**********************\n");
                                                        //System.out.print(intBit);
                                                        System.out.print("\n \n");
                                                        */


                                                        //FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                        //DatabaseReference myRef = database.getReference();
                                                        //myRef.setValue("H");



                                                        //Long tsLong = System.currentTimeMillis();
                                                        //String ts = tsLong.toString();

                                                        /*List<Float> temps = new LinkedList<Float>();//(floatValue, floatValue2, floatValue3, floatValue4, floatValue5)
                                                        temps.add(floatValue);
                                                        temps.add(floatValue2);
                                                        temps.add(floatValue3);
                                                        temps.add(floatValue4);
                                                        temps.add(floatValue5);*/


                                                        //dataRef.child("Temperature5").child(ts).setValue(floatValue5);

                                                        //DatabaseReference temp1 = dataRef.child("Temperature1");

                                                        //temp1.push(floatValue2);


                                                        //dataRef.push("Temperature1", floatValue2);

                                                        //dataRef.("Temperature1", floatValue);
                                                        //mDatabase.child("Temperature1").setValue(floatValue);
                                                        //mDatabase.setValue(floatValue);

                                                        /*String label4 = "x";
                                                        String label5 = "y";
                                                        String label6 = "z";

                                                        showSenorValue(senseText6, floatValue, label4);
                                                        showSenorValue(senseText7, floatValue2, label5);
                                                        showSenorValue(senseText8, floatValue3, label6);*/

                                                        //showSenorValue(senseText4, floatValue4, label4);
                                                        //showSenorValue(senseText5, floatValue5, label5);

                                                        //updateGraph2(IMU1_sensor_graph, floatValue, floatValue2, floatValue3, "Raw Accelerometer Values");

                                                        /*float difference1 = Math.abs(floatValue - floatValue2);
                                                        float difference2 = Math.abs(floatValue - floatValue3);
                                                        float difference3 = Math.abs(floatValue2 - floatValue3);

                                                        float majorityVoting = 0f;

                                                        if (difference1 <= difference2 && difference1<= difference3){
                                                            majorityVoting = ((floatValue + floatValue2)/2);
                                                        }
                                                        if (difference2 <= difference1 && difference2<= difference3){
                                                            majorityVoting = ((floatValue + floatValue3)/2);
                                                        }
                                                        if (difference3 <= difference2 && difference3<= difference1){
                                                            majorityVoting = ((floatValue2 + floatValue3)/2);
                                                        }

                                                        float plot_value_difference = (majorityVoting - floatValue5);

                                                        String label12 = "Celsius";

                                                        showSenorValue(senseText12, majorityVoting, label12);

                                                        updateGraph3(difference_temp_sensor_graph, plot_value_difference, "Majority Voting Skin Temperature Minus Ambient Temperature");

                                                        if(majorityVoting >= 36.0)
                                                        {

                                                            displayNotification(getContext());
                                                        }



                                                        String sensor6 = HexUtil.formatHexString(characteristic.getValue(), false).substring(0, 8);
                                                        intBits = Long.valueOf(sensor6, 16).intValue();
                                                        float floatValue6 = Float.intBitsToFloat(intBits);

                                                        String sensor7 = HexUtil.formatHexString(characteristic.getValue(), false).substring(0, 8);
                                                        intBits = Long.valueOf(sensor7, 16).intValue();
                                                        float floatValue7 = Float.intBitsToFloat(intBits);

                                                        String sensor8 = HexUtil.formatHexString(characteristic.getValue(), false).substring(0, 8);
                                                        intBits = Long.valueOf(sensor8, 16).intValue();
                                                        float floatValue8 = Float.intBitsToFloat(intBits);

                                                        String label6 = "x";
                                                        String label7 = "y";
                                                        String label8 = "z";
                                                        showSenorValue(senseText6, floatValue6, label6);
                                                        showSenorValue(senseText7, floatValue7, label7);
                                                        showSenorValue(senseText8, floatValue8, label8);

                                                        activity = Math.abs(floatValue6) + Math.abs(floatValue7) + Math.abs(floatValue8);

                                                        updateGraph2(IMU1_sensor_graph, floatValue6, floatValue7, floatValue8,  "X-axis");

                                                        String sensor9 = HexUtil.formatHexString(characteristic.getValue(), false).substring(64, 72);
                                                        intBits = Long.valueOf(sensor9, 16).intValue();
                                                        float floatValue9 = Float.intBitsToFloat(intBits);

                                                        String sensor10 = HexUtil.formatHexString(characteristic.getValue(), false).substring(72, 80);
                                                        intBits = Long.valueOf(sensor10, 16).intValue();
                                                        float floatValue10 = Float.intBitsToFloat(intBits);

                                                        String sensor11 = HexUtil.formatHexString(characteristic.getValue(), false).substring(80, 88);
                                                        intBits = Long.valueOf(sensor11, 16).intValue();
                                                        float floatValue11 = Float.intBitsToFloat(intBits);

                                                        String label9 = "_x";
                                                        String label10 = "_y";
                                                        String label11 = "_z";
                                                        showSenorValue(senseText9, floatValue9, label9);
                                                        showSenorValue(senseText10, floatValue10, label10);
                                                        showSenorValue(senseText11, floatValue11, label11);

                                                        updateGraph2(IMU2_sensor_graph, floatValue9, floatValue10, floatValue11,  "X-axis");
                                                           */
                                                        /*DatabaseReference activityRef = myRef.child(user+"/"+selected_activity+"/"+selected_location+"/Activity");

                                                        List<Float> activity = new LinkedList<Float>();//(floatValue, floatValue2, floatValue3, floatValue4, floatValue5)
                                                        activity.add(floatValue6);
                                                        activity.add(floatValue7);
                                                        activity.add(floatValue8);
                                                        activity.add(floatValue9);
                                                        activity.add(floatValue10);
                                                        activity.add(floatValue11);


                                                        activityRef.child(ts).setValue(activity);*/
                                                        //activityRef.child("acc-y-axis").child(ts).setValue(floatValue7);
                                                        //activityRef.child("acc-z-axis").child(ts).setValue(floatValue8);
                                                        //activityRef.child("gyro-x-axis").child(ts).setValue(floatValue9);
                                                        //activityRef.child("gyro-y-axis").child(ts).setValue(floatValue10);
//                                                        activityRef.child("gyro-z-axis").child(ts).setValue(floatValue11);





                                                        //updateGraph(temp3_sensor_graph, floatValue, floatValue2, floatValue3, floatValue4, "Other Temperature");




                                                        //updateGraph(temp4_sensor_graph, floatValue, floatValue2, floatValue3, floatValue4, "Other Temperature");




                                                        //updateGraph(ambient_sensor_graph, floatValue, floatValue2, floatValue3, floatValue4, "Ambient Temperature");


                                                    //}
                                               // });
                                            }
                                        });
                            } else {
                                btn.setText("Capture Data");
                                BleManager.getInstance().stopNotify(
                                        bleDevice,
                                        characteristic.getService().getUuid().toString(),
                                        characteristic.getUuid().toString());
                            }
                        }
                    });
                    layout_add.addView(view_add);
                    layout_add2.addView(view_add2);
                    layout_add3.addView(view_add3);

                }
                break;

                case PROPERTY_INDICATE: {
                    View view_add = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteric_operation_button, null);
                    final Button btn = (Button) view_add.findViewById(R.id.btn);
                    btn.setText("Capture Data");

                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (btn.getText().toString().equals("Capture Data")) {
                                btn.setText("Stop");
                                BleManager.getInstance().indicate(
                                        bleDevice,
                                        characteristic.getService().getUuid().toString(),
                                        characteristic.getUuid().toString(),
                                        new BleIndicateCallback() {

                                            @Override
                                            public void onIndicateSuccess() {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        addText(txt, "indicate success");
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onIndicateFailure(final BleException exception) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        addText(txt, exception.toString());
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCharacteristicChanged(byte[] data) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        addText(txt, HexUtil.formatHexString(characteristic.getValue(), true));
                                                    }
                                                });
                                            }
                                        });
                            } else {
                                btn.setText("Capture Data");
                                BleManager.getInstance().stopIndicate(
                                        bleDevice,
                                        characteristic.getService().getUuid().toString(),
                                        characteristic.getUuid().toString());
                            }
                        }
                    });
                    layout_add.addView(view_add);

                }
                break;
            }

            layout_container.addView(view);
        }
    }

    private void runOnUiThread(Runnable runnable) {
        if (isAdded() && getActivity() != null)
            System.out.println("Running on UI thread");
            getActivity().runOnUiThread(runnable);
    }

    private void addText(TextView textView, String content) {
        textView.append(content);
        textView.append("\n");
        offset = textView.getLineCount() * textView.getLineHeight();
        if (offset > textView.getHeight()) {
            textView.scrollTo(0, offset - textView.getHeight());
        }
    }
    private void showSenorValue(TextView sensors, float sensor_value, String value) {

        sensors.setTextSize(14);
        //sensors.setTextAlignment(5);

        //sensors.setTextColor(Color.rgb(255, 102, 0));
        if (value == "Hypotenuse") {
            sensors.setTextColor(Color.rgb(255, 153, 0));

            addText(sensors, String.format("%s :  %f m/s^2", value, sensor_value));
        }
        if (value == "Threshold") {
            sensors.setTextColor(Color.rgb(51, 204, 255));

            addText(sensors, String.format("%s:  %f m/s^2", value, sensor_value));
        }
        if (value == "Steps") {
            sensors.setTextColor(Color.rgb(255, 102, 0));
            //sensors.setTextSize(24);
            sensor_value = Math.round(sensor_value);
            int value1 = (int) sensor_value;
            addText(sensors, String.format("%s Taken:   %d ", value, value1));
        }
        if (value == "4") {
            sensors.setTextColor(Color.rgb(79, 121, 66));
            addText(sensors, String.format("Skin Sensor%s Temperature:   %f Celsius", value, sensor_value));
        }

        if (value == "5") {
            sensors.setTextColor(Color.rgb(204, 204, 204));
            addText(sensors, String.format("Ambient Sensor Temperature:   %f Celsius", sensor_value));

        }
        if (value == "x") {
            sensors.setTextColor(Color.rgb(255, 102, 0));
            addText(sensors, String.format("Accelerometer Sensor %s-axis:  %f m/s^2", value, sensor_value));
        }
        if (value == "y") {
            sensors.setTextColor(Color.rgb(255, 153, 0));
            addText(sensors, String.format("Accelerometer Sensor %s-axis:  %f m/s^2", value, sensor_value));
        }
        if (value == "z") {
            sensors.setTextColor(Color.rgb(51, 204, 255));
            addText(sensors, String.format("Accelerometer Sensor %s-axis:   %f m/s^2", value, sensor_value));

        }
        if (value == "_x") {
            sensors.setTextColor(Color.rgb(255, 102, 0));
            addText(sensors, String.format("Gyroscope Sensor x-axis:  %f RPS", sensor_value));
        }
        if (value == "_y") {
            sensors.setTextColor(Color.rgb(255, 153, 0));
            addText(sensors, String.format("Gyroscope Sensor y-axis:  %f RPS", sensor_value));
        }
        if (value == "_z") {
            sensors.setTextColor(Color.rgb(51, 204, 255));
            addText(sensors, String.format("Gyroscope Sensor z-axis:   %f RPS", sensor_value));

        }
        if (value == "Celsius") {
            sensors.setTextColor(Color.rgb(255, 102, 0));
            addText(sensors, String.format("Majority Voting - Ambient Temperature:  %f %s", sensor_value, value));
        }
    }
    public String fromHexString(String hex) {
        //StringBuilder str = new StringBuilder();

        for (int i = 0; i < hex.length(); i+=2) {


                //System.out.println(Integer.parseInt(hex.substring(i, i + 2), 16));

                    buff.append((char) Integer.parseInt(hex.substring(i, i + 2), 16));

            //str.append((char) Integer.parseInt(hex.substring(i, i + 2), 16));
        }
        //String str1 = str.toString();
        //str1 = str1.replaceAll (",", "");
        finalString = buff.toString();
        //int length = buff.end;
        buff.delete(0,buff.length());
        return finalString;
    }
}
