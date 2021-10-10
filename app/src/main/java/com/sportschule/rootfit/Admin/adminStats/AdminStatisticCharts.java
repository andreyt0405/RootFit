package com.sportschule.rootfit.Admin.adminStats;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sportschule.rootfit.Admin.AdminActivity;
import com.sportschule.rootfit.R;

import java.util.ArrayList;

public class AdminStatisticCharts extends AppCompatActivity {
    public static int count = 0;
    private final FirebaseDatabase dbPhone = FirebaseDatabase.getInstance();
    private final DatabaseReference Statistics = dbPhone.getReference().child("Statistics");
    private static final String TAG ="AdminStatistic";
    ArrayMap<String,Float> trainingStatisticMap = new ArrayMap<>();
    ArrayMap<String,Float> personalAndStateMap = new ArrayMap<>();
    ArrayMap<String,Float> signINAndOUTMap = new ArrayMap<>();
    ArrayMap<String,Integer> agesStatisticMap = new ArrayMap<>();
    Description description = new Description();
    PieChart pieChartTraining;
    BarChart barChart;
    LineChart lineChart;
    RadarChart radarChartAge;

    public void onBackPressed() {
        Intent intent = new Intent(AdminStatisticCharts.this, AdminActivity.class);
        AdminStatisticCharts.this.startActivity(intent);
        AdminStatisticCharts.this.finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_statistic_chart);
        pieChartTraining = (PieChart)findViewById(R.id.pieChartTraining);
        barChart = (BarChart) findViewById(R.id.barChart);
        lineChart = (LineChart)findViewById(R.id.lineChartSign);
        radarChartAge = (RadarChart)findViewById(R.id.ageRadarChart);
        description.setEnabled(false);
        getRealTimeStatistic();
    }

    private void getRealTimeStatistic() {
        Statistics.orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    if(dataSnapshot.getKey().contains("Training_"))
                    {
                        trainingStatisticMap.put(dataSnapshot.getKey(),Float.parseFloat(dataSnapshot.getValue().toString()));
                    }
                    if(dataSnapshot.getKey().contains("Female") || dataSnapshot.getKey().contains("Male") || dataSnapshot.getKey().contains("Trainer")
                    || dataSnapshot.getKey().contains("Trainee"))
                    {
                        personalAndStateMap.put(dataSnapshot.getKey(),Float.parseFloat(dataSnapshot.getValue().toString()));
                    }
                    if(dataSnapshot.getKey().contains("SignIN") || dataSnapshot.getKey().contains("SignOUT"))
                    {
                        signINAndOUTMap.put(dataSnapshot.getKey(),Float.parseFloat(dataSnapshot.getValue().toString()));
                    }
                    if(dataSnapshot.getKey().contains("age"))
                    {
                        agesStatisticMap.put(dataSnapshot.getKey(),Integer.parseInt(dataSnapshot.getValue().toString()));
                    }
                }
                addDatePieChartTraining();
                addDataBarChartPersonalAndState();
                addDataLineChartSigning();
//                addDataPieChartAge();
                addDataPieChartAge();
                Log.d("TrainingStatisticMap", trainingStatisticMap.toString());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @SuppressLint("ResourceAsColor")
    private void addDatePieChartTraining() {
        pieChartTraining.setDescription(description);
        pieChartTraining.setRotationEnabled(true);
        pieChartTraining.setCenterText("Training");
        pieChartTraining.setHoleRadius(25f);
        pieChartTraining.setCenterTextSize(13);
        pieChartTraining.setEntryLabelColor(Color.BLACK);
        pieChartTraining.setDrawCenterText(true);
        Log.d(TAG,"add data");
        ArrayList<PieEntry> yEntry = new ArrayList<>();
        for(int i =0; i<trainingStatisticMap.size(); i++)
        {
            String training = trainingStatisticMap.keyAt(i).substring(trainingStatisticMap.keyAt(i).indexOf("_")+1);
            if(trainingStatisticMap.valueAt(i)!=0.0) {
                yEntry.add(new PieEntry(trainingStatisticMap.valueAt(i), training));
                Log.d(TAG, trainingStatisticMap.valueAt(i).toString());
            }
        }
        PieDataSet pieDataSet = new PieDataSet(yEntry,"Training Statistics");
        pieDataSet.setSliceSpace(2);
        pieDataSet.setValueTextSize(13);
        pieDataSet.setColors(ColorTemplate.JOYFUL_COLORS);

        PieData pieData = new PieData(pieDataSet);
        pieData.setDrawValues(true);
        pieData.setValueTextColor(Color.BLACK);
        pieChartTraining.setData(pieData);
        pieChartTraining.invalidate();
    }
    @SuppressLint("ResourceAsColor")
    private void addDataBarChartPersonalAndState()
    {
        barChart.setDescription(description);
        barChart.setDrawValueAboveBar(true);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(true);
        ArrayList<BarEntry> barEntry = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        for(int i = 0; i< personalAndStateMap.size(); i++)
        {
            barEntry.add(new BarEntry(i, personalAndStateMap.valueAt(i)));
            labels.add(personalAndStateMap.keyAt(i));
        }
        BarDataSet barDataSet = new BarDataSet(barEntry,"Personal and States");
        barDataSet.setValueTextSize(13);
        barDataSet.setColors(ColorTemplate.PASTEL_COLORS);

        BarData data = new BarData(barDataSet);
        data.setValueTextSize(13);
        barChart.setData(data);
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(13);
        xAxis.setLabelCount(labels.size());
    }
    private void addDataLineChartSigning()
    {
        lineChart.setDescription(description);
        lineChart.setDragEnabled(true);
        lineChart.setDrawGridBackground(true);

        ArrayList<Entry> SignValue = new ArrayList<>();
        ArrayList<Entry> outSignValue = new ArrayList<>();
        SignValue.add(new Entry(count,signINAndOUTMap.valueAt(0)));
        outSignValue.add(new Entry(count,signINAndOUTMap.valueAt(1)));
        count++;

        LineDataSet set1 = new LineDataSet(SignValue,"SignIn");
        LineDataSet set2 = new LineDataSet(outSignValue,"SignOut");
        set1.setDrawCircleHole(true);
        set2.setDrawCircleHole(true);
        set1.setCircleHoleColor(Color.GREEN);
        set2.setCircleHoleColor(Color.RED);
        set1.setCircleColors(Color.GREEN);
        set2.setCircleColors(Color.RED);
        set1.setColors(Color.GREEN);
        set2.setColors(Color.RED);
        set1.setCircleRadius(5);
        set2.setCircleRadius(5);

        set1.setFillAlpha(110);
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);
        dataSets.add(set2);
        LineData data = new LineData(dataSets);
        data.setValueTextColor(Color.BLACK);
        data.setValueTextSize(13);
        lineChart.setData(data);

    }
        private void addDataPieChartAge() {
            radarChartAge.setDescription(description);
            ArrayList<RadarEntry> keys = new ArrayList<>();
            for(int i=0; i<agesStatisticMap.size(); i++)
            {
                if(agesStatisticMap.valueAt(i)!=0.0) {
                    keys.add(new RadarEntry(agesStatisticMap.valueAt(i)));
                }
            }
            RadarDataSet radarDataSet = new RadarDataSet(keys, "Trainees Age");
            radarDataSet.setColor(Color.BLUE);
            radarDataSet.setFormLineWidth(2f);
            radarDataSet.setValueTextColor(Color.BLACK);
            radarDataSet.setValueTextSize(13);
            RadarData data = new RadarData(radarDataSet);
            String [] labels = {"11-20","21-30","31-40","41-50","51-60","61-70","71-80","81-90","91-99"};
            XAxis xAxis = radarChartAge.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
            radarChartAge.setData(data);
        }
}