package io.github.epi155.pm.sort;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

/**
 * Layer to define sorting
 */
public interface LayerSort {
    /**
     * Sort the records using the given comparator
     *
     * @param comparator sort comparator
     * @return {@link LayerOutRec} instance
     */
    @NotNull LayerOutRec sort(@NotNull Comparator<String> comparator);
}
