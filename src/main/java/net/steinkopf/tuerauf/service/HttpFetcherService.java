package net.steinkopf.tuerauf.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Logging and sending admin mails.
 */
@Service
public class HttpFetcherService {

    private static final Logger logger = LoggerFactory.getLogger(HttpFetcherService.class);


    @SuppressWarnings("SpellCheckingInspection")
    public String fetchFromUrl(final String url, final int maxLen) throws IOException {

        String loggableUrl = StringUtils.replacePattern(url, "storepinlist.*:", "storepinlist?PWHIDDEN:");
        loggableUrl = StringUtils.replacePattern(loggableUrl, "/[0-9][0-9][0-9][0-9]/", "/0000/");
        logger.debug("fetchFromUrl({})", loggableUrl);
        return readStream(new URL(url).openStream(), maxLen);
    }

    private String readStream(final InputStream inputStream, final int maxLen) throws IOException {

        // maybe this could be more efficient, but arduino's answers are always short.
        final byte[] bytes = new byte[maxLen];
        int got = 0;
        int read;
        do {
//            System.out.println("readStream offset " + got + ", thread: " + Thread.currentThread().getId());
            read = inputStream.read(bytes, got, maxLen - got);
            if (read > 0) {
                got += read;
            }
        }
        while (got < maxLen && read != -1);
        return new String(bytes, 0, got, Charset.forName("UTF-8"));
    }
}
