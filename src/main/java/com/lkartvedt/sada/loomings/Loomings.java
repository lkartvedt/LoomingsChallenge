package com.lkartvedt.sada.loomings;

import com.google.common.hash.Hashing;
import io.minio.*;
import io.minio.errors.MinioException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.lang.Math;
import java.util.stream.Collectors;

import com.lkartvedt.sada.loomings.LinePair;

// Code sourced from https://docs.min.io/docs/java-client-quickstart-guide.html
public class Loomings {

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        try {
            //these dashes help me personally locate my console output
            System.out.println("--------------------------------------------------------------");
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
                System.out.printf("LOGMSG: Bucket '%s' already exists.\n", bucketName);
            }
            // Upload file as a named object to the bucket
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .filename(fileName)
                            .build());
            System.out.printf("LOGMSG: '%s' was successfully uploaded as object '%s' to bucket '%s'.\n", fileName, objectName, bucketName);

            //Loop through file. If line is not empty, increase line count, put it in a text file, add the textfile to the bucket
            int lineCount = 0;
            //Open the file as a stream with correct encoding, then read with InputStreamReader, then wrap in BufferedReader (faster)
            BufferedReader bufferedReader = new BufferedReader((new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_8)));
            String lineString;
            // Create a list to store name and corresponding size in
            List<LinePair> lineList = new ArrayList<>();
            while((lineString = bufferedReader.readLine()) != null){
                String lineFileName;
                if(!lineString.isEmpty()) {
                    lineCount++;
                    if(lineCount < 10){
                        lineFileName = "File-0" + String.valueOf(lineCount);
                    }else {
                        lineFileName = "File-" + String.valueOf(lineCount);
                    }
                    InputStream inputStream = new ByteArrayInputStream(lineString.getBytes());
                    // Create hash and metadata
                    // Using the Guava Library for the SHA-254 Hash function
                    String contentSha256Hash = Hashing.sha256()
                            .hashString(lineString, StandardCharsets.UTF_8)
                            .toString();
                    HashMap<String, String> userMetadata = new HashMap<>();
                    userMetadata.put("content-hash", contentSha256Hash);
                    // Build and upload object to bucket
                    minioClient.putObject(
                            PutObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(lineFileName)
                                    .stream(inputStream, lineString.length(), -1)
                                    .userMetadata(userMetadata)
                                    .contentType("text/plain")
                                    .build());
                    //create key/value pair to store the line's file name and size
                    LinePair linePair = new LinePair(lineFileName, lineString.length());
                    lineList.add(linePair);
                }
            }
            System.out.printf("LOGMSG: Line files from '%s' were successfully uploaded with unique names to bucket '%s'.\n", fileName, bucketName);
            bufferedReader.close();

            System.out.println("--------------------------------------------------------------");

            if (lineCount == 1) {
                System.out.printf("\nThere is %d (non-blank) line in '%s'.\n", lineCount, fileName);
            } else {
                System.out.printf("\nThere are %d (non-blank) lines in '%s'.\n", lineCount, fileName);
            }

            //sort ArrayList by each pair's value
            lineList.sort(Comparator.comparingInt(LinePair::value));

            //print file names in order from smallest to largest
            System.out.print("\nOrdered Line Files\n");
            System.out.print("------------------\n");
            for (LinePair pair : lineList) {
                if (pair.value() < 1024) {
                    System.out.printf("%10s: %dB\n", pair.key(), pair.value());
                } else {
                    Double largeSize = pair.value() / 1024.0;
                    System.out.printf("%10s: %.1fKiB\n", pair.key(), largeSize);
                }
            }

            //Find duplicate using API
            HashMap<String, String> lineHash = new HashMap<String, String>();
            Set<Integer> duplicateSet = new HashSet<>();
            for (LinePair linePair : lineList) {
                StatObjectResponse objectStat = minioClient.statObject(
                        StatObjectArgs.builder()
                                .bucket(bucketName)
                                .object(linePair.key())
                                .build());
                if (!lineHash.containsKey(objectStat.userMetadata().get("content-hash"))) {
                    lineHash.put(objectStat.userMetadata().get("content-hash"), linePair.key());
                } else {
                    //found duplicate
                    System.out.printf("\nDuplication in %s and %s.\n\n", linePair.key(), lineHash.get(objectStat.userMetadata().get("content-hash")));
                    InputStream stream = minioClient.getObject(
                            GetObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(linePair.key())
                                    .build());
                    String streamstring = new BufferedReader(new InputStreamReader(stream)).lines().collect(Collectors.joining("\n"));
                    System.out.println("Duplicate Line: " + streamstring);
                    duplicateSet.add(Integer.valueOf(linePair.key().split("-")[1]));
                }
            }

            //create new file without duplicate
            lineCount = 0;
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_8));
            PrintWriter newLoomings = new PrintWriter("loomings-clean.txt", StandardCharsets.UTF_8);
            while((lineString = bufferedReader.readLine()) != null){
                if(!lineString.isEmpty()) {
                    lineCount++;
                }

                if(duplicateSet.contains(lineCount)) {
                    duplicateSet.remove(lineCount);
                }
                else {
                    newLoomings.println(lineString);
                }
            }
            newLoomings.close();
            bufferedReader.close();


        } catch (MinioException e) { //try catch block for MinIO exception because I actually want to see the error messages
            System.out.println("Error occurred: " + e);
            System.out.println("HTTP trace: " + e.httpTrace());
        }
        System.out.println("--------------------------------------------------------------");
    }
}