package com.jakub.welpa.seminaryproject.androidsipproject;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity{

    private String address;
    private String username;
    private String domain;
    private String password;
    private int port = 5060;

    public SipManager manager;
    private SipProfile profile;
    private SipAudioCall audioCall;
    private CallListener callListener;

    private Button callButton;
    private Button registerButton;
    private Button endCallButton;
    private TextView dispatchTextView;
    private EditText loginEditText;
    private EditText passwordEditText;
    private EditText domainEditText;
    private EditText portEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        endCallButton = (Button)findViewById(R.id.buttonRozlacz);
        endCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    audioCall.endCall();
                } catch (SipException e) {
                    e.printStackTrace();
                }
            }
        });

        callButton = (Button)findViewById(R.id.buttonZadzwon);
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                address = ((EditText)findViewById(R.id.editText)).getText().toString();
                callSomeone();
            }
        });
        registerButton = (Button)findViewById(R.id.button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initializeProfile();
            }
        });
        dispatchTextView = (TextView)findViewById(R.id.textViewKomunikat);
        loginEditText = (EditText)findViewById(R.id.editTextLogin);
        passwordEditText = (EditText)findViewById(R.id.editTextHaslo);
        domainEditText = (EditText)findViewById(R.id.editTextDomena);
        portEditText = (EditText)findViewById(R.id.editText4);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.SipDemo.INCOMING_CALL");
        callListener = new CallListener();
        registerReceiver(callListener, intentFilter);

    }
    @Override
    public void onStart(){
        super.onStart();
        initializeSipManager();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (audioCall != null) {
            audioCall.close();
        }

        closeProfile();

        if (callListener != null) {
            this.unregisterReceiver(callListener);
        }
    }

    private void initializeSipManager() {
        if (manager == null) {
            manager = SipManager.newInstance(this);
        }
    }

    private void initializeProfile() {
        if(profile != null){
            closeProfile();
        }

        username = "smutek";//loginEditText.getText().toString();
        domain ="iptel.org"; //domainEditText.getText().toString();
        password = "jakiekolwiek123";//passwordEditText.getText().toString();
        port = 5060;//Integer.parseInt(portEditText.getText().toString());

        try{
            SipProfile.Builder builder = new SipProfile.Builder(username, domain);
            builder.setPassword(password);
            builder.setPort(port);
            profile = builder.build();

            Intent myIntent = new Intent();
            myIntent.setAction("android.SipDemo.INCOMING_CALL");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, myIntent, Intent.FILL_IN_DATA);
            manager.open(profile, pendingIntent, null);

            manager.setRegistrationListener(profile.getUriString(), new SipRegistrationListener() {
                @Override
                public void onRegistering(String localProfileUri) {
                    setDispatch("Rejestrowanie...");
                }

                @Override
                public void onRegistrationDone(String localProfileUri, long expiryTime) {
                    setDispatch("Polaczono!");
                }

                @Override
                public void onRegistrationFailed(String localProfileUri, int errorCode, String errorMessage) {
                    setDispatch("Nie mozna polaczyc z serwerem.");
                }
            });
        } catch (Exception e){
            e.printStackTrace();
            setDispatch("Wystapil blad.");
        }
    }

    private void closeProfile(){
        try{
            if(profile != null){
                manager.close(profile.getUriString());
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void callSomeone(){
        setDispatch(address);

        try{
            SipAudioCall.Listener listener = new SipAudioCall.Listener(){
                @Override
                public void onCallEstablished(SipAudioCall call) {
                    call.startAudio();
                    call.setSpeakerMode(true);
                    setDispatch("Nawiazano polaczenie!");
                }

                @Override
                public void onCallEnded(SipAudioCall call){
                    setDispatch("Polaczenie zakonczono.");
                }
            };

            audioCall = manager.makeAudioCall(profile.getUriString(), address, listener, 35);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setDispatch(final String message){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tv = (TextView)findViewById(R.id.textViewKomunikat);
                tv.setText(message);
            }
        });
    }

    public void updateCallStatus(SipAudioCall currentCall){
        if((audioCall!= null) && audioCall.isInCall()){
            try{
                currentCall.endCall();
            } catch (SipException e){
                e.printStackTrace();
            }
            currentCall.close();
            return;
        }
        audioCall = currentCall;
        String name = audioCall.getPeerProfile().getDisplayName();
        if(name == null){
            name = audioCall.getPeerProfile().getProfileName();
        }
        setDispatch(name + "@" + audioCall.getPeerProfile().getSipDomain());
    }

}
