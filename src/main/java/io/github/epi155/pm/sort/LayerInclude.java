package io.github.epi155.pm.sort;

/**
 * Layer to set INCLUDE, STOPAFT, INREC options and define sorting
 */
public interface LayerInclude extends LayerStopAfter {
    /**
     * Set the condition to include (or discard) records
     *
     * @param test condition to include
     * @return {@link LayerStopAfter} instance
     */
    LayerStopAfter include(SortFilter test);
}
