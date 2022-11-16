package io.github.epi155.pm.sort;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

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
    @NotNull LayerStopAfter include(@NotNull Predicate<String> test);
}
