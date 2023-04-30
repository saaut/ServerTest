package com.example.servertest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button connect_btn;                 // ip 받아오는 버튼

    EditText ip_edit;               // ip 에디트
    TextView show_text;             // 서버에서온거 보여주는 에디트
    // 소켓통신에 필요한것
    private Handler mHandler;//TCP Connection을 진행하는 Thread를 변경할 핸들러

    private Socket socket;

    private BufferedReader networkReader;
    private PrintWriter networkWriter;

    private DataOutputStream dos;
    private DataInputStream dis;

    private String ip = "192.168.3.39";            // IP 번호
    private int port = 9999;                          // port 번호
    private Bitmap img;
    private Bitmap rotatedBitmap = null;
    private String img_path;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connect_btn = (Button)findViewById(R.id.connect_btn);
        connect_btn.setOnClickListener(this);

        ip_edit = (EditText)findViewById(R.id.ip_edit);
        show_text = (TextView)findViewById(R.id.show_text);

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.connect_btn:     // ip 받아오는 버튼
                connect();
        }
    }

    // 로그인 정보 db에 넣어주고 연결시켜야 함.
    void connect(){
        mHandler = new Handler();
        Log.w("connect","연결 하는중");
        // 받아오는거
        Thread sendImg_getText = new Thread() {
            public void run() {
                ByteArrayOutputStream byteArray = new ByteArrayOutputStream();//이미지 전달을 위한 바이트 배열 변수
                rotatedBitmap.compress(bitmap.CompressFormat.PNG, 100,byteArray);//안드로이드에서 촬영된 알약 이미지를 bitmap 형태로 담고 있다.
                byte[] bytes = byteArray.toByteArray();//bitmap을 소켓통신으로 전송 가능하도록 bytearray 형태로 전환한다.

                // 서버 접속
                try {
                    socket = new Socket(ip, port);
                    Log.w("서버 접속됨", "서버 접속됨");
                } catch (IOException e1) {
                    Log.w("서버접속못함", "서버접속못함");
                    e1.printStackTrace();
                }

                Log.w("edit 넘어가야 할 값 : ","안드로이드에서 서버로 연결요청");

                // Buffered가 잘못된듯.
                try {
                    dos = new DataOutputStream(socket.getOutputStream());   // data를 주고받기 위한 stream. output에 보낼꺼 넣음
                    dis = new DataInputStream(socket.getInputStream());     // input에 받을꺼 넣어짐
                    dos.writeUTF("안드로이드에서 서버로 연결요청");

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.w("버퍼", "버퍼생성 잘못됨");
                }
                Log.w("버퍼","버퍼생성 잘됨");
                //서버에 보내보자
                try{
                    dos.writeUTF(Integer.toString(bytes.length));//길이부터 보냄. integer를 string으로 전송.
                    dos.flush();//버퍼를 바로 비워주자.

                    dos.write(bytes);//bytearray전송
                    dos.flush();

                    img_path = readString(dis);
                    //원하는 정보 얻어오기
                    show_text.setText("");
                    show_text.append(img_path);
                    socket.close();
                }
                catch (Exception e){
                    Log.w("error","erroor occur");
                }
            }
        };
        // 소켓 접속 시도, 버퍼생성
        sendImg_getText.start();
        try{
            sendImg_getText.join();
        }catch(InterruptedException e){

        }
    }
    public String readString(DataInputStream dis) throws IOException{
        int length = dis.readInt();//문자열의 길이를 받는다.
        byte[] data=new byte[length];//해당 길이의 byteArray를 생성한다.
        dis.readFully(data,0,length);//해당 길이의 byteArray를 받는다.
        String text = new String(data, StandardCharsets.UTF_8);//디코딩 필수
        return text;
    }
}