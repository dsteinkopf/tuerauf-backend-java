package net.steinkopf.tuerauf.util;

import org.mockito.listeners.InvocationListener;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;

import java.nio.charset.Charset;
import java.util.Base64;

/**
 * Misc utils for tests.
 */
public class TestUtils {

    public static InvocationListener getLoggingMockInvocationListener(Logger logger) {
        return methodInvocationReport -> logger.trace("mock {} returns {};",
                methodInvocationReport.getInvocation().toString().replace("\n", " "),
                methodInvocationReport.getReturnedValue());
    }

    public static void addBasicAuthHeader(final HttpHeaders headers, final String password, final String user) {
        if (user != null && password !=  null) {
            byte[] authString = (user + ":" + password).getBytes(Charset.defaultCharset());
            byte[] authStringBase64 = Base64.getEncoder().encode(authString);
            headers.set(HttpHeaders.AUTHORIZATION, "Basic " + new String(authStringBase64));
        }
    }
}
