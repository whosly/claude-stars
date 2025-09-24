package com.whosly.stars.springboot2.webflux.controller;

import com.whosly.stars.springboot2.webflux.asyn.file.wal.body.WALBlockBody;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.body.WALBlockResult;
import com.whosly.stars.springboot2.webflux.asyn.console.IAsynConsoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@RestController
public class SubmitToQueueReturnFutureController implements IController {
	private static final Logger logger = LoggerFactory.getLogger(SubmitToQueueReturnFutureController.class);

	@Autowired
	private IAsynConsoleService asynService;

	/**
	 * submit
	 *
	 * http://localhost:8060/submitToQueueReturnFuture
	 */
	@RequestMapping(value = "/submitToQueueReturnFuture", method = RequestMethod.GET)
	public Mono<String> submitToQueueReturnFuture(ServerWebExchange exchange) {
		logger.info("submit");
		CompletableFuture<WALBlockResult> future =  asynService.submit(
				WALBlockBody.builder()
						.processId("name")
						.sql(18 + "")
						.build());
		logger.info("get CompletableFuture, await...");

		try {
			WALBlockResult respResult = future.get();
			logger.info("get CompletableFuture, respResult:{}." + respResult);

			return Mono.just("flux方式 ok: " + respResult);
		} catch (Exception e) {
			return Mono.just("flux方式 ok: " + e.getMessage());
		}
	}

}
