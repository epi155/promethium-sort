package io.github.epi155.pm.sort;

import java.io.IOException;

/**
 * Generic Sort Exception
 */
public class SortException extends RuntimeException {
    /**
     * @param e       parent IOException
     * @param pattern error message pattern
     * @param objects error message parameters
     */
    public SortException(IOException e, String pattern, Object... objects) {
        super(String.format(pattern, objects), e);
    }
}
