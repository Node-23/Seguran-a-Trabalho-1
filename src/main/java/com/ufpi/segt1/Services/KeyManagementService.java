package com.ufpi.segt1.Services;

import com.ufpi.segt1.Infra.S3Management;
import com.ufpi.segt1.Models.Key;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.io.IOException;
import java.io.StringWriter;
import java.security.*;
import java.util.Base64;

@Service
public class KeyManagementService {
    private final S3Management s3Management;
    private final KeyService keyService;
    public static final String privateKeySufix = "_private.pem";
    public static final String publicKeySufix = "_public.pem";

    public KeyManagementService(S3Management s3Management, KeyService keyService) {
        this.s3Management = s3Management;
        this.keyService = keyService;
    }

    public void CreateKeyPair(Key key) {
        try {
            Security.addProvider(new BouncyCastleProvider());
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            StringWriter privateWriter = new StringWriter();
            try (PemWriter pemWriter = new PemWriter(privateWriter)) {
                PemObject pemObject = new PemObject("PRIVATE KEY", keyPair.getPrivate().getEncoded());
                pemWriter.writeObject(pemObject);
            }
            String privateKeyContent = privateWriter.toString();
            StringWriter publicWriter = new StringWriter();
            try (PemWriter pemWriter = new PemWriter(publicWriter)) {
                PemObject pemObject = new PemObject("PUBLIC KEY", keyPair.getPublic().getEncoded());
                pemWriter.writeObject(pemObject);
            }
            String publicKeyContent = publicWriter.toString();
            String privateKeyKeyName = key.getName() + privateKeySufix;
            String publicKeyKeyName = key.getName() + publicKeySufix;
            s3Management.uploadStringToS3(privateKeyContent, privateKeyKeyName);
            s3Management.uploadStringToS3(publicKeyContent, publicKeyKeyName);
        } catch (Exception e) {
            //TODO: Make this exception
            e.printStackTrace();
        }
    }

    public String EncryptMessage(String message) {
        try {
            byte[] encryptedMessage = encrypt(message);
            return Base64.getEncoder().encodeToString(encryptedMessage);
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    private byte[] encrypt(String message) throws Exception {
        PublicKey publicKey = null;
        try{
            publicKey = s3Management.readPublicKeyFromS3("public_key.pem");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(message.getBytes("UTF-8"));
    }

    public String DecryptMessage(String encryptedMessage){
        try {
            PrivateKey privateKey = s3Management.readPrivateKeyFromS3("private_key.pem");
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedMessage);
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }
}
