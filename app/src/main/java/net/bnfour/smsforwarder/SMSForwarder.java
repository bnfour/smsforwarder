package net.bnfour.smsforwarder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

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

            for (SmsMessage sms: Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                String message = sms.getMessageBody();
                String sender = sms.getOriginatingAddress();

                boolean filterEnabled = preferences.getBoolean("filter", false);

                if (filterEnabled) {
                    String filterType = preferences.getString("list_type", "0");

                    String[] entriesAsArray = preferences.getString("filter_list", "").split(";");

                    // "0" is blacklist
                    if (filterType.equals("0")) {
                        for (String filter: entriesAsArray) {
                            if (sender.equals(filter) || PhoneNumberUtils.compare(sender, filter)) {
                                return;
                            }
                        }
                    }
                    // "1" is whitelist
                    else {
                        boolean found = false;
                        for (String filter: entriesAsArray) {
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
                SmsManager.getDefault().sendTextMessage(number, null, toSend, null, null);
            }
        }
    }
}
