package com.example.urekaapp;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import android.app.ServiceStartNotAllowedException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.urekaapp.ble.BLEManager;
import com.example.urekaapp.ble.BLEViewModel;

import org.bouncycastle.mime.smime.SMimeMultipartContext;
import org.spongycastle.asn1.ASN1EncodableVector;
import org.spongycastle.asn1.ASN1Integer;
import org.spongycastle.asn1.ASN1Sequence;
import org.spongycastle.asn1.DERSequence;
import org.spongycastle.jce.ECNamedCurveTable;
import org.spongycastle.math.ec.ECCurve;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.Objects;

import ureka.framework.Environment;
import ureka.framework.model.message_model.Message;
import ureka.framework.model.message_model.RTicket;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.crypto.ECC;
import ureka.framework.resource.crypto.ECDH;
import ureka.framework.resource.crypto.SerializationUtil;
import ureka.framework.resource.logger.SimpleLogger;
import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;

import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPrivateKeySpec;


public class TestActivity extends AppCompatActivity {
    private BLEManager bleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        Environment.applicationContext = TestActivity.this;
        setContentView(R.layout.activity_test);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        try {
//            String publicKeyBase64 = "xyVam2yUMl2Zr7rbeiApICk4WrWBetK7wEusfMIIp44=-dvHta1ksfPsAkAlvkpc+VIoko9qloZixDePT2cmZyVY=";
//            String privateKeyHex = "693e667770e6eb1a7699196a1fb52d59cf3a59dbabd0878e75e0a53cab177b98";
//
//            PublicKey publicKey = SerializationUtil.base64ToPublicKey(publicKeyBase64);
//            PrivateKey privateKey = SerializationUtil.hexToPrivateKey(privateKeyHex);
//
//            String message = "{\"protocol_version\":\"UREKA-1.0\",\"r_ticket_type\":\"INITIALIZATION\",\"device_id\":\"xyVam2yUMl2Zr7rbeiApICk4WrWBetK7wEusfMIIp44=-dvHta1ksfPsAkAlvkpc+VIoko9qloZixDePT2cmZyVY=\",\"result\":\" -> SUCCESS: VERIFY_UT_CAN_EXECUTE\",\"ticket_order\":1,\"audit_start\":\"hs9BBZca9pU4NhrLOotEXA6PBWIrwHvs5rRDtuC+as0=\"}\n";
//            byte[] messageBytes = SerializationUtil.strToBytes(message);
//            byte[] signatureBytes = ECC.signSignature(messageBytes, privateKey);
//            boolean result = ECC.verifySignature(signatureBytes, messageBytes, publicKey);
//            SimpleLogger.simpleLog("info", "result = " + result);
            // The result is true
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//            String xHex = "d4a7d75d580522fa6da9a8c341a0795fbb29e10fbd39910a70e63928cb4e51cd";
//            String yHex = "2cf35542ef68415e62d7975213376e9421fbd94019ca6af82fd95e2209c6441a";
//            PublicKey publicKey = SerializationUtil.hexToPublicKey(xHex, yHex);
//
//            String rHex = "0b99de8bf68920d612dfd3bbf9bb4f09e260a5539783cf5eef1ce361f75f1682";
//            String sHex = "cfc2b3dee908ca61bb8fab7704a30f4cb18c742448e67225d102836b6d5b8248";
//            byte[] signatureBytes = SerializationUtil.hexToSignature(rHex, sHex);
//
//            String message = "{\"protocol_version\":\"UREKA-1.0\",\"r_ticket_type\":\"INITIALIZATION\",\"device_id\":\"1KfXXVgFIvptqajDQaB5X7sp4Q+9OZEKcOY5KMtOUc0=-LPNVQu9oQV5i15dSEzdulCH72UAZymr4L9leIgnGRBo=\",\"result\":\" -> SUCCESS: VERIFY_UT_CAN_EXECUTE\",\"ticket_order\":1,\"audit_start\":\"h/Euv/2vTP1c80AJQoWQcV/QSA10yR7cVmIAOFFNnEU=\"}";
//            byte[] messageBytes = SerializationUtil.strToBytes(message);
//            boolean result = ECC.verifySignature(signatureBytes, messageBytes, publicKey);
//            SimpleLogger.simpleLog("info", "result = " + result);
            // The result is true
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        } catch (Exception e) {
            SimpleLogger.simpleLog("error", "Test failed");
            throw new RuntimeException(e);
        }

        BLEViewModel bleViewModel = new ViewModelProvider(this).get(BLEViewModel.class);
        bleManager = bleViewModel.getBLEManager(TestActivity.this);

        Button buttonConnect = findViewById(R.id.buttonConnect);
        Button buttonSendData = findViewById(R.id.buttonSendData);
        TextView textViewConnectingStatus = findViewById(R.id.textViewConnectionStatus);
        TextView textViewDataSent = findViewById(R.id.textViewDataSent);
        TextView textViewDataReceived = findViewById(R.id.textViewDataReceived);

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TestActivity.this, "Connecting...", Toast.LENGTH_SHORT).show();
                bleManager.startScan("HC-04BLE", new BLEManager.BLECallback() {
                    @Override
                    public void onConnected() {
                        SimpleLogger.simpleLog("info", "Device connected!");
                        runOnUiThread(() -> textViewConnectingStatus.setText("Device Connected."));
                    }

                    @Override
                    public void onDisconnected() {
                        SimpleLogger.simpleLog("info", "Device disconnected!");
                        runOnUiThread(() -> textViewConnectingStatus.setText("Device Disconnected."));
                    }

                    @Override
                    public void onDataReceived(String data) {
                        SimpleLogger.simpleLog("info", "Received data: " + data);
                        runOnUiThread(() -> {
                            Log.d("TestActivity", data);
                            textViewDataReceived.setText("Data received: " + data);
                        });
                    }
                });
            }
        });

        buttonSendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "{\"message_operation\":\"MESSAGE_VERIFY_AND_EXECUTE\",\"message_type\":\"UTICKET\",\"message_str\":\"{\\\"protocol_version\\\":\\\"UREKA-1.0\\\",\\\"u_ticket_id\\\":\\\"CI3T1XsBos4MEEFMLuK0Wy2/h+rzghjkXDH0AwxoRCA=\\\",\\\"u_ticket_type\\\":\\\"INITIALIZATION\\\",\\\"device_id\\\":\\\"no_id\\\",\\\"ticket_order\\\":0,\\\"holder_id\\\":\\\"Vp0YwOXAkVf7wNO5gqfVqveAwcNh7YY42Lzk/7kc7kg=-fb2eO4YWd0mhg4e3rGePzUjn4RBpgTm9EnIoCXBwamg=\\\"}\"}$";
//                textViewDataSent.setText(message);
                bleManager.sendData(message);
            }
        });
    }

    public static byte[] encodeDERSignature(BigInteger r, BigInteger s) throws Exception {
        ASN1EncodableVector vector = new ASN1EncodableVector();
        vector.add(new ASN1Integer(r));
        vector.add(new ASN1Integer(s));
        DERSequence sequence = new DERSequence(vector);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(sequence.getEncoded());
        return outputStream.toByteArray();
    }
}