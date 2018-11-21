package com.example.ruru.registeration;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    String token, data;
    String ip;
    int port;
    Handler handler;
    ClientThread clientThread;
    String section, campus;
    int seatindex;
    TextView titleText;
    ImageView btn[][] = new ImageView[3][6];
    ProgressBar seatPg[][] = new ProgressBar[3][6];
    TextView seatNumber[][] = new TextView[3][6];
    TextView seatState_taked[][] = new TextView[3][6];
    TextView seatState_empty[][] = new TextView[3][6];
    ImageButton tab[] = new ImageButton[3];

    //위젯에 접근하기 위한 객체 생성
    void initId() {
        titleText = findViewById(R.id.titleText);

        btn[0][0] = findViewById(R.id.seatImage1);
        btn[0][1] = findViewById(R.id.seatImage2);
        btn[0][2] = findViewById(R.id.seatImage3);
        btn[0][3] = findViewById(R.id.seatImage4);
        btn[0][4] = findViewById(R.id.seatImage5);
        btn[0][5] = findViewById(R.id.seatImage6);
        btn[1][0] = findViewById(R.id.seatImage7);
        btn[1][1] = findViewById(R.id.seatImage8);
        btn[1][2] = findViewById(R.id.seatImage9);
        btn[1][3] = findViewById(R.id.seatImage10);
        btn[1][4] = findViewById(R.id.seatImage11);
        btn[1][5] = findViewById(R.id.seatImage12);
        btn[2][0] = findViewById(R.id.seatImage13);
        btn[2][1] = findViewById(R.id.seatImage14);
        btn[2][2] = findViewById(R.id.seatImage15);
        btn[2][3] = findViewById(R.id.seatImage16);
        btn[2][4] = findViewById(R.id.seatImage17);
        btn[2][5] = findViewById(R.id.seatImage18);

        seatPg[0][0] = findViewById(R.id.seatPg1);
        seatPg[0][1] = findViewById(R.id.seatPg2);
        seatPg[0][2] = findViewById(R.id.seatPg3);
        seatPg[0][3] = findViewById(R.id.seatPg4);
        seatPg[0][4] = findViewById(R.id.seatPg5);
        seatPg[0][5] = findViewById(R.id.seatPg6);
        seatPg[1][0] = findViewById(R.id.seatPg7);
        seatPg[1][1] = findViewById(R.id.seatPg8);
        seatPg[1][2] = findViewById(R.id.seatPg9);
        seatPg[1][3] = findViewById(R.id.seatPg10);
        seatPg[1][4] = findViewById(R.id.seatPg11);
        seatPg[1][5] = findViewById(R.id.seatPg12);
        seatPg[2][0] = findViewById(R.id.seatPg13);
        seatPg[2][1] = findViewById(R.id.seatPg14);
        seatPg[2][2] = findViewById(R.id.seatPg15);
        seatPg[2][3] = findViewById(R.id.seatPg16);
        seatPg[2][4] = findViewById(R.id.seatPg17);
        seatPg[2][5] = findViewById(R.id.seatPg18);

        seatNumber[0][0] = findViewById(R.id.seatNumber1);
        seatNumber[0][1] = findViewById(R.id.seatNumber2);
        seatNumber[0][2] = findViewById(R.id.seatNumber3);
        seatNumber[0][3] = findViewById(R.id.seatNumber4);
        seatNumber[0][4] = findViewById(R.id.seatNumber5);
        seatNumber[0][5] = findViewById(R.id.seatNumber6);
        seatNumber[1][0] = findViewById(R.id.seatNumber7);
        seatNumber[1][1] = findViewById(R.id.seatNumber8);
        seatNumber[1][2] = findViewById(R.id.seatNumber9);
        seatNumber[1][3] = findViewById(R.id.seatNumber10);
        seatNumber[1][4] = findViewById(R.id.seatNumber11);
        seatNumber[1][5] = findViewById(R.id.seatNumber12);
        seatNumber[2][0] = findViewById(R.id.seatNumber13);
        seatNumber[2][1] = findViewById(R.id.seatNumber14);
        seatNumber[2][2] = findViewById(R.id.seatNumber15);
        seatNumber[2][3] = findViewById(R.id.seatNumber16);
        seatNumber[2][4] = findViewById(R.id.seatNumber17);
        seatNumber[2][5] = findViewById(R.id.seatNumber18);

        seatState_empty[0][0] = findViewById(R.id.seatState1_empty);
        seatState_empty[0][1] = findViewById(R.id.seatState2_empty);
        seatState_empty[0][2] = findViewById(R.id.seatState3_empty);
        seatState_empty[0][3] = findViewById(R.id.seatState4_empty);
        seatState_empty[0][4] = findViewById(R.id.seatState5_empty);
        seatState_empty[0][5] = findViewById(R.id.seatState6_empty);
        seatState_empty[1][0] = findViewById(R.id.seatState7_empty);
        seatState_empty[1][1] = findViewById(R.id.seatState8_empty);
        seatState_empty[1][2] = findViewById(R.id.seatState9_empty);
        seatState_empty[1][3] = findViewById(R.id.seatState10_empty);
        seatState_empty[1][4] = findViewById(R.id.seatState11_empty);
        seatState_empty[1][5] = findViewById(R.id.seatState12_empty);
        seatState_empty[2][0] = findViewById(R.id.seatState13_empty);
        seatState_empty[2][1] = findViewById(R.id.seatState14_empty);
        seatState_empty[2][2] = findViewById(R.id.seatState15_empty);
        seatState_empty[2][3] = findViewById(R.id.seatState16_empty);
        seatState_empty[2][4] = findViewById(R.id.seatState17_empty);
        seatState_empty[2][5] = findViewById(R.id.seatState18_empty);

        seatState_taked[0][0] = findViewById(R.id.seatState1_taked);
        seatState_taked[0][1] = findViewById(R.id.seatState2_taked);
        seatState_taked[0][2] = findViewById(R.id.seatState3_taked);
        seatState_taked[0][3] = findViewById(R.id.seatState4_taked);
        seatState_taked[0][4] = findViewById(R.id.seatState5_taked);
        seatState_taked[0][5] = findViewById(R.id.seatState6_taked);
        seatState_taked[1][0] = findViewById(R.id.seatState7_taked);
        seatState_taked[1][1] = findViewById(R.id.seatState8_taked);
        seatState_taked[1][2] = findViewById(R.id.seatState9_taked);
        seatState_taked[1][3] = findViewById(R.id.seatState10_taked);
        seatState_taked[1][4] = findViewById(R.id.seatState11_taked);
        seatState_taked[1][5] = findViewById(R.id.seatState12_taked);
        seatState_taked[2][0] = findViewById(R.id.seatState13_taked);
        seatState_taked[2][1] = findViewById(R.id.seatState14_taked);
        seatState_taked[2][2] = findViewById(R.id.seatState15_taked);
        seatState_taked[2][3] = findViewById(R.id.seatState16_taked);
        seatState_taked[2][4] = findViewById(R.id.seatState17_taked);
        seatState_taked[2][5] = findViewById(R.id.seatState18_taked);

        tab[0] = findViewById(R.id.tab1);
        tab[1] = findViewById(R.id.tab2);
        tab[2] = findViewById(R.id.tab3);

        //탭 이벤트
        for (int i = 0; i < 3; i++) {
            final int index = i + 1;
            tab[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeView(index);
                }
            });
        }
    }

    //좌석 조회
    @RequiresApi(api = Build.VERSION_CODES.O)
    void refresh(String jsonstr) {
        long seating_time, exp_time;
        try {
            //JSON으로부터 좌석에 대한 정보 도출
            JSONObject jsonobject = new JSONObject(jsonstr);
            int i = 0;
            while (true) {
                JSONArray seatsarray = null;
                if (i == 0) {
                    seatsarray = (JSONArray) jsonobject.get("제1열람실");
                } else if (i == 1) {
                    seatsarray = (JSONArray) jsonobject.get("제2열람실");
                } else if (i == 2) {
                    seatsarray = (JSONArray) jsonobject.get("노트북실");
                } else if (i == 3) break;
                for (int j = 0; j < seatsarray.length(); j++) {
                    JSONObject seatobject = (JSONObject) seatsarray.get(j);

                    //대출 중인 좌석에 대한 처리
                    if (seatobject.get("state").equals("taken")) {
                        //ProgressBar 처리
                        seating_time = seatobject.getLong("seating_time");
                        exp_time = seatobject.getLong("exp_time");
                        seating_time *= 1000;
                        exp_time *= 1000;

                        seatPg[i][j].setMin(0);
                        seatPg[i][j].setMax((int)(exp_time - seating_time));
                        seatPg[i][j].setProgress((int)(Calendar.getInstance().getTimeInMillis() - seating_time));

                        //버튼 이미지, 대여 상태 텍스트, ProgressBar 처리
                        btn[i][j].setImageResource(R.drawable.r_seat_taked);
                        seatState_empty[i][j].setVisibility(View.INVISIBLE);
                        seatState_taked[i][j].setVisibility(View.VISIBLE);
                        seatPg[i][j].setVisibility(View.VISIBLE);

                        //대출 불가능 좌석에 대한 좌석 정보 이벤트 처리
                        final String str = (String) seatNumber[i][j].getText();
                        final Long sttv = seating_time;
                        final Long ettv = exp_time;
                        btn[i][j].setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
                                View dialogView = View.inflate(MainActivity.this, R.layout.dialog1, null);
                                TextView seating_timeTextView = dialogView.findViewById(R.id.tv1);
                                TextView exp_timeTextView = dialogView.findViewById(R.id.tv2);
                                SimpleDateFormat timeformet = new SimpleDateFormat("HH:mm:ss");

                                dlg.setTitle(str + "번");
                                seating_timeTextView.setText("입실 시간 : " + timeformet.format(new Date(sttv)));
                                exp_timeTextView.setText("퇴실 시간 : " + timeformet.format(new Date(ettv)));
                                dlg.setView(dialogView);
                                dlg.setPositiveButton("확인", null);
                                dlg.show();
                            }
                        });

                        //빈 좌석에 대한 처리
                    } else {
                        //버튼 이미지, 대여 상태 텍스트, ProgressBar 처리
                        btn[i][j].setImageResource(R.drawable.r_seat_empty);
                        seatState_empty[i][j].setVisibility(View.VISIBLE);
                        seatState_taked[i][j].setVisibility(View.INVISIBLE);
                        seatPg[i][j].setVisibility(View.INVISIBLE);

                        //빈 좌석에 대한 이벤트 처리
                        final TextView tv = seatNumber[i][j];
                        btn[i][j].setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                //선택된 좌석에 대한 seatindex 도출
                                seatindex = (Integer.parseInt((String) tv.getText()) % 6);
                                if (seatindex == 0) seatindex = 6;

                                //QR코드 스캐너 호출
                                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                                integrator.setCaptureActivity(CustomScannerActivity.class);
                                integrator.initiateScan();
                            }
                        });
                    }
                }
                i++;
            }
        } catch (JSONException e) {
        }
    }

    //내 좌석 정보
    void myInfo(String receive) {
        String[] code = receive.split(",", 4);

        //좌석을 정상 대출 중인 경우
        if (code[0].equals("100")) {
            Intent intent = new Intent(MainActivity.this, ExtendActivity.class);
            intent.putExtra("token", token);
            intent.putExtra("ip", ip);
            intent.putExtra("port", port);
            intent.putExtra("section", section);
            intent.putExtra("seatindex", seatindex);
            intent.putExtra("campus", campus);
            try {
                JSONObject jsonObject = new JSONObject(code[3]);
                intent.putExtra("seating_time", jsonObject.getLong("seating_time"));
                intent.putExtra("exp_time", jsonObject.getLong("exp_time"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            this.startActivity(intent);
            //대출 중인 좌석이 없을 경우
        } else if (code[0].equals("203")) {
            Toast.makeText(MainActivity.this, "대출 중인 좌석이 없습니다.", Toast.LENGTH_SHORT).show();
            //그 외
        } else Toast.makeText(MainActivity.this, "소켓 에러", Toast.LENGTH_SHORT).show();
    }

    //QR코드 확인
    void isRightQR(String receive) {
        String[] code = receive.split(",", 2);

        //좌석 정보와 QR코드가 일치할 경우
        if (code[0].equals("100")) {
            data = "3,5," + token;
            handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    String str = msg.getData().getString("msg");
                    String code = ClientMethod.checkData(data, str);
                    if (!code.equals("Error")) {
                        myInfo(code);
                    }
                }
            };
            clientThread = new ClientThread(handler, ip, port, data);
            clientThread.start();

            //불일치
        } else if (code[0].equals("201")) {
            Toast.makeText(MainActivity.this, "좌석과 QR코드가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            //대출 중인 좌석의 조회
        } else if (code[0].equals("202")) {
            Toast.makeText(MainActivity.this, "이미 대출 중인 좌석입니다.", Toast.LENGTH_SHORT).show();
            //그 외
        } else Toast.makeText(MainActivity.this, "소켓 에러", Toast.LENGTH_SHORT).show();
    }

    //툴바 메뉴 생성
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //메뉴 아이템이 선택될 때
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //좌석 정보 요청
            case R.id.refresh:
                data = "2,1," + token;
                handler = new Handler() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void handleMessage(Message msg) {
                        String str = msg.getData().getString("msg");
                        String code = ClientMethod.checkData(data, str);
                        if (!code.equals("Error")) {
                            refresh(code);
                            Toast.makeText(MainActivity.this, "새로고침 완료", Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                clientThread = new ClientThread(handler, ip, port, data);
                clientThread.start();
                return true;
            case R.id.myInfo:
                data = "3,5," + token;
                handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        String str = msg.getData().getString("msg");
                        String code = ClientMethod.checkData(data, str);
                        if (!code.equals("Error")) {
                            myInfo(code);
                        }
                    }
                };
                clientThread = new ClientThread(handler, ip, port, data);
                clientThread.start();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //탭 선택에 의한 뷰 변경
    void changeView(int index) {
        RelativeLayout section1 = findViewById(R.id.section1);
        RelativeLayout section2 = findViewById(R.id.section2);
        RelativeLayout section3 = findViewById(R.id.section3);

        switch (index) {
            case 1:
                section1.setVisibility(View.VISIBLE);
                section2.setVisibility(View.INVISIBLE);
                section3.setVisibility(View.INVISIBLE);
                tab[0].setImageResource(R.drawable.r_tab_on_1);
                tab[1].setImageResource(R.drawable.r_tab_off_1);
                tab[2].setImageResource(R.drawable.r_tab_off_1);
                section = "제1열람실";
                break;
            case 2:
                section1.setVisibility(View.INVISIBLE);
                section2.setVisibility(View.VISIBLE);
                section3.setVisibility(View.INVISIBLE);
                tab[0].setImageResource(R.drawable.r_tab_off_1);
                tab[1].setImageResource(R.drawable.r_tab_on_1);
                tab[2].setImageResource(R.drawable.r_tab_off_1);
                section = "제2열람실";
                break;
            case 3:
                section1.setVisibility(View.INVISIBLE);
                section2.setVisibility(View.INVISIBLE);
                section3.setVisibility(View.VISIBLE);
                tab[0].setImageResource(R.drawable.r_tab_off_1);
                tab[1].setImageResource(R.drawable.r_tab_off_1);
                tab[2].setImageResource(R.drawable.r_tab_on_1);
                section = "노트북실";
                break;
        }
    }

    //QR코드 결과
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            String QRCode = scanResult.getContents();
            data = "3,2," + token + "," + section + "," + seatindex + "," + QRCode;

            handler = new Handler() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void handleMessage(Message msg) {
                    String str = msg.getData().getString("msg");
                    String code = ClientMethod.checkData(data, str);
                    if (!code.equals("Error")) {
                        isRightQR(code);
                    }
                }
            };
            clientThread = new ClientThread(handler, ip, port, data);
            clientThread.start();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //툴바 생성
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

        LayoutInflater inflater = getLayoutInflater();
        FrameLayout frame = findViewById(R.id.frame);
        View view = inflater.inflate(R.layout.section1, frame, false);
        frame.addView(view);
        view = inflater.inflate(R.layout.section2, frame, false);
        frame.addView(view);
        view = inflater.inflate(R.layout.section3, frame, false);
        frame.addView(view);

        //초기화
        initId();

        //객체 생성
        Intent it = getIntent();
        token = it.getStringExtra("token");
        ip = it.getStringExtra("ip");
        port = it.getIntExtra("port", 11007);
        campus = it.getStringExtra("campus");

        titleText.setText(campus);

        changeView(1);

        //초기 새로고침
        data = "2,1," + token;
        handler = new Handler() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void handleMessage(Message msg) {
                String str = msg.getData().getString("msg");
                String code = ClientMethod.checkData(data, str);
                if (!code.equals("Error")) {
                    refresh(code);
                }
            }
        };
        clientThread = new ClientThread(handler, ip, port, data);
        clientThread.start();
    }
}