package com.linkedin.replica.mediaServer;

import com.linkedin.replica.mainServer.config.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

public class MediaClient {


    public static String writeFile(File file) throws IOException, URISyntaxException {
        org.apache.hadoop.conf.Configuration config = new org.apache.hadoop.conf.Configuration();
        Configuration configuration = Configuration.getInstance();
        System.setProperty("HADOOP_USER_NAME", configuration.getAppConfigProp("hadoop.username"));

        FileInputStream fileInputStreamReader = new FileInputStream(file);
        InputStream in = new BufferedInputStream(fileInputStreamReader);
        String hadoopPath = String.format("hdfs://%s:%s",
                configuration.getAppConfigProp("hadoop.ip"),
                configuration.getAppConfigProp("hadoop.port"));

        FileSystem hadoopFS = FileSystem.get(new URI(hadoopPath), config);
        UUID fileId = UUID.randomUUID();
        Path path = new Path(hadoopPath + "/media/" + fileId.toString());
        OutputStream out = hadoopFS.create(path);
        IOUtils.copyBytes(in, out, 4096, true);
        return "/webhdfs/v1/media/" + fileId.toString() + "?op=OPEN";
    }
}