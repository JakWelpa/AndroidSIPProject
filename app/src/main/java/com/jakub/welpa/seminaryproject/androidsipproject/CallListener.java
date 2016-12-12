package com.jakub.welpa.seminaryproject.androidsipproject;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.sip.SipAudioCall;
import android.net.sip.SipProfile;

public class CallListener extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        SipAudioCall calling = null;
        try {
            SipAudioCall.Listener listener = new SipAudioCall.Listener() {
                @Override
                public void onRinging(SipAudioCall call, SipProfile caller) {
                    try {
                        call.answerCall(30);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            MainActivity main = (MainActivity) context;

            calling = main.manager.takeAudioCall(intent, listener);
            calling.answerCall(30);
            calling.startAudio();
            calling.setSpeakerMode(true);

            main.updateCallStatus(calling);

        } catch (Exception e) {
            if (calling != null) {
                calling.close();
            }
        }


    }
}
