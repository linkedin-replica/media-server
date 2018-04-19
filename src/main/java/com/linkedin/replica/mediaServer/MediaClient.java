package com.linkedin.replica.mediaServer;

import org.apache.commons.net.util.Base64;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

public class MediaClient {

    // TODO: Handle security of media server

    public static String writeFile(File file) throws IOException, URISyntaxException {
        if(System.getProperty("HADOOP_USER_NAME") == null)
            System.setProperty("HADOOP_USER_NAME", "hduser");
        Configuration config = new Configuration();

        FileInputStream fileInputStreamReader = new FileInputStream(file);
        InputStream in = new BufferedInputStream(fileInputStreamReader);
        FileSystem fs = FileSystem.get(new URI( "hdfs://localhost:9000" ), config);
        UUID fileId = UUID.randomUUID();
        Path path = new Path("hdfs://localhost:9000/media/" + fileId.toString());
        OutputStream out = fs.create(path);
        IOUtils.copyBytes(in, out, 4096, true);
        return "/webhdfs/v1/media/" + fileId.toString() + "?op=OPEN";
    }

//    public static String writeFile(File file) throws IOException, URISyntaxException {
//        if(System.getProperty("HADOOP_USER_NAME") == null)
//            System.setProperty("HADOOP_USER_NAME", "hduser");
//        Configuration config = new Configuration();
//
//        FileInputStream fileInputStreamReader = new FileInputStream(file);
//        byte[] bytes = new byte[(int)file.length()];
//        fileInputStreamReader.read(bytes);
//        BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
//
//        FileSystem fs = FileSystem.get(new URI( "hdfs://localhost:9000" ), config );
//        UUID imageId = UUID.randomUUID();
//        Path path = new Path("hdfs://localhost:9000/images/" + imageId.toString());
//        ImageIO.write(image,"PNG", fs.create(path));
//        return "/webhdfs/v1/images/" + imageId.toString() + "?op=OPEN";
//    }
}