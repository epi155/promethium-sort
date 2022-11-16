package io.github.epi155.pm.sort;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;

import java.io.IOException;

/**
 * Generic Sort Exception
 */
@Slf4j
public class SortException extends RuntimeException {
    /**
     * @param e       parent IOException
     * @param pattern error message pattern
     * @param objects error message parameters
     */
    public SortException(IOException e, String pattern, Object... objects) {
        super(MessageFormatter.arrayFormat(pattern, objects).getMessage(), e);
        log.error(getMessage(), this);
    }
}
