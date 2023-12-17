package io.github.epi155.pm.sort;

/**
 * Final sort builder
 */
public interface SortBuilder {
    /**
     * Provides the sort context
     *
     * @return {@link LayerSortIn} instance
     */
    LayerSortIn build();
}
