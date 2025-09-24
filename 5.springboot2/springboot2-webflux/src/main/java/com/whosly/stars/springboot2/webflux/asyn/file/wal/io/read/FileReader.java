package com.whosly.stars.springboot2.webflux.asyn.file.wal.io.read;

import java.io.*;

public class FileReader {

    public static final byte[] readAll(String filePath) throws IOException {
        return readAll(new File(filePath));
    }

    public static final byte[] readAll(File file) throws IOException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(file)))
        {
            //当文件没有结束时，每次读取一个字节显示
            byte[] data = new byte[in.available()];
            in.read(data);

            return data;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

}
