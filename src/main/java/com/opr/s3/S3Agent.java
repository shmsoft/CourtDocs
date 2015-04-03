package com.opr.s3;

import com.opr.finshred.Settings;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mark
 */
public class S3Agent {

    private String myAwsAccessKey;
    private String myAwsSecretKey;
    private String myBucket;
    private final static Logger logger = LoggerFactory.getLogger(S3Agent.class);
    private S3Service s3Service;
    private String CONTENT_TYPE_KEY = "Content-Type";
    private String contentType;

    /**
     * Put the file into S3.
     *
     * @param key key for S3, will be sanitized for Windows-to-S3 conventions, or kept as is in
     * Linux.
     * @param fileName file path to put in.
     * @return
     */
    public boolean putFileInS3(String key, String fileName) {
        Settings settings = Settings.getSettings();
        String bucket = myBucket != null ? myBucket : settings.getBucket();
        logger.info("Putting file {} into bucket {} with key {}", fileName, bucket,
                key.replaceAll("\\\\", "/"));
        try {
            connect();
            File fileData = new File(fileName);
            S3Object fileObject = new S3Object(fileData);
            fileObject.setKey(key.replaceAll("\\\\", "/"));
            if (contentType != null) {
                fileObject.addMetadata(CONTENT_TYPE_KEY, contentType);
            }
            s3Service.putObject(bucket, fileObject);
            logger.info("Successfully uploaded file {} to S3 bucket {} with key {}",
                    fileName, bucket, key);
        } catch (S3ServiceException | NoSuchAlgorithmException | IOException e) {
            logger.error("Error putting file into bucket", e);
            return false;
        }
        return true;
    }

    public String getFileFromS3AsString(String key) {
        String result = "";
        try {
            connect();
            S3Object s3object = s3Service.getObject(myBucket, key);
            InputStream objectData = s3object.getDataInputStream();
            result = IOUtils.toString(objectData);
            objectData.close();
        } catch (Exception e) {
            logger.error("Error getting file {} from bucket {}", key, myBucket, e);
        }
        return result;
    }
    private void connect() throws S3ServiceException {
        Settings settings = Settings.getSettings();
        String awsAccessKey = myAwsAccessKey != null ? myAwsAccessKey : settings.getAmazonId();
        String awsSecretKey = myAwsSecretKey != null ? myAwsSecretKey : settings.getAmazonKey();
        AWSCredentials awsCredentials =
                new AWSCredentials(awsAccessKey, awsSecretKey);
        s3Service = new RestS3Service(awsCredentials);
    }

    public static void main(String argv[]) {
        System.out.println("FinShred - S3 upload - version " + Settings.VERSION);
        if (argv.length == 0) {
            System.out.println("Arguments: \n"
                    + "<bucket> - bucket to which to upload files\n"
                    + "<aws_access_key> - Amazon id key\n"
                    + "<aws_secret_key> - Amazon secret key\n"
                    + "<upload_folder> - folder from which to upload files\n"
                    + "<s3_folder> - S3 folder to which to upload files\n");
            System.exit(0);
        }
        S3Agent agent = new S3Agent();
        agent.setMyBucket(argv[0]);
        agent.setMyAwsAccessKey(argv[1]);
        agent.setMyAwsSecretKey(argv[2]);
        File[] files = new File(argv[3]).listFiles();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String dateStr = dateFormat.format(date);
        String archiveDir = argv[3] + File.separator + dateStr;
        new File(archiveDir).mkdirs();
        for (File file : files) {
            String path = file.getPath();
            String key = argv[4] + "/" + dateStr + "/" + new File(path).getName();
            agent.putFileInS3(key, path);
            // archive the file
            Path filePath = Paths.get(file.getPath());
            Path archivePath = Paths.get(archiveDir + File.separator + file.getName());
            try {
                Files.move(filePath, archivePath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                logger.error("Problem archiving file {} to {}", filePath.toString(),
                        archivePath.toString(), e);
            }
        }
    }

    /**
     * @return the myAwsAccessKey
     */
    public String getMyAwsAccessKey() {
        return myAwsAccessKey;
    }

    /**
     * @param myAwsAccessKey the myAwsAccessKey to set
     */
    public void setMyAwsAccessKey(String myAwsAccessKey) {
        this.myAwsAccessKey = myAwsAccessKey;
    }

    /**
     * @return the myAwsSecretKey
     */
    public String getMyAwsSecretKey() {
        return myAwsSecretKey;
    }

    /**
     * @param myAwsSecretKey the myAwsSecretKey to set
     */
    public void setMyAwsSecretKey(String myAwsSecretKey) {
        this.myAwsSecretKey = myAwsSecretKey;
    }

    /**
     * @return the myBucket
     */
    public String getMyBucket() {
        return myBucket;
    }

    /**
     * @param myBucket the myBucket to set
     */
    public void setMyBucket(String myBucket) {
        this.myBucket = myBucket;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
