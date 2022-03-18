import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;

import java.io.*;

public class Main {

    public static void main(String[] args) {
        Main main = new Main();
        main.run();
    }

    public void run() {
        AmazonS3 s3 = createS3Client();
        try {
            depositToS3(s3);
            System.out.println("success");
        } catch (IOException e) {
            System.out.println("something went wrong");
        }
    }

    private AmazonS3 createS3Client() {
        MyLogger.debug("Connecting to Amazon S3");
        AWSCredentials credentials = null;
        try {
            credentials = new EnvironmentVariableCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException("Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct location (~/.aws/credentials), " +
                    "and is in valid format.", e);
        }

        @SuppressWarnings("deprecation")
        AmazonS3 s3 = new AmazonS3Client(credentials);
        Region usEast1 = Region.getRegion(Regions.fromName("us-east-2"));
        s3.setRegion(usEast1);

        return s3;
    }

    private void depositToS3(AmazonS3 s3) throws IOException {
        try {
            long time = System.currentTimeMillis();
            String timeStamp = Long.toString(time);

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
            PutObjectRequest putRequest = new PutObjectRequest("s3dtestbucket", "testkey" + timeStamp + ".json",
                    createSampleFile("testvalue"));
            putRequest.setMetadata(objectMetadata);

            /*
             * Upload an object to your bucket - You can easily upload a file to S3, or
             * upload directly an InputStream if you know the length of the data in the
             * stream. You can also specify your own metadata when uploading to S3, which
             * allows you set a variety of options like content-type and content-encoding,
             * plus additional metadata specific to your applications.
             */
            MyLogger.debug("Uploading a new object to S3: " + "testvalue");
            PutObjectResult result = s3.putObject(putRequest);
            MyLogger.debug(result.toString());
        } catch (AmazonServiceException ase) {
            MyLogger.debug("Caught an AmazonServiceException, which means your request made it to Amazon S3, " +
                    "but was rejected with an error response for some reason.");
            MyLogger.debug("Error Message:    " + ase.getMessage());
            MyLogger.debug("HTTP Status Code: " + ase.getStatusCode());
            MyLogger.debug("AWS Error Code:   " + ase.getErrorCode());
            MyLogger.debug("Error Type:       " + ase.getErrorType());
            MyLogger.debug("Request ID:       " + ase.getRequestId());
            throw ase;
        } catch (AmazonClientException ace) {
            MyLogger.debug("Caught an AmazonClientException, which means the client encountered a serious internal " +
                    "problem while trying to communicate with S3, such as not being able to access the network.");
            MyLogger.debug("Error Message: " + ace.getMessage());
            throw ace;
        }
    }

    private File createSampleFile(String json) throws IOException {
        File file = File.createTempFile("aws-java-sdk-", ".json");
        file.deleteOnExit();

        Writer writer = new OutputStreamWriter(new FileOutputStream(file));
        writer.write(json);
        writer.close();

        return file;
    }
}