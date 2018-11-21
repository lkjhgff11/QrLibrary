package com.example.ruru.registeration;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class ClientThread extends Thread {
    static private Socket socket;
    private String ip;
    private int port;
    private String data;
    private Handler handler;

    ClientThread(Handler handler, String ip, int port, String data) {
        this.handler = handler;
        this.ip = ip;
        this.port = port;
        this.data = data;
    }

    @Override
    public void run() {
        try {
            //서버와의 동기를 맞추기 위한 tick
            socket = new Socket(ip, port);
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeUTF(data);
            Log.w("send_Data", data);
            outputStream.flush();
            receiveThread receiveThread = new receiveThread(handler);
            receiveThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class receiveThread extends Thread {
        private Handler handler;
        private DataInputStream inputStream;

        receiveThread(Handler handler) {
            this.handler = handler;
            try {
                inputStream = new DataInputStream(socket.getInputStream());
            } catch (Exception e) {
            }
        }

        @Override
        public void run() {
            if (socket.isConnected()) {
                try {
                    byte[] b = new byte[2048];
                    int ac = inputStream.read(b, 0, b.length);
                    Log.w("ac", Integer.toString(ac));
                    String receiveData = new String(b, 0, ac,  "UTF-8");
                    Log.w("receive_Data", receiveData);

                    Message msg = handler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putString("msg", receiveData);
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                    socket.close();
                } catch (Exception e) {
                }
            }
        }
    }
}