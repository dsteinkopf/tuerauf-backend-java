package net.steinkopf.tuerauf.util;

import org.mockito.listeners.InvocationListener;
import org.slf4j.Logger;

/**
 * Misc utils.
 */
public class Utils {

    public static InvocationListener getLoggingInvocationListener(Logger logger) {
        return methodInvocationReport -> logger.trace(methodInvocationReport.getInvocation().toString().replace("\n", " "));
    }
}
