package com.sequenceiq.gcc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.Security;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;

import com.github.fommil.ssh.SshRsaCrypto;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.compute.Compute;
import com.google.api.services.compute.ComputeScopes;
import com.google.api.services.compute.model.Image;
import com.google.api.services.compute.model.ImageList;
import com.google.api.services.storage.StorageScopes;

public class GccTesterMain {

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Arrays.asList(ComputeScopes.COMPUTE, StorageScopes.DEVSTORAGE_FULL_CONTROL);

    public static void main(String[] args) {

        if (args.length < 3) {
            System.out.println("Usage:");
            System.out.println("args[0]: key file path");
            System.out.println("args[1]: service account mail");
            System.out.println("args[2]: project id");
            System.out.println("  java -jar gcc-credential-tester.jar <keyFile> <serviceAccountEmailAddress> <projectId>");
            System.exit(-1);
        }
        if(!args[1].endsWith("@developer.gserviceaccount.com")) {
            System.out.println("Serviceaccount is in wrong format it should ends with @developer.gserviceaccount.com");
            System.exit(-1);
        }
        String key = "";
        try {
            FileInputStream fisTargetFile = new FileInputStream(new File(args[0]));
            key = IOUtils.toString(fisTargetFile, "UTF-8");
        } catch (Exception e) {
            System.out.println("File not found");
            System.exit(-1);
        }

        validate(key);
        Compute compute = buildCompute(args[2], key, args[1]);

        if(compute == null){
            System.out.println("Your credentials are invalid...");
            System.exit(-1);
        } else {
            try {
                Compute.Images.List list = compute.images().list(args[2]);
                ImageList execute = list.execute();
                try {
                    for(Image image : execute.getItems()) {
                        System.out.println(image.getName());
                    }
                } catch (NullPointerException ex) {
                    System.out.println("You have no images...");
                }
            } catch (IOException e) {
                System.out.println("Your credentials are invalid: " + e.getMessage());
                System.out.println(e);
                System.exit(-1);
            }
        }
        System.out.println("Your credential is ok...");
    }

    public static Compute buildCompute(String projectid, String key, String serviceAccountId) {
        try {
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            BufferedReader br = new BufferedReader(new StringReader(key));
            Security.addProvider(new BouncyCastleProvider());
            KeyPair kp = (KeyPair) new PEMReader(br).readObject();
            GoogleCredential credential = new GoogleCredential.Builder().setTransport(httpTransport)
                    .setJsonFactory(JSON_FACTORY)
                    .setServiceAccountId(serviceAccountId)
                    .setServiceAccountScopes(SCOPES)
                    .setServiceAccountPrivateKey(kp.getPrivate())
                    .build();
            Compute compute = new Compute.Builder(
                    httpTransport, JSON_FACTORY, null).setApplicationName("myapp")
                    .setHttpRequestInitializer(credential)
                    .build();
            return compute;
        } catch (GeneralSecurityException e) {
            System.out.println("General security exception: " + e.getMessage());
            System.exit(-1);
        } catch (IOException e) {
            System.out.println("Ioexception under the compute creation: " + e.getMessage());
            System.exit(-1);
        } catch (Exception e) {
            System.out.println("Error occured: " + e.getMessage());
            System.exit(-1);
        }
        return null;
    }

    public static void validate(String key) {
        try {
            SshRsaCrypto rsa = new SshRsaCrypto();
            PublicKey publicKey = rsa.readPublicKey(rsa.slurpPublicKey(key));
        } catch (Exception e) {
            String errorMessage = String.format("Could not validate publickey certificate  %s", key);
        }
    }
}
