package com.nikeasoft.powermanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final int ACCESS_REQUEST = 100;

    EditText phone;
    Button sendSMS, updateState;
    CheckBox[] power;

    StringBuilder recvFrom;
    StringBuilder recvMessage;
    boolean permissionState;

    interface CommandManager {
        void AppendCommand(StringBuilder message);
        void ParseCommand(String message);
    }

    CommandManager cmdManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            phone = findViewById(R.id.phoneNumber);
            power = new CheckBox[]{
                    findViewById(R.id.power1),
                    findViewById(R.id.power2),
                    findViewById(R.id.power3),
                    findViewById(R.id.power4)
            };
            updateState = findViewById(R.id.updateState);
            sendSMS = findViewById(R.id.sendSMS);

            updateState.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SendMessage("SN0000CHECK");
                }
            });

            sendSMS.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SendMessage("SN0000COM");
                }
            });

            SMSListener.RegisterCallback(this::OnMsgReceive);

            cmdManager = new CommandManager() {
                @Override
                public void AppendCommand(StringBuilder message)
                {
                    for(CheckBox com : power) {
                        message.append(com.isChecked() ? 'N' : 'F');
                    }
                }

                @Override
                public void ParseCommand(String message) {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();

                    message = message.toLowerCase();

                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();

                    int offset = 0;
                    for(int i = 0; i < power.length; ++i)
                    {
                        String data = "sw" + Integer.toString(i+1) + " is ";
                        if(message.contains(data)) {
                            char symbol = message.charAt(message.indexOf(data) + data.length() + 1);
                            power[i].setChecked(symbol == 'n' ? true : false);
                        }
                    }
                }
            };

            if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECEIVE_SMS)
                    != PackageManager.PERMISSION_GRANTED)
            {
                String[] permissions = new String[]{
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.RECEIVE_SMS
                };

                ActivityCompat.requestPermissions(MainActivity.this, permissions, ACCESS_REQUEST);
            } else {
                permissionState = true;
            }
        } catch (Exception error) {
            Log.e("Exception catch", error.getMessage());
        }
    }

    private void OnMsgReceive(String from, String msg)
    {
        if(!permissionState) {
            Toast.makeText(MainActivity.this,
                    "Permission denied for read sms!", Toast.LENGTH_SHORT).show();
            return;
        }

        from = from.replaceAll("[\\s\\-\\+()]", "");
        while(from.length() > 10) {
            from = from.substring(1);
        }

        String myPhone = phone.getText().toString().replaceAll("[\\s\\-\\+()]", "");
        while(myPhone.length() > 10) {
            myPhone = myPhone.substring(1);
        }

        if(from.compareTo(myPhone) == 0) {
            cmdManager.ParseCommand(msg);
        }
    }

    private void SendMessage(String message)
    {
        if(!permissionState) {
            Toast.makeText(MainActivity.this,
                    "Permission denied for send sms!", Toast.LENGTH_SHORT).show();
            return;
        }

        String sPhone = "+7" + phone.getText().toString().trim();
        sPhone.replaceAll("[\\s\\-()]", "");
        StringBuilder sMessage = new StringBuilder(message);

        if(sPhone.isEmpty()) {
            Toast.makeText(MainActivity.this,
                    "Phone number field is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        if(sPhone.length() != 12) {
            Toast.makeText(MainActivity.this,
                    "Phone number is incorrect!", Toast.LENGTH_SHORT).show();
            return;
        }

        if(sMessage.toString().contains("SN0000COM")) {
            cmdManager.AppendCommand(sMessage);
        }

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(sPhone, null, sMessage.toString(),
                null, null);

        Toast.makeText(MainActivity.this,
                "Command: " + sMessage.toString() + " send success!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            if(requestCode == ACCESS_REQUEST) {
                permissionState = true;
                Toast.makeText(MainActivity.this, "Permission success!",
                        Toast.LENGTH_SHORT).show();
            } else {
                permissionState = false;
                Toast.makeText(MainActivity.this, "Permission denied!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}

