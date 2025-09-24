package com.whosly.stars.springboot2.webflux.controller;

import com.whosly.stars.springboot2.webflux.entry.TblEntry;
import com.whosly.stars.springboot2.webflux.service.ITblService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 */
@RestController
public class TblController implements IController {

	@Autowired
	private ITblService tblService;

	/**
	 * queryForList
	 *
	 * GET http://localhost:8060/list
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public Flux<TblEntry> queryForList(ServerWebExchange exchange) {
		return tblService.queryForListFlux();
	}

	/**
	 * query
	 *
	 * GET http://localhost:8060/query/2
	 */
	@RequestMapping(value = "/query/{id}", method = RequestMethod.GET)
	public Mono<TblEntry> query(@PathVariable("id") Long id, ServerWebExchange exchange) {
		return tblService.queryByIdFlux(id);
	}

	/**
	 * delete
	 *
	 * GET http://localhost:8060/delete/2
	 */
	@RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
	public Mono<Long> delete(@PathVariable("id") Long id, ServerWebExchange exchange) {
		return tblService.deleteByIdFlux(id);
	}

}
