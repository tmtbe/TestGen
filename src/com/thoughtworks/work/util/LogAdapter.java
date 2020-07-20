package com.thoughtworks.work.util;

import com.intellij.openapi.diagnostic.Logger;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;

/**
 * this class performs adaption between velocity and the logger we have wrapped
 *
 * @author Jon Osborn
 * @since 1/3/12 4:28 PM
 */
public class LogAdapter implements LogChute {

    @Override
    public void init(RuntimeServices runtimeServices) throws Exception {
        //nothing to init
    }

    @Override
    public void log(int i, String s) {
        Logger log = JUnitGeneratorUtil.getLogger(LogAdapter.class);

        switch (i) {
            case LogChute.TRACE_ID:
            case LogChute.DEBUG_ID:
                log.debug(s);
                break;
            case LogChute.INFO_ID:
                log.info(s);
                break;
            case LogChute.WARN_ID:
                log.warn(s);
                break;
            case LogChute.ERROR_ID:
                log.error(s);
                break;
            default:
                log.warn(s);
        }
    }

    @Override
    public void log(int i, String s, Throwable throwable) {
        Logger log = JUnitGeneratorUtil.getLogger(LogAdapter.class);

        switch (i) {
            case LogChute.TRACE_ID:
            case LogChute.DEBUG_ID:
                log.debug(s, throwable);
                break;
            case LogChute.INFO_ID:
                log.info(s, throwable);
                break;
            case LogChute.WARN_ID:
                log.warn(s, throwable);
                break;
            case LogChute.ERROR_ID:
                log.error(s, throwable);
                break;
            default:
                log.warn(s, throwable);
        }
    }

    @Override
    public boolean isLevelEnabled(int i) {
        return true;
    }
}
