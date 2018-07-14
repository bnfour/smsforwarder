package net.bnfour.smsforwarder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SMSForwarder extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            // should it work?
            SharedPreferences preferences
                    = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

            String number = preferences.getString("phone", "");
            if (number.equals("")) {
                return;
            }

            boolean enabled = preferences.getBoolean("enabled", false);
            if (!enabled) {
                return;
            }

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

                    boolean filterEnabled = preferences.getBoolean("filter", false);

                    if (filterEnabled) {
                        String filterType = preferences.getString("list_type", "0");

                        String[] entriesAsArray = preferences.getString("filter_list", "").split(";");

                        // "0" is blacklist
                        if (filterType.equals("0")) {
                            for (String filter : entriesAsArray) {
                                if (sender.equals(filter) || PhoneNumberUtils.compare(sender, filter)) {
                                    return;
                                }
                            }
                        }
                        // "1" is whitelist
                        else {
                            boolean found = false;
                            for (String filter : entriesAsArray) {
                                if (sender.equals(filter) || PhoneNumberUtils.compare(sender, filter)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                return;
                            }
                        }
                    }

                    String toSend = sender + ": " + message;
                    ArrayList<String> dividedMessage = SmsManager.getDefault().divideMessage(toSend);

                    SmsManager.getDefault()
                            .sendMultipartTextMessage(number, null, dividedMessage, null, null);
                }
            }
        }
    }
}
