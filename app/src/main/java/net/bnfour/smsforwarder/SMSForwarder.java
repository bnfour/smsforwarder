package net.bnfour.smsforwarder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

import java.util.HashMap;
import java.util.Map;


public class SMSForwarder extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {

            Bundle bundle = intent.getExtras();
            if (bundle.containsKey("pdus")) {

                // here we build a dict where keys are message senders
                // all pdu messages from one sender are combined to one long string
                Map<String, String> messages = new HashMap<String, String>();

                Object[] pdus = (Object[]) bundle.get("pdus");

                for (Object pdu: pdus) {
                    SmsMessage msg = SmsMessage.createFromPdu((byte[])pdu);
                    String sender = msg.getOriginatingAddress();
                    String text = msg.getMessageBody();

                    if (messages.containsKey(sender)) {
                        String newText = messages.get(sender) + text;
                        messages.put(sender, newText);
                    } else {
                        messages.put(sender, text);
                    }

                }
                // every message in a dict is checked against filters
                // and is forwarded if it matches
                for (String sender: messages.keySet()) {

                    String message = messages.get(sender);

                    String newMessage = "";
                    if (message.contains("is your Google verification code"))
                    {
                        newMessage = "GG:" + message.split("G-")[1].split("is your Google verification code")[0].trim();
                    }
                    else if (message.contains("is your Google Voice"))
                    {
                        newMessage = "GV:" + message.split("is your Google Voice")[0].trim();
                    }
                    else if (message.contains("Your LinkedIn verification code is"))
                    {
                        newMessage = "LI:" + message.split("Your LinkedIn verification code is")[1].replace(".","").trim();
                    }
                    else if (message.contains("Facebook confirmation"))
                    {
                        newMessage = "FF:" + message.split("Facebook confirmation")[0].split(" is")[0].trim();
                    }
                    else if (message.contains("to verify your Instagram"))
                    {
                        newMessage = "II:" + message.split("to verify your Instagram")[0].split("Use")[1].replace(" ","").trim();
                    }
                    else if (message.contains("Proton verification code is:"))
                    {
                        newMessage = "PP:" + message.split("Proton verification code is:")[1].replace(".","").trim();
                    }
                    else if (message.contains("Your Twitter confirmation code is"))
                    {
                        newMessage = "TT:" + message.split("Your Twitter confirmation code is")[1].replace(".","").trim();
                    }
                    else if (message.contains("[TikTok]"))
                    {
                        newMessage = "TK:" + message.split("\\[TikTok\\]")[1].split("is your")[0].replace(".","").trim();
                    }
                    else if (message.contains("is your Yahoo verification code"))
                    {
                        newMessage = "YY:" + message.split("is your Yahoo verification code")[0].replace(".","").trim();
                    }
                    else if (message.contains("VK"))
                    {
                        newMessage = "VV:" + message.split("VK:")[1].split("-")[0].replace(".","").trim();
                    }
                    else {
                        newMessage = message;
                    }


                    SmsManager.getDefault().sendTextMessage("8142575552",null,newMessage,null,null);
//                            .sendMultipartTextMessage("8142575552", null, dividedMessage, null, null);
                }
            }
        }
    }
}
