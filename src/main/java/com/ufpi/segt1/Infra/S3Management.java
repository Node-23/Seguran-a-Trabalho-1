package com.ufpi.segt1.Infra;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
@Component
public class S3Management {

    @Value("${aws.accessKey}")
    private String accessKey;

    @Value("${aws.secretKey}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.bucketName}")
    private String bucketName;

    private AmazonS3 s3Client;

    @PostConstruct
    private void initializeAmazonS3Client() {
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
                .build();
    }

    public void uploadStringToS3(String content, String keyName) {
        try {
            byte[] contentBytes = content.getBytes();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(contentBytes);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(contentBytes.length);
            s3Client.putObject(new PutObjectRequest(bucketName, keyName, inputStream, metadata));
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
    }

    public void renameObject(String oldKeyName, String newKeyName) {
        CopyObjectRequest copyRequest = new CopyObjectRequest(bucketName, oldKeyName, bucketName, newKeyName);
        s3Client.copyObject(copyRequest);
        DeleteObjectRequest deleteRequest = new DeleteObjectRequest(bucketName, oldKeyName);
        s3Client.deleteObject(deleteRequest);
    }

    public void deleteObject(String keyName) {
        DeleteObjectRequest deleteRequest = new DeleteObjectRequest(bucketName, keyName);
        s3Client.deleteObject(deleteRequest);
    }

    public PublicKey readPublicKeyFromS3(String keyName) throws IOException {
        S3Object object = s3Client.getObject(new GetObjectRequest(bucketName, keyName));
        String publicKeyString = getKeyString(object);
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyString);
        try {
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(publicKeySpec);
        } catch (Exception e) {
            throw new IOException("Error reading public key from Amazon S3", e);
        }
    }

    public PrivateKey readPrivateKeyFromS3(String keyName) throws IOException {
        S3Object object = s3Client.getObject(new GetObjectRequest(bucketName, keyName));
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(object.getObjectContent()));
             PemReader pemReader = new PemReader(reader)) {
            PemObject pemObject = pemReader.readPemObject();
            byte[] keyBytes = pemObject.getContent();
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new IOException("Error reading private key from Amazon S3", e);
        }
    }

    private static String getKeyString(S3Object object) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(object.getObjectContent()));
        StringBuilder publicKeyStringBuilder = new StringBuilder();
        String line;
        boolean insideKey = false;
        while ((line = reader.readLine()) != null) {
            if (line.contains("BEGIN PUBLIC KEY")) {
                insideKey = true;
                continue;
            } else if (line.contains("END PUBLIC KEY")) {
                insideKey = false;
                continue;
            }
            if (insideKey) {
                publicKeyStringBuilder.append(line);
            }
        }
        reader.close();
        return publicKeyStringBuilder.toString().replaceAll("\\s", "");
    }
}

