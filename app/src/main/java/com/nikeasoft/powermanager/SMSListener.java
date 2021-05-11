package com.nikeasoft.powermanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSListener extends BroadcastReceiver {
    interface Callback {
        void OnMsgReceive(String from, String msg);
    }

    static Callback callback;

    static public void RegisterCallback(Callback callback) {
        SMSListener.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED"))
            {
                Bundle bundle = intent.getExtras();
                String format = bundle.getString("format");

                Object[] pdus = (Object[])bundle.get("pdus");

                if(pdus == null) {
                    return;
                }

                SmsMessage[] message = new SmsMessage[pdus.length];
                for(int i = 0; i < message.length; ++i) {
                    message[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    String from = message[i].getOriginatingAddress();
                    String msg = message[i].getMessageBody();
                    callback.OnMsgReceive(from, msg);
                }
            }
        } catch (Exception error) {
            Log.e("ERROR", error.getMessage());
        }
    }


}
