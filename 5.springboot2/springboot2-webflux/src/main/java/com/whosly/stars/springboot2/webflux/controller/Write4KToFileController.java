package com.whosly.stars.springboot2.webflux.controller;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.date.SystemClock;
import com.google.common.io.Files;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.body.WALBlockBody;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.body.WALBlockResult;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.IWalFile;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.IFileHandler;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.handler.WALCompatibilityAction;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.io.read.FileReader;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.fs.blocks.WALBlocksLog;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 每次写文件， 4 k 写入
 */
@RestController
public class Write4KToFileController implements IController {
	private static final Logger logger = LoggerFactory.getLogger(Write4KToFileController.class);

	@Autowired
	private IWalFile walFile;

	/**
	 *
	 * http://localhost:8060/4k
	 */
	@RequestMapping(value = "/4k", method = RequestMethod.GET)
	public Mono<String> k_4(ServerWebExchange exchange) {
		Long tableId = 2L;

		long st1 = SystemClock.now();
		Optional<IFileHandler> fileHandlerOptional = walFile.open(1L, tableId);
		logger.info("try open 时间:{} ms.", SystemClock.now() - st1);

		if(!fileHandlerOptional.isPresent()){
			return Mono.just("文件不存在！");
		}

		IFileHandler fileHandler = fileHandlerOptional.get();

		StopWatch stopWatch = new StopWatch();
		//  内容随机
		for (int i = 0; i < 2000; i++) {
			stopWatch.start("" + i);

			WALBlockBody reqData = WALBlockBody.builder()
					.processId("p1 name" + i)
					.sql("create table SaleOrder\n" +
							"(\n" +
							"    id 　　　　　　int identity(1," + i + "),\n" +
							"    OrderNumber  int　　　　　　　　 ,\n" +
							"    CustomerId   varchar(20)      ,\n" +
							"    OrderDate    datetime         ,\n" +
							"    Remark       varchar(200)\n" +
							")\n")
					.build();

			long ct1 = SystemClock.now();
			CompletableFuture<WALBlockResult> future = fileHandler.commit(reqData);
			long ct2 = SystemClock.now();

			try {
				WALBlockResult rs = future.get();
				long end = SystemClock.now();
				logger.info("get {} WALBlockResult 时间(ms): {}/{}.",
						i, ct2 - ct1, end - ct1);

				logger.debug("rs:{}.", rs);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}

			stopWatch.stop();
		}
		String rs = stopWatch.prettyPrint();
		System.out.println("总耗时(ms):" + stopWatch.getTotalTimeMillis() +
				", io 耗时(ms):" + fileHandler.getIOCostTime());

//		walFile.close(tableId);

		return Mono.just(rs);
	}

	/**
	 *
	 * http://localhost:8060/4k/reader
	 */
	@RequestMapping(value = "/4k/reader", method = RequestMethod.GET)
	public Mono<String> k_4_reader(ServerWebExchange exchange) {
		String currentFile = WALCompatibilityAction.getCurrentFilePath(1L, 2L);

		// 读 currentFile 取 fileName
		String fileName = "";
		try{
			List<String> lines =  Files.readLines(new File(currentFile), Charset.forName("utf-8"));

			if(CollectionUtils.isNotEmpty(lines)){
				fileName = lines.get(0);
			}
		}catch (IOException ex){  // 没有 CURRENT 文件
			ex.printStackTrace();
		}

		String filePath = WALCompatibilityAction.getFilePath(1L, 2L, fileName);

		try{
			byte[] fileData = FileReader.readAll(filePath);
			WALBlocksLog blocks = WALBlocksLog.fromBytes(fileData);

			return Mono.just("文件data:" + blocks);
		}catch (IOException ex){
			return Mono.just("文件data:" + ex.getMessage());
		}
	}

}
