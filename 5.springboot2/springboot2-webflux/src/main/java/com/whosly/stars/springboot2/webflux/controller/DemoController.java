package com.whosly.stars.springboot2.webflux.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@RestController
public class DemoController implements IController {

	@Value("${spring.application.name}")
	private String applicationName;

	/**
	 * SpringMvc方式
	 *
	 * http://localhost:18080/mvc
	 */
	@RequestMapping(value = "/mvc", method = RequestMethod.GET)
	public String mvc(ServerWebExchange exchange) {
		try{
			TimeUnit.MILLISECONDS.sleep(50L);
		}catch (InterruptedException e){

		}

		String date = new SimpleDateFormat("HH:mm:ss").format(new Date());
		return applicationName + " SpringMvc方式 ok: " + date;
	}

	/**
	 * 函数式编程, 见 CHandler
	 *
	 * http://localhost:8060/flux
	 */
	@RequestMapping(value = "/flux", method = RequestMethod.GET)
	public Mono<String> reactor(ServerWebExchange exchange) {
		try{
			TimeUnit.MILLISECONDS.sleep(50L);
		}catch (InterruptedException e){

		}

		String date = new SimpleDateFormat("HH:mm:ss").format(new Date());

		return Mono.just("flux方式 ok: " + date);
	}

}
