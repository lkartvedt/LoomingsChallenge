package com.lkartvedt.sada.loomings;

import com.google.common.hash.Hashing;
import io.minio.*;
import io.minio.errors.MinioException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

// Code sourced from https://docs.min.io/docs/java-client-quickstart-guide.html
public class Loomings {

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        try {
            // Create a minioClient with the MinIO server playground, access key, and secret key
            MinioClient minioClient =
                    MinioClient.builder()
                            .endpoint("https://play.min.io")
                            .credentials("Q3AM3UQ867SPQQA43P2F", "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG")
//                            .credentials("lkartvedt", "zoomieS141523!")
                            .build();

            String bucketName = "loomings";
            String objectName = "Loomings";
            String fileName = "./loomings.txt";

            // Make bucket if it doesn't exist
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                // Make a new bucket
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            } else {
                System.out.printf("Bucket '%s' already exists.\n", bucketName);
            }
            // Upload file as a named object to the bucket
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .filename(fileName)
                            .build());
            System.out.printf("'%s' was successfully uploaded as object '%s' to bucket '%s'.\n", fileName, objectName, bucketName);

            //Loop through file. If line is not empty, increase line count, put it in a text file, add the textfile to the bucket
            int lineCount = 0;
            LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(fileName));
            String lineString;
            while((lineString = lineNumberReader.readLine()) != null){
                if(!lineString.isEmpty()) {
                    lineCount++;
                    String lineFileName = "File-" + String.valueOf(lineCount);
                    InputStream inputStream = new ByteArrayInputStream(lineString.getBytes());
                    // Create hash and metadata
                    // Using the Guava Library for the SHA-254 Hash function
                    String contentSha256Hash = Hashing.sha256()
                            .hashString(lineString, StandardCharsets.UTF_8)
                            .toString();
                    HashMap<String, String> userMetadata = new HashMap<>();
                    userMetadata.put("Content-hash", contentSha256Hash);
                    // Build and upload object to bucket
                    minioClient.putObject(
                            PutObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(lineFileName)
                                    .stream(inputStream, lineString.length(), -1)
                                    .userMetadata(userMetadata)
                                    .contentType("text/plain")
                                    .build());
                }
            }
            System.out.printf("Line files from '%s' were successfully uploaded with unique names to bucket '%s'.\n", fileName, bucketName);
            lineNumberReader.close();

            if (lineCount == 1) {
                System.out.printf("There is %d line in '%s'.\n", lineCount, fileName);
            } else {
                System.out.printf("There are %d total lines in '%s'.\n", lineCount, fileName);
            }

        } catch (MinioException e) { //try catch block for MinIO exception because I actually want to see the error messages
            System.out.println("Error occurred: " + e);
            System.out.println("HTTP trace: " + e.httpTrace());
        }
    }
}