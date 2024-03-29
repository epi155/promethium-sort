package io.github.epi155.pm.sort;

import java.io.File;

/**
 * Partial builder (temporary directory)
 */
public interface SortBuilderWork extends SortBuilder {
    /**
     * Set directory for temporary files (default ENV {@code java.io.tmpdir})
     *
     * @param tempDirectory temp Directory
     * @return instance of {@link SortBuilder}
     */
    SortBuilder withTempDirectory(File tempDirectory);

    /**
     * Set directory for temporary files (default ENV {@code java.io.tmpdir})
     *
     * @param tempDirectory temp Directory
     * @return instance of {@link SortBuilder}
     */
    SortBuilder withTempDirectory(String tempDirectory);
}
