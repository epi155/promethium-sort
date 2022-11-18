package io.github.epi155.pm.sort;

/**
 * Sort record filter - Predicate&lt;String&gt; replacement
 */
public interface SortFilter {
    /**
     * test if line must be included or not
     * @param line record line
     * @return true &rarr; include, false &rarr; omit
     */
    boolean test(String line);
}
