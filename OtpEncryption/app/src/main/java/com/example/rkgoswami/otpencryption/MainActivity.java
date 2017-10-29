package com.example.rkgoswami.otpencryption;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    EditText editTextMobileNumber;
    TextView tvPrivateKey, tvPublicKey, tvGeneratedOtp, tvEncryptedOtp, tvDecryptedOtp;
    Button buttonSendOTP, btnGenRsaKey, btnGenOtp, btnEncryptOtp;
    PublicKey publicKey;
    PrivateKey privateKey;
    Integer otpInt;
    String otpString,encryptedOtpStr, decryptedOtpStr;

    /*Map object to hold the key*/
    Map<String, Object> RsaKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*Initialize the views*/
        tvPrivateKey = (TextView) findViewById(R.id.tv_private_key);
        tvPublicKey = (TextView) findViewById(R.id.tv_public_key);
        tvGeneratedOtp = (TextView) findViewById(R.id.tv_generate_otp);
        tvEncryptedOtp = (TextView) findViewById(R.id.tv_encrypted_otp);
        tvDecryptedOtp = (TextView) findViewById(R.id.tv_decrypted_otp);

        btnGenRsaKey = (Button) findViewById(R.id.btn_rsa_key);
        btnGenOtp = (Button) findViewById(R.id.btn_generate_otp);
        btnEncryptOtp = (Button) findViewById(R.id.btn_encrypt_otp);

        editTextMobileNumber = (EditText) findViewById(R.id.et_mobile_no);
        buttonSendOTP = (Button) findViewById(R.id.btn_send_otp);

        /* Generate Public and Private Key using RSA */
        btnGenRsaKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    RsaKey = getRSAKeys();
                    publicKey = (PublicKey) RsaKey.get("public Key");
                    privateKey = (PrivateKey) RsaKey.get("private Key");

                    /* Programmatically added scroll view to textView */
                    tvPublicKey.setMovementMethod(new ScrollingMovementMethod());
                    tvPrivateKey.setMovementMethod(new ScrollingMovementMethod());

                    tvPublicKey.setText(Base64.encodeToString(publicKey.getEncoded(),Base64.DEFAULT));
                    tvPrivateKey.setText(Base64.encodeToString(privateKey.getEncoded(),Base64.DEFAULT));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        /* Generate the Otp using HMAC Function*/
        btnGenOtp.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                TimeBasedOneTimePasswordGenerator tOtp = null;
                try {
                    tOtp = new TimeBasedOneTimePasswordGenerator();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

                final Key secretKey;
                {
                    final KeyGenerator keyGenerator;
                    try {
                        assert tOtp != null;
                        keyGenerator = KeyGenerator.getInstance(tOtp.getAlgorithm());

                        /* SHA-1 and SHA-256 prefer 64-byte (512-bit) keys;
                         * SHA512 prefers 128-byte keys
                         * */
                        keyGenerator.init(512);
                        secretKey = keyGenerator.generateKey();

                        /* Need current Time for TimeStamp for Otp Generation*/
                        final Date now = new Date();
                        otpInt = tOtp.generateOneTimePassword(secretKey, now);
                        otpString = otpInt.toString();
                        tvGeneratedOtp.setText(otpString);

                    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        btnEncryptOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    encryptedOtpStr = encryptMessage(otpString,publicKey);
                    tvEncryptedOtp.setText(encryptedOtpStr);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        buttonSendOTP.setOnClickListener(
                new View.OnClickListener() {

                    String userString;
                    EditText editTextOTP;
                    AlertDialog dialogUsername;

                    @Override
                    public void onClick(View v) {
                        try {
                            if(!TextUtils.isEmpty(editTextMobileNumber.getText())) {
                                sendOTP();
                                AlertDialog.Builder builderUsername = new AlertDialog.Builder(MainActivity.this);

                                builderUsername.setPositiveButton("Validate", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        editTextOTP = (EditText) dialogUsername.findViewById(R.id.EditTextOTP);
                                        assert editTextOTP != null;
                                        userString = String.valueOf(editTextOTP.getText());

                                        /*Decrypt the text received before matching*/
                                        try {
                                            decryptedOtpStr = decryptMessage(userString, privateKey);
                                            tvDecryptedOtp.setText(decryptedOtpStr);

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        if (otpString.equals(decryptedOtpStr)) {
                                            Toast.makeText(MainActivity.this, "OTP Validated!!!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(MainActivity.this, "Invalid OTP!!!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                                builderUsername.setNegativeButton("Cancel", null);
                                View myView = LayoutInflater.from(MainActivity.this).inflate(R.layout.validate_otp, null);
                                builderUsername.setView(myView);
                                builderUsername.setCancelable(false);
                                dialogUsername = builderUsername.create();
                                dialogUsername.show();
                            }
                            else {
                                editTextMobileNumber.setError("Cannot be left blank");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    private void sendOTP() throws NoSuchAlgorithmException, InvalidKeyException {
                        String mobileNumber = editTextMobileNumber.getText().toString();

                        final String acknowledgeSent = "OTP Sent!!!";
                        final String acknowledgeDelivered = "OTP Delivered!!!";

                        PendingIntent pendingIntentSent = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(acknowledgeSent), 0);
                        PendingIntent pendingIntentDelivered = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(acknowledgeDelivered), 0);

                        registerReceiver(new BroadcastReceiver() {

                            public void onReceive(Context context, Intent intent) {
                                if (getResultCode() == Activity.RESULT_OK) {
                                    Toast.makeText(MainActivity.this, acknowledgeSent, Toast.LENGTH_SHORT).show();
                                } else if (getResultCode() == SmsManager.RESULT_ERROR_GENERIC_FAILURE) {
                                    Toast.makeText(MainActivity.this, "Generic Failure.", Toast.LENGTH_SHORT).show();
                                } else if (getResultCode() == SmsManager.RESULT_ERROR_NO_SERVICE) {
                                    Toast.makeText(MainActivity.this, "No Service.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, new IntentFilter(acknowledgeSent));

                        registerReceiver(new BroadcastReceiver() {

                            public void onReceive(Context context, Intent intent) {
                                if (getResultCode() == Activity.RESULT_OK) {
                                    Toast.makeText(MainActivity.this, acknowledgeDelivered, Toast.LENGTH_SHORT).show();
                                } else if (getResultCode() == Activity.RESULT_CANCELED) {
                                    Toast.makeText(MainActivity.this, "OTP not delivered.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, new IntentFilter(acknowledgeDelivered));

                        sendMessage(encryptedOtpStr, mobileNumber, pendingIntentSent, pendingIntentDelivered);
                    }

                    private void sendMessage(String message, String mobileNumber, PendingIntent pendingIntentSent, PendingIntent pendingIntentDelivered) {
                        SmsManager smsManager = SmsManager.getDefault();
                        Toast.makeText(MainActivity.this,"Sent Text: "+message+"\n Length: "+message.length(),Toast.LENGTH_SHORT).show();
                        smsManager.sendTextMessage(mobileNumber, null, message , pendingIntentSent, pendingIntentDelivered);
                    }
                }
        );

        ScrollView parentSV = (ScrollView) findViewById(R.id.parentScrollView);
        parentSV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                tvPrivateKey.getParent().requestDisallowInterceptTouchEvent(false);
                tvPublicKey.getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }
        });
        tvPrivateKey.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        tvPublicKey.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();


    private static Map<String,Object> getRSAKeys() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(512);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        Map<String, Object> keys = new HashMap<>();
        keys.put("private Key", privateKey);
        keys.put("public Key", publicKey);
        return keys;
    }

    // Encrypt using RSA private key
    private static String encryptMessage(String plainText, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        return Base64.encodeToString(cipher.doFinal(plainText.getBytes()),Base64.DEFAULT);
        //return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes()));
    }

    // Decrypt using RSA public key
    private static String decryptMessage(String encryptedText, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        //return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedText)));

        return new String(cipher.doFinal(Base64.decode(encryptedText,Base64.DEFAULT)),"UTF-8");
    }


}
