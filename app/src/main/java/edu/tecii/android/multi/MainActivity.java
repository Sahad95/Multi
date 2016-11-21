package edu.tecii.android.multi;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Random;


public class MainActivity extends Activity {

    ProgressBar bar1;
    ProgressBar bar2;
    TextView msgWorking;
    TextView msgReturned;
    ScrollView myScrollView;

    // Esta es una variable de control usada por el hilo de fondo
    protected boolean isRunning = false;
    // Tiempo de vida (en segundos) por el hilo del fondo
    protected final int MAX_SEC = 30;
    // Valor global visto por todos los hilos - agregado sincronizado get / set
    protected int globalIntTest = 0;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String returnedValue = (String)msg.obj;
//Aqui se hace algo con el valor enviado por el hilo de fondo
            msgReturned.append("\n returned value: " + returnedValue );
            myScrollView.fullScroll(View.FOCUS_DOWN);
            bar1.incrementProgressBy(1);
//Prueba de terminación anticipada
            if (bar1.getProgress() == MAX_SEC){
                msgReturned.append(" \nDone \n back thread has been stopped");
                isRunning = false;
            }
            if (bar1.getProgress() == bar1.getMax()){
                msgWorking.setText("Done");
                bar1.setVisibility(View.INVISIBLE);
                bar2.setVisibility(View.INVISIBLE);
            }
            else {
                msgWorking.setText("Working..." + bar1.getProgress() );
            }
        }
    }; //Controlador
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_main);
        bar1 = (ProgressBar) findViewById(R.id.progress1);
        bar1.setProgress(0);
        bar1.setMax(MAX_SEC);
        bar2 = (ProgressBar) findViewById(R.id.progress2);
        msgWorking = (TextView)findViewById(R.id.txtWorkProgress);
        msgReturned = (TextView)findViewById(R.id.txtReturnedValues);
        myScrollView = (ScrollView)findViewById(R.id.myscroller);
// poner la variable global (Accer por hilo (s) de fondo )
        globalIntTest = 1;
    }//onCreate
    public void onStart() {
        super.onStart();
// Este código crea la actividad de fondo donde se realiza el trabajo ocupado
        Thread background = new Thread(new Runnable() {
            public void run() {
                try {
                    for (int i = 0; i < MAX_SEC && isRunning; i++) {
// Prueba un método Toast aquí (no funcionará!)
// trabajo falso ocupado ocupado aquí
                        Thread.sleep(1000); // 1000 msec.
// Este es un valor generado localmente entre 0-100
                        Random rnd = new Random();
                        int localData = (int) rnd.nextInt(101);
// Podemos ver y cambiar variables de clase (globales) (inseguro!)
// utiliza para sincronizar get-set acesando al  MONITOR
                        String data = "Data-" + getGlobalIntTest() + "-" + localData;
                        increaseGlobalIntTest(1);
//Solicitar un token de mensaje y poner algunos datos en él
                        Message msg = handler.obtainMessage(1, (String)data);
// Si este hilo sigue vivo, envíe el mensaje
                        if (isRunning) {
                            handler.sendMessage(msg);
                        }
                    }
                }
                catch (Throwable t) {
// Acaba de terminar el hilo de fondo
                    isRunning = false;
                }
            }
        });// Hilo
        isRunning = true;
        background.start();
    }//onStart
    public void onStop() {
        super.onStop();
        isRunning = false;
    }//onStop
    // Acceso seguro a la var global (no se necesita here-only un backthread!)
    public synchronized int getGlobalIntTest() {
        return globalIntTest;
    }
    public synchronized int increaseGlobalIntTest(int inc) {
        return globalIntTest += inc;
    }
}//clase


