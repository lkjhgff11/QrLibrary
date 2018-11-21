package com.example.ruru.registeration;

public class ClientMethod {
    //수신한 data가 옳은 data인지 확인.
    //옳은 data일 경우 100 or 100,data값 반환, 아닐 경우 Error 반환
    public static String checkData (String sendData, String receiveData) {

        String[] receiveCode = receiveData.split(",", 4);
        String[] sendCode = sendData.split(",", 4);

        if(receiveCode[0].equals(sendCode[0]) && receiveCode[1].equals(sendCode[1])) {
            String[] resultCode = receiveData.split(",", 3);
            return resultCode[2];
        }
        else return "Error";
    }

}
