package io.github.epi155.pm.sort;

import java.nio.charset.Charset;

/**
 * Partial Builder (Charset)
 */
public interface SortBuilderCharset extends SortBuilderThread {
    /**
     * Set file charset (default UTF-8)
     *
     * @param charset file charset
     * @return instance of {@link SortBuilderThread}
     */
    SortBuilderThread withCharset(Charset charset);
}
