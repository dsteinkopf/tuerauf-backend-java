package net.steinkopf.tuerauf.controller;


import net.steinkopf.tuerauf.data.AccessLog;
import net.steinkopf.tuerauf.repository.AccessLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;


/**
 * Show access logs.
 */
@Controller
@Secured({"ROLE_ADMIN"})
@RequestMapping(AccessLogController.ACCESS_LOG_URL)
public class AccessLogController {

    private static final Logger logger = LoggerFactory.getLogger(AccessLogController.class);


    public static final String ACCESS_LOG_URL = "/accessLog";
    public static final String ACCESS_LOG_VIEW = "accesslog";
    private static final int NUMBER_OF_LOGS_PER_PAGE = 20;


    @Autowired
    AccessLogRepository accessLogRepository;


    /**
     * Display access log.
     */
    @RequestMapping("/")
    public String accessLog(@RequestParam(value = "page", required = false, defaultValue = "0") int page,
                            Map<String, Object> model) {

        logger.trace("accessLog");

        Page<AccessLog> requestedPage = accessLogRepository.findAll(constructPageSpecification(page));
        model.put("page", requestedPage);

        return ACCESS_LOG_VIEW;
    }

    /**
     * Returns a new object which specifies the the wanted result page.
     *
     * @param pageIndex The index of the wanted result page
     * @return the page spec.
     */
    private Pageable constructPageSpecification(int pageIndex) {
        Pageable pageSpecification = new PageRequest(
                pageIndex,
                NUMBER_OF_LOGS_PER_PAGE,
                new Sort(Sort.Direction.DESC, "accessTimestamp")
        );
        return pageSpecification;
    }
}
