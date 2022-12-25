package io.github.epi155.pm.sort;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Aggregate (and edit) record
 * <br>
 * <p>
 * Example Group Count: the key and its occurrences are provided in the output.
 * </p>
 * <pre>
 * class GroupCount implements RecordAccumulator {
 *     private int count = 0;
 *     private String cacheKey = null;
 *     private String keyOf(String line) { ... }
 *
 *     &#64;Override
 *     public String reduce(String line) {
 *         String out;
 *         String key = keyOf(line);
 *         if (cacheKey == null) {
 *             out = null;
 *             cacheKey = key;
 *         } else if (cacheKey.compareTo(key) == 0) {
 *             out = null;
 *         } else {
 *             out = cacheKey+String.format("%03d", count);
 *             count = 0;
 *             cacheKey = key;
 *         }
 *         count++;
 *         return out;
 *     }
 *
 *     &#64;Override
 *     public String flush() {
 *         return cacheKey+String.format("%03d", count);
 *     }
 * }
 * </pre>
 */
public interface RecordAccumulator {
    /**
     * Performs a reduction on the elements of the records,
     * using some associative accumulation function,
     * and returns an original, or edited, record with the reduced value,
     * or null.
     *
     * @param line  original record
     * @return      original, or edited, record, or null
     */
    @Nullable
    String reduce(@NotNull String line);

    /**
     * returns any value left in the accumulation cache or null.
     *
     * @return      original, or edited, record, or null
     */
    @Nullable
    String flush();
}
