package com.whosly.stars.springboot2.webflux.service;

import com.whosly.stars.springboot2.webflux.dao.TblRepository;
import com.whosly.stars.springboot2.webflux.entry.TblEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class TblServiceImpl implements ITblService {

    @Autowired
    private TblRepository tblRepository;

    @Override
    public List<TblEntry> queryForList() {
        return tblRepository.findAll();
    }

    @Override
    public TblEntry queryById(Long id) {
        try {
            TblEntry entry =  tblRepository.getOne(id);

            return entry;
        } catch (Exception e){
            return null;
        }
    }

    @Override
    public Mono<TblEntry> queryByIdFlux(Long id) {
        return Mono.justOrEmpty(this.queryById(id));
    }

    @Override
    public Long deleteById(Long id) {
        try {
            tblRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            return 0L;
        }

        return id;
    }

    @Override
    public Mono<Long> deleteByIdFlux(Long id) {
//        return Mono.just(this.deleteById(id));
        // or
        return Mono.create(monoSink -> monoSink.success(this.deleteById(id)));
    }

    @Override
    public Flux<TblEntry> queryForListFlux() {
        return Flux.fromIterable(this.queryForList());
    }

}
