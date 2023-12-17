package io.github.epi155.pm.sort;

/**
 * Partial sort builder (Max Record)
 */
public interface SortBuilderRecord extends SortBuilderCharset {
    /**
     * Set max records to load in memory (default 2048)
     *
     * @param maxRecord max records to load in memory
     * @return instance of {@link SortBuilderCharset}
     */
    SortBuilderCharset withMaxRecord(int maxRecord);
}
