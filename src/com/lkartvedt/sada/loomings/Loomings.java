package com.lkartvedt.sada.loomings;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.MinioException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

// Code sourced from https://docs.min.io/docs/java-client-quickstart-guide.html
public class Loomings {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        try {

            // Create a minioClient with the MinIO server playground, access key, and secret key
            MinioClient minioClient =
                    MinioClient.builder()
                            .endpoint("https://play.min.io")
                            .credentials("Q3AM3UQ867SPQQA43P2F", "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG")
                            .build();

            String bucketName = "loomings";
            String objectName = "LoomingsTextFile";
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
            System.out.printf("'%s' is successfully uploaded as object '%s' to bucket '%s'.\n", fileName, objectName, bucketName);

        } catch (MinioException e) {
            System.out.println("Error occurred: " + e);
            System.out.println("HTTP trace: " + e.httpTrace());
        }
    }
}
