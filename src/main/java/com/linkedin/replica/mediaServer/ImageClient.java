package com.linkedin.replica.mediaServer;

import org.apache.commons.net.util.Base64;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

public class ImageClient {

    // TODO: Handle security of media server

    public static String writeBufferedImage(String encodedFile) throws IOException, URISyntaxException {
        System.setProperty("HADOOP_USER_NAME", "hduser");
        Configuration config = new Configuration();

        BASE64Decoder decoder = new BASE64Decoder();
        byte[] decodedBytes = decoder.decodeBuffer(encodedFile);
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(decodedBytes));

        FileSystem fs = FileSystem.get(new URI( "hdfs://localhost:9000" ), config );
        UUID imageId = UUID.randomUUID();
        Path path = new Path("hdfs://localhost:9000/images/" + imageId.toString());
        ImageIO.write(image,"JPG", fs.create(path));
        return "/webhdfs/v1/images/" + imageId.toString() + "?op=OPEN";
    }

    public static void main(String[] args) throws Exception {
        File file = new File("lights.jpg");
        FileInputStream fileInputStreamReader = new FileInputStream("lights.jpg");
        byte[] bytes = new byte[(int)file.length()];
        fileInputStreamReader.read(bytes);
        String encodedFile = null;
        encodedFile = new String(Base64.encodeBase64(bytes), "UTF-8");

        System.out.println(writeBufferedImage(encodedFile));
    }
}