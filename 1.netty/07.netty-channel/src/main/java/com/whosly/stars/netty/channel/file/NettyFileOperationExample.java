package com.whosly.stars.netty.channel.file;

import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * @author fengyang
 * @date 2025-08-21 14:10:46
 * @description
 */
public class NettyFileOperationExample {
    public static String SOURCE_FILE = "enc_meta_for_cust.yml";
    public static String TARGET_FILE = "enc_meta_for_cust_copy.yml";

    //  功能目标： 将文件 enc_meta_for_cust.yml 的内容，写入到 enc_meta_for_cust_copy.yml 中
	public static void main(String[] args) throws Exception {
		// 创建Netty事件循环组
		EventLoopGroup group = new NioEventLoopGroup();

		try {
			// 获取事件循环用于执行异步操作
			EventLoop eventLoop = group.next();

			// 使用 CompletableFuture 处理异步文件操作
			CompletableFuture.supplyAsync(() -> {
				try {
					return readFile();
				} catch (Exception e) {
					throw new CompletionException(e);
				}
			}, eventLoop)
			.thenAcceptAsync(result -> {
				writeToFile(eventLoop, result);
			}, eventLoop)
			.thenRun(() -> System.out.println("所有文件操作完成"))
			.exceptionally(throwable -> {
				System.err.println("文件操作失败: " + throwable.getMessage());
				throwable.printStackTrace();
				return null;
			});

			// 等待操作完成（实际应用中会有更优雅的关闭机制）
			Thread.sleep(5000);
		} finally {
			// 优雅关闭事件循环组
			group.shutdownGracefully().sync();
		}
	}

	/**
	 * 读取文件并返回读取结果（文件大小与源路径）
	 */
	private static ReadResult readFile() throws IOException, URISyntaxException {
		Path sourcePath = Paths.get(NettyFileOperationExample.class.getClassLoader()
				.getResource(SOURCE_FILE).toURI());
		try (FileChannel inChannel = FileChannel.open(sourcePath, StandardOpenOption.READ)) {

			System.out.println("开始读取文件...");
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			int bytesRead;
			long fileSize = inChannel.size();

			// 读取文件内容
			while ((bytesRead = inChannel.read(buffer)) > 0) {
				buffer.flip();
				byte[] data = new byte[bytesRead];
				buffer.get(data);
				System.out.println("读取到内容: " + new String(data));
				buffer.clear();
			}

			System.out.println("文件读取完成");
			return new ReadResult(fileSize, sourcePath);
		}
	}

	/**
	 * 写入文件：将源文件内容复制到相同目录下的新文件中
	 */
	private static void writeToFile(EventLoop eventLoop, ReadResult result) {
		try {
			Path sourcePath = result.sourcePath;
			Path parentDir = sourcePath.getParent();
			String srcName = sourcePath.getFileName().toString();
			String targetName = TARGET_FILE;
			Path targetPath = parentDir.resolve(targetName);

			// 复制源文件内容到目标文件（存在则覆盖）
			Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

			System.out.println("文件写入完成 -> " + targetPath.toString() +
					"（原文件大小: " + result.fileSize + " bytes）");
		} catch (Exception e) {
			throw new CompletionException(e);
		}
	}

	private static final class ReadResult {
		final long fileSize;
		final Path sourcePath;

		ReadResult(long fileSize, Path sourcePath) {
			this.fileSize = fileSize;
			this.sourcePath = sourcePath;
		}
	}
}
