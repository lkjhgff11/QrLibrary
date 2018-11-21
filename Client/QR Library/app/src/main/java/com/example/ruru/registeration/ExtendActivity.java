package com.example.ruru.registeration;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;
import static com.example.ruru.registeration.R.id.exit;


public class ExtendActivity extends AppCompatActivity {

    private AlertDialog dialog;

    String token, data, campus;
    String ip;
    int port;
    ClientThread clientThread;
    String section;
    int seatindex;
    long exp_time;
    long seating_time;

    private TextView CampusView;
    private TextView SeatingTimeView;
    private TextView ExitTimeView;
    private TextView SeatNumberView1;
    private TextView SeatNumberView2;
    private ImageButton extend;
    private ImageButton exit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_extend );
        CampusView = (TextView) findViewById ( R.id.CampusView );
        SeatNumberView1 = (TextView) findViewById ( R.id.SeatNumberView1 );
        SeatNumberView2 = (TextView) findViewById ( R.id.SeatNumberView2 );
        SeatingTimeView = (TextView) findViewById ( R.id.SeatingTimeView );
        ExitTimeView = (TextView) findViewById ( R.id.ExitTimeView );

        // 연장, 퇴실버튼
        extend = (ImageButton) findViewById ( R.id.extend );
        exit = (ImageButton) findViewById ( R.id.exit );


        Intent it = getIntent ();
        token = it.getStringExtra ( "token" );
        ip = it.getStringExtra ( "ip" );
        port = it.getIntExtra ( "port", 11007 );
        campus = it.getStringExtra ( "campus" );


        section = it.getStringExtra ( "section" );                    //열람실
        seatindex = it.getIntExtra ( "seatindex", 0 );   //좌석번호

        seating_time = it.getLongExtra ( "seating_time", 0 ); // 틱값으로 착석시간 받아옴
        seating_time *= 1000;
        exp_time = it.getLongExtra ( "exp_time", 0 );           // 틱값으로 종료시간 받아옴
        exp_time *= 1000;


        CampusView.setText ( campus );          // 캠퍼스 대연/중앙도서관
        SeatNumberView1.setText ( section );   //열람실
        SeatNumberView2.setText ( " " + seatindex );   //좌석번호

        SimpleDateFormat timeformet = new SimpleDateFormat ( "HH:mm:ss" );

        SeatingTimeView.setText ( " " + timeformet.format ( new Date ( seating_time ) ) );   // 착석시간 출력
        ExitTimeView.setText ( " " + timeformet.format ( new Date ( exp_time ) ) );           // 종료시간출력


        // 연장버튼 클릭시
        extend.setOnClickListener ( new View.OnClickListener () {

            @Override
            public void onClick(View v) {

                // 우선 서버에 연장가능한가 묻기위해 토큰을 보낸다.
                data = "4,3," + token;
                Handler handler1;
                handler1 = new Handler () {                         //  서버로 부터 받은데이터
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void handleMessage(Message msg) {
                        String str = msg.getData ().getString ( "msg" );
                        String code = ClientMethod.checkData ( data, str );
                        if (!code.equals ( "Error" )) {             // 정상적으로 서버로부터 데이터가 왔을때
                            String[] splitString = code.split ( ",", 2 );
                            if (splitString[0].equals ( "100" )) {

                                //QR코드 스캐너 호출
                                IntentIntegrator integrator = new IntentIntegrator ( ExtendActivity.this );
                                integrator.setCaptureActivity ( CustomScannerActivity.class );
                                integrator.initiateScan ();

                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder ( ExtendActivity.this );
                                dialog = builder.setMessage ( "퇴실시간 30분전부터 연장이 가능합니다." )
                                        .setNegativeButton ( "확인", null )
                                        .create ();
                                dialog.show ();
                            }
                        }
                    }
                };
                clientThread = new ClientThread ( handler1, ip, port, data );
                clientThread.start ();
            }

        } );

        // 퇴실버튼 클릭시

        exit.setOnClickListener ( new View.OnClickListener () {
            @Override
            public void onClick(View v) {

                // 우선 서버에 퇴실가능한가 묻기위해 토큰을 보낸다.
                data = "3,4," + token;
                Handler handler1;
                handler1 = new Handler () {                         //  서버로 부터 받은데이터
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void handleMessage(Message msg) {
                        String str = msg.getData ().getString ( "msg" );
                        String code = ClientMethod.checkData ( data, str );

                        try {

                            if (!code.equals ( "Error" )) {             // 정상적으로 서버로부터 데이터가 왔을때
                                String[] splitString = code.split ( ",", 2 );
                                if (splitString[0].equals ( "100" )) {

                                    AlertDialog.Builder builder = new AlertDialog.Builder ( ExtendActivity.this );
                                    dialog = builder.setMessage ( "퇴실되었습니다." )
                                            .setPositiveButton ( "확인", new DialogInterface.OnClickListener () {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Intent intent = new Intent ( ExtendActivity.this, MainActivity.class );
                                                    intent.addFlags ( FLAG_ACTIVITY_REORDER_TO_FRONT );  //
                                                    startActivity ( intent );

                                                }
                                            } )
                                            .create ();
                                    dialog.show ();
                                }
                            }

                        } catch (Exception e) {

                            e.printStackTrace ();
                        }
                    }
                };
                clientThread = new ClientThread ( handler1, ip, port, data );
                clientThread.start ();
            }

        } );


        /*// 착석시간이아니라 현재시간이 종료시간보다 넘으면 Main화면으로 intent

        final Thread mythread = new Thread(new Runnable() {
            @Override
            public void run() {
                Date date = new Date();
                Long current_time = date.getTime();
                while (current_time < exp_time) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }
                    current_time = date.getTime();

                }
                Handler handler = new Handler() {

                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);

                        Intent intent = new Intent(ExtendActivity.this, MainActivity.class);

                        AlertDialog.Builder builder = new AlertDialog.Builder(ExtendActivity.this);
                        dialog = builder.setMessage("퇴실시간이 지나 반납됐습니다.")
                                .setPositiveButton("확인",new DialogInterface.OnClickListener(){
                                    public void onClick(DialogInterface dialog, int which){
                                        Intent intent = new Intent(ExtendActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    }
                                })
                                .create();
                        dialog.show();

                    }
                };
            }
        });
     *//*   mythread.run();
         */
    }

    // qr 찍고나서 연장되는 화면
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            IntentResult scanResult = IntentIntegrator.parseActivityResult ( requestCode, resultCode, intent );
            String QRCode = scanResult.getContents ();
            data = "3,3," + token + "," + QRCode;



            try {
                Thread.sleep ( 1000 );

            } catch (Exception e) {


            }

            Handler handler1;
            handler1 = new Handler () {
                @Override

                public void handleMessage(Message msg) {
                    String str = msg.getData ().getString ( "msg" );
                    String code = ClientMethod.checkData ( data, str );  //str은 서버로부터 3,3,100 이런식으로 받아오는거
                    String[] minji = code.split ( ",", 2 ); // minji[0]은 100, minji[1]은 exp_time
                    if (minji[0].equals ( "100" )) {
                        SimpleDateFormat timeformet = new SimpleDateFormat ( "HH:mm:ss" );

                        long etx = 0;
                        int i = 0;
                        while (minji[1].charAt ( i ) != '.') {
                            etx += minji[1].charAt ( i ) - 48;
                            etx *= 10;
                            i++;
                        }
                        etx *= 100;

                        ExitTimeView.setText ( " " + timeformet.format ( new Date ( etx ) ) );
                        
                        Toast.makeText ( ExtendActivity.this, "연장되었습니다.", Toast.LENGTH_SHORT ).show ();
                    } else if (minji[0].equals ( "201" )) {

                        Toast.makeText ( ExtendActivity.this, "잘못된 qr코드입니다.", Toast.LENGTH_SHORT ).show ();

                    } else if (minji[0].equals ( "203" )) {
                        Toast.makeText ( ExtendActivity.this, "자리가없습니다.", Toast.LENGTH_SHORT ).show ();

                    } else {
                        Toast.makeText ( ExtendActivity.this, "error!!", Toast.LENGTH_SHORT ).show ();
                    }
                }
            };
            clientThread = new ClientThread ( handler1, ip, port, data ); // 서버로전송
            clientThread.start ();
        }
    }

    @Override
    protected void onStop() {
        super.onStop ();
        if (dialog != null) {
            dialog.dismiss ();
            dialog = null;
            finish ();
        }
    }
}