package com.nlog2n.mukey;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Button;
import android.text.method.ScrollingMovementMethod;

public class MainActivity extends Activity {
    static {
        System.loadLibrary("mukey");
        //System.loadLibrary("rootcheck");
    }


    // 之前: target java method to be xposed: returnPwd
    public String getMuDeviceID(String input)
    {
        String status = "";
        MuDeviceID mMuDeviceID = new MuDeviceID(this.getApplicationContext());
        status +=  mMuDeviceID.getAndroidDeviceId();
        return status;
    }

    public String getMuThreats(String input)
    {
        String status = "";
        MuThreats  mMuThreats  = new MuThreats(this, this.getApplicationContext());
        status += mMuThreats.getStatus();
        return status;
    }

    public String getMuService(String input)
    {
        String status = "";

        // call MuServices to get OTP
        MuServices mMuServices = new MuServices(this, this.getApplicationContext());
        int otp = mMuServices.GenOTP();
        status += "One Time Password=" + otp;

        return status;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 设置按钮响应函数
        Button button1 = (Button)findViewById(R.id.button);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView1 = (TextView)findViewById(R.id.textView);
                textView1.setMovementMethod(new ScrollingMovementMethod());

                String myText = "";
                myText += getMuDeviceID("root checker -by nlog2n\n");

                textView1.setText(myText);
            }

        });

        Button button2 = (Button)findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView1 = (TextView)findViewById(R.id.textView);
                textView1.setMovementMethod(new ScrollingMovementMethod());

                String myText = "";
                myText += getMuThreats("root checker -by nlog2n\n");

                textView1.setText(myText);
            }

        });

        Button button3 = (Button)findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView1 = (TextView)findViewById(R.id.textView);
                textView1.setMovementMethod(new ScrollingMovementMethod());

                String myText = "";
                myText += getMuService("root checker -by nlog2n\n");

                textView1.setText(myText);
            }

        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
