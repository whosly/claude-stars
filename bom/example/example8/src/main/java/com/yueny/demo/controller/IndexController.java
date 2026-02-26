package com.yueny.demo.controller;

import com.yueny.demo.controller.result.R;
import com.yueny.demo.service.BomInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * http://localhost:8080/
 */
@RestController
@Slf4j
public class IndexController {
    private static final Set<String> BOM_FILE_PATH = new HashSet<>(Arrays.asList(
            "../bom/bom8/pom.xml",
            "bom/bom8/pom.xml",
            "../../bom/bom8/pom.xml"
    ));

    @Autowired
    private BomInfoService bomInfoService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public Mono<R<Map<String, Object>>> list() {
        Map<String, Object> bomInfo = bomInfoService.getBomInfo(BOM_FILE_PATH);
        return Mono.just(R.ok(bomInfo));
    }
}
