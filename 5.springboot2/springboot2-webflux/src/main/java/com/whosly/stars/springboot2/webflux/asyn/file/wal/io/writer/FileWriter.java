package com.whosly.stars.springboot2.webflux.asyn.file.wal.io.writer;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileWriter implements IFileWriter {
    private final BufferedOutputStream writer;

    public FileWriter(String fileName) throws FileNotFoundException {
        this(fileName, true);
    }

    public FileWriter(String fileName, boolean append) throws FileNotFoundException {
        this(new File(fileName), append);
    }

    public FileWriter(File file) throws FileNotFoundException {
        this(file, true);
    }

    /**
     *
     * @param file
     * @param append   是否追加    true 为追加
     *
     * @throws FileNotFoundException
     */
    public FileWriter(File file, boolean append) throws FileNotFoundException {
        this.writer = new BufferedOutputStream(new FileOutputStream(file, append));
    }

    @Override
    public void write(int c) throws IOException {
        this.writer.write(c);
        flush();
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        this.writer.write(bytes);
        flush();
    }

    @Override
    public void write(String val) throws IOException {
        if(val == null){
            return;
        }

        this.writer.write(val.getBytes(StandardCharsets.UTF_8));
        flush();
    }

    @Override
    public void close() throws IOException {
        this.writer.close();
    }

    @Override
    public void flush() throws IOException {
        this.writer.flush();
    }
}
