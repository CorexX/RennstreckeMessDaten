package com.example.team919.rennstreckemessdaten;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Calendar;
import java.util.Timer;

public class MainActivity extends AppCompatActivity {
    EditText edit_Treshold;
    EditText txt_name;
    Button btn_start;
    TextView txtACC;
    TextView txtGyro;
    TextView txtRichtung;
    Spinner spinnerSensorSpeed;


    int flag = 0;
    int rechts = 0;
    int links = 0;
    double tres = 0.5;

    double summe;
    int anzsumme;

    long starttimestamp = 0;
    long endtimestamp = 0;

    double timesek;
    double avgdeg;

    String drehung = "gerade";

    DecimalFormat decimalFormat;

    SensorManager sensorManager;

    LinkedList<Long> timeList = new LinkedList<>();
    LinkedList<Float> accX = new LinkedList<>();
    LinkedList<Float> accY = new LinkedList<>();
    LinkedList<Float> accZ = new LinkedList<>();
    LinkedList<Float> gyroX = new LinkedList<>();
    LinkedList<Float> gyroY = new LinkedList<>();
    LinkedList<Float> gyroZ = new LinkedList<>();
    LinkedList<String> gyroDrehung = new LinkedList<>();
    LinkedList<String> gyroList = new LinkedList<>();
    LinkedList<String> ACCList = new LinkedList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edit_Treshold = findViewById(R.id.edit_Treshold);
        btn_start = findViewById(R.id.btn_start);
        txtACC = findViewById(R.id.txtACC);
        txtGyro = findViewById(R.id.txtGyro);
        txtRichtung =  findViewById(R.id.txtRichtung);
        spinnerSensorSpeed = findViewById(R.id.spinnerSensorSpeed);
        txt_name = findViewById(R.id.txt_name);

        decimalFormat = new DecimalFormat("00.00");


        //Inhalt der Spinner
        LinkedList<String> spinnerSensorSpeedList = new LinkedList<>();
        spinnerSensorSpeedList.add("FASTEST");
        spinnerSensorSpeedList.add("GAME");
        spinnerSensorSpeedList.add("UI");
        spinnerSensorSpeedList.add("NORMAL");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerSensorSpeedList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSensorSpeed.setAdapter(arrayAdapter);




        //############################SensorManager/Listener############################
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        final SensorEventListener SElistener= new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                switch(event.sensor.getType()){
                    case Sensor.TYPE_ACCELEROMETER:
                        txtACC.setText("ACC:\n" + "X: " + decimalFormat.format(event.values[0])  + "\nY: " + decimalFormat.format(event.values[1]) + "\nZ: " + decimalFormat.format(event.values[2]));
                        //ACCList.add("\""+ Calendar.getInstance().getTime().getTime() + "\";\"" + event.values[0]  + "\";\"" +event.values[1]  + "\";\"" +event.values[2]  + "\";");
                        timeList.add(Calendar.getInstance().getTime().getTime());
                        accX.add(event.values[0]);
                        accY.add(event.values[1]);
                        accZ.add(event.values[2]);

                    break;

                    case Sensor.TYPE_GYROSCOPE:

                        if(Math.abs(event.values[2]) >= tres){   //treshold bestimmen

                            //summe+=Math.abs(event.values[2]);
                            //anzsumme++;
                            if(event.values[2]>0){              // linksdrehung
                                drehung = "Links";
                                txtRichtung.setText("<");
                                txtRichtung.setBackgroundColor(Color.RED);
                                //if(flag == 0 ) starttimestamp = event.timestamp;
                                flag=1;

                            }
                            if(event.values[2]<0) {              // rechtsdrehung
                                drehung = "Rechts";
                                txtRichtung.setText(">");
                                txtRichtung.setBackgroundColor(Color.GREEN);
                                // if(flag == 0 ) starttimestamp = event.timestamp;
                                flag=2;
                            }
                        }else{
                            txtRichtung.setText("|");
                            drehung = "gerade";           // keine drehung
                            txtRichtung.setBackgroundColor(Color.WHITE);
                            if(flag == 1){                      // linksdrehung beenden
                                // links++;
                                // endtimestamp = event.timestamp;
                                flag =0;

                            }
                            if(flag == 2){                      // rechtsdrehung beenden
                                // rechts++;
                                //endtimestamp = event.timestamp;
                                flag =0;

                            }
                        }
                        txtGyro.setText("GYRO:\n" + "X: " + decimalFormat.format(event.values[0])  + "\nY: " + decimalFormat.format(event.values[1]) + "\nZ: " + decimalFormat.format(event.values[2]));
                        //gyroList.add("\"" + event.values[0]  + "\";\"" +event.values[1]  + "\";\"" +event.values[2]  + "\";\"" + drehung +"\"\n");
                        gyroX.add(event.values[0]);
                        gyroY.add(event.values[1]);
                        gyroZ.add(event.values[2]);
                        gyroDrehung.add(drehung);

                        break;

                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btn_start.getText() != "STOP") {
                    registerSensor(SElistener);
                    spinnerSensorSpeed.setEnabled(false);
                    btn_start.setText("STOP");

                }else{
                    sensorManager.unregisterListener(SElistener);
                    btn_start.setText("START");
                    spinnerSensorSpeed.setEnabled(true);

                    //############################FileWriter###########################
                    File dir = new File(Environment.getExternalStorageDirectory(),"Praktikum2");
                    if(!dir.exists()){
                        if(!dir.mkdirs()){
                            Log.e("Error", "Can't create Directory");
                        }
                    }


                    int j = 0;
                    File file = new File(dir,txt_name.getText() + Integer.toString(j)+".csv");
                    while(file.exists()){
                        file = new File(dir,txt_name.getText() + Integer.toString(j)+".csv");
                        j++;
                    }

                    accX = glaetten(accX, 15);
                    //accY = glaetten(accY, 15);
                    //accZ = glaetten(accZ, 15);
                    //gyroX = glaetten(gyroX, 5);
                    //gyroY = glaetten(gyroY, 5);
                    gyroZ = glaetten(gyroZ, 5);
                    for(int i = 0; i<accX.size(); i++){
                        ACCList.add("\""+ timeList.get(i) + "\";\"" + accX.get(i)  + "\";\"" + accY.get(i)  + "\";\"" + accZ.get(i)  + "\";");
                    }
                    for(int i = 0; i<gyroX.size(); i++){
                        gyroList.add("\"" + gyroX.get(i)  + "\";\"" + gyroY.get(i)  + "\";\"" + gyroZ.get(i)  + "\";\"" + gyroDrehung.get(i) +"\"\n");
                    }

                    int smaler;
                    if(ACCList.size()<gyroList.size()){
                        smaler =ACCList.size();
                    }else {
                        smaler = gyroList.size();
                    }

                    try{
                        FileOutputStream fos = new FileOutputStream(file,false);
                        String bezeichnung = "\"Timestamp\";\"ACCX\";\"ACCY\";\"ACCZ\";\"GYROX\";\"GYROY\";\"GYROZ\";\"Drehung\"\n";
                        fos.write(bezeichnung.getBytes());
                        for(int i=0;i<smaler;i++){
                            fos.write(ACCList.get(i).getBytes());
                            fos.write(gyroList.get(i).getBytes());
                        }

                        fos.close();

                    } catch (FileNotFoundException e1){
                        e1.printStackTrace();
                        Log.e("Error", "FileNotFoundException)");
                    } catch (IOException e2){
                        e2.printStackTrace();
                        Log.e("Error", "IOException");
                    }

                    ACCList.clear();
                    gyroList.clear();
                    timeList.clear();
                    accX.clear();
                    accY.clear();
                    accZ.clear();
                    gyroX.clear();
                    gyroY.clear();
                    gyroZ.clear();
                    gyroDrehung.clear();


                }

            }
        });




     edit_Treshold.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            tres = Double.valueOf(edit_Treshold.getText().toString());
        }
    });
    }

    private void registerSensor(SensorEventListener SE){
        sensorManager.unregisterListener(SE);
        sensorManager.registerListener(SE,sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),spinnerSensorSpeed.getSelectedItemPosition());
        sensorManager.registerListener(SE,sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),spinnerSensorSpeed.getSelectedItemPosition());

    }

    private LinkedList<Float> glaetten(LinkedList<Float> linkedList, int range){
        float temp = 0;
        if((range%2) != 1) range++;
        for(int i = ((range-1)/2); i < linkedList.size()-((range-1)/2); i++){
            for(int j = 0-(range-1)/2; j<= (range-1)/2; j++){
                temp += linkedList.get(i+j);
            }
            linkedList.set(i, temp/range);
            temp = 0;
        }
        return linkedList;
    }

}
