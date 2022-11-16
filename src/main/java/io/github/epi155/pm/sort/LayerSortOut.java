package io.github.epi155.pm.sort;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Layer to define the output file
 */
public interface LayerSortOut {
    /**
     * Set the sorted file
     *
     * @param sortedFile sorted file
     */
    void sortOut(@NotNull File sortedFile);
}
