package net.steinkopf.tuerauf.util;

import org.mockito.listeners.InvocationListener;
import org.slf4j.Logger;

/**
 * Misc utils.
 */
public class Utils {

    public static InvocationListener getLoggingMockInvocationListener(Logger logger) {
        return methodInvocationReport -> logger.trace("mock {} returns {};",
                methodInvocationReport.getInvocation().toString().replace("\n", " "),
                methodInvocationReport.getReturnedValue());
    }
}
