package com.whosly.stars.springboot2.webflux.asyn.file.wal.io.writer;

import java.io.IOException;

public interface IFileWriter {

    void write(int c) throws IOException;

    void write(byte[] bytes) throws IOException;

    void write(String val) throws IOException;

    void close() throws IOException;

    void flush() throws IOException;

}
