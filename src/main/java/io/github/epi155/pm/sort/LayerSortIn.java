package io.github.epi155.pm.sort;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Layer to set the name of the input file
 */
public interface LayerSortIn {
    /**
     * Set the file to be sorted.
     *
     * @param unsortedFile file to be sorted
     * @return {@link LayerSkipRecord} instance
     */
    @NotNull LayerSkipRecord sortIn(@NotNull File unsortedFile);
}
