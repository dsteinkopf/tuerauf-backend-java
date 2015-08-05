package net.steinkopf.tuerauf.service;


import net.steinkopf.tuerauf.data.AccessLog;
import net.steinkopf.tuerauf.data.User;
import net.steinkopf.tuerauf.repository.AccessLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Date;


/**
 * Manipulation of AccessLogs.
 */
@Service
public class AccessLogService {

    @Autowired
    AccessLogRepository accessLogRepository;


    /**
     * Creates a new access log entry.
     *
     * @return the new entry.
     */
    public AccessLog log(User user,
                         AccessLog.AccessType accessType,
                         @Nullable Double geoy,
                         @Nullable Double geox,
                         @Nullable String result) {

        AccessLog accessLog = new AccessLog();

        accessLog.setAccessTimestamp(new Date());
        accessLog.setUser(user);
        accessLog.setAccessType(accessType);
        accessLog.setGeoy(geoy);
        accessLog.setGeox(geox);
        accessLog.setResult(result);

        accessLogRepository.save(accessLog);

        return accessLog;
    }
}
