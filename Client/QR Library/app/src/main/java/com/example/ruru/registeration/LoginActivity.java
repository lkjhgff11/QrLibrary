package com.example.ruru.registeration;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;


public class LoginActivity extends AppCompatActivity {

    private AlertDialog dialog;
    String token, data;
    String ip = "192.168.46.62";
    int port =  9876;
    ClientThread clientThread;
    Handler handler;
    int campusIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final String[] data1 = getResources().getStringArray(R.array.library);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, data1);
        final Spinner campus = (Spinner) findViewById(R.id.campus);
        campus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                campusIndex = i;
                Toast.makeText(getApplicationContext(), data1[i], Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        final EditText id = (EditText) findViewById(R.id.id);
        final EditText pw = (EditText) findViewById(R.id.pw);
        final Button loginButton = (Button) findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String userID = id.getText().toString();
                final String userPassword = pw.getText().toString();

                data = "1,1," + data1[campusIndex] + "," + userID + "," + userPassword;   // 내가 보낼 데이터
//                Toast.makeText(getApplicationContext(), data, Toast.LENGTH_SHORT).show();
                handler = new Handler() {                         //  서버로 부터 받은데이터
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void handleMessage(Message msg) {
                        String str = msg.getData().getString("msg");
                        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
                        String code = ClientMethod.checkData(data, str);

                        try {

                            if (!code.equals("Error")) {             // 정상적으로 서버로부터 데이터가 왔을때
                                String[] splitString = code.split(",", 2);
                                if(splitString[0].equals("100")) {
                                    token = splitString[1];
                                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                    dialog = builder.setMessage("로그인")
                                            .setPositiveButton("확인", null)
                                            .create();

                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.putExtra("token", token);
                                    intent.putExtra("ip", ip);
                                    intent.putExtra("port", port);
                                    intent.putExtra("campus", data1[campusIndex]);

                                    LoginActivity.this.startActivity(intent);
                                    dialog.show();
                                    finish();
                                }

                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                dialog = builder.setMessage("ID,PASSWORD가 틀렸습니다.")
                                        .setNegativeButton("다시시도", null)
                                        .create();
                                dialog.show();
                            }

                        } catch (Exception e) {

                            e.printStackTrace();
                        }
                    }
                };
                clientThread = new ClientThread(handler, ip, port, data);
                clientThread.start();
            }

        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
            finish();
        }
    }
}