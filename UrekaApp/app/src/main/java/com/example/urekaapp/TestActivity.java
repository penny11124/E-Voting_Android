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
import java.util.HexFormat;
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
            // Sign and Verify
            // Message
            String message = "hello";
//            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
            byte[] messageBytes = SerializationUtil.hexToBytes("24aec01891da122a72855fafa76f5686575f30c190fd2ef7b919994116b47f85");

            // PublicKey
            String xHex = "bd79c248bd7053198b7e62521e051b5d32ee52b8db081951c622d8907e7b47ad";
            String yHex = "241291c8bb206f0aff82b1beb388ced439a4239deb5599285d1f536ff4f29277";
//            String xBase64 = SerializationUtil.byteToBase64Str(SerializationUtil.hexToBytes(xHex));
//            String yBase64 = SerializationUtil.byteToBase64Str(SerializationUtil.hexToBytes(yHex));

            BigInteger x = new BigInteger(xHex, 16);
            BigInteger y = new BigInteger(yHex, 16);
            ECPoint ecPoint = new ECPoint(x, y);

            ECGenParameterSpec ecGenSpec = new java.security.spec.ECGenParameterSpec("secp256k1");
            KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("EC", "SC");
            kpg.initialize(ecGenSpec);
            ECParameterSpec ecSpec = ((ECPublicKey) kpg.generateKeyPair().getPublic()).getParams();

            ECPublicKeySpec ecPublicKeySpec = new ECPublicKeySpec(ecPoint, ecSpec);
            KeyFactory keyFactory = KeyFactory.getInstance("EC", "SC");
            PublicKey publicKey = keyFactory.generatePublic(ecPublicKeySpec);
//            String publicKeyBase64 = xBase64 + "-" + yBase64;
//            String publicKeyBase64 = "HyxOPKkWw6iBdnW9UsCvSIaTmtpAmqWcUZ0lTPKUFW8=-dr9x3JLY3+vEwwEYPG7GIQK7fRS5JktX7iOZQkFYxro=";
//            ECPublicKey publicKey = (ECPublicKey) SerializationUtil.strToKey(publicKeyBase64, "eccPublicKey");

            // PrivateKey
            String privateKeyHex = "cdbddef053e9ecfdd155b3986a44c463c204d18c90bc4ce0c1c71e4a260c6f61";
            byte[] privateKeyBytes = SerializationUtil.hexToBytes(privateKeyHex);

            BigInteger privateKeyValue = new BigInteger(1, privateKeyBytes); // Ensure positive
            org.spongycastle.jce.spec.ECPrivateKeySpec privateKeySpec = new org.spongycastle.jce.spec.ECPrivateKeySpec(privateKeyValue, ECNamedCurveTable.getParameterSpec("secp256k1"));
            keyFactory = KeyFactory.getInstance("EC", "SC");
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

            // Signature
            String signatureBase64 = "qqtQmnzOgbBw1kyC4KzrgpttWtDFFn2EKxbVxqcSIm0=-IcFFfeeTX7jSYUh6O2HvNkul/frxGPuLJ4xA5PDbhI8=";
            String rBase64 = "qqtQmnzOgbBw1kyC4KzrgpttWtDFFn2EKxbVxqcSIm0";
            String sbase64 = "IcFFfeeTX7jSYUh6O2HvNkul/frxGPuLJ4xA5PDbhI8";
            byte[] rBytes = SerializationUtil.base64StrBackToByte(rBase64);
            byte[] sBytes = SerializationUtil.base64StrBackToByte(sbase64);
            rBase64 = SerializationUtil.byteToBase64Str(rBytes);
            sbase64 = SerializationUtil.byteToBase64Str(sBytes);
            signatureBase64 = rBase64 + "-" + sbase64;
//            byte[] signatureBytes = ECC.signSignature(messageBytes, privateKey);
//            Signature signer = Signature.getInstance("SHA256withECDSA", "SC");
//            signer.initSign(privateKey);
//            signer.update(messageBytes);
//            byte[] signatureBytes = signer.sign();
            byte[] signatureBytes = SerializationUtil.base64StrToSignature(signatureBase64);
            Signature verifier = Signature.getInstance("SHA256withECDSA", "SC");
            verifier.initVerify(publicKey);
            verifier.update(messageBytes);
            boolean isValid = verifier.verify(signatureBytes);
//            boolean isValid = ECC.verifySignature(messageBytes, signatureBytes, publicKey);
            SimpleLogger.simpleLog("info", "isValid = " + isValid);
            ///////////////////////////////////////////////////////////////////////////////////
        } catch (Exception e) {
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