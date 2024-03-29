package io.github.epi155.pm.sort;

/**
 * Provides rules for summing records
 * <br>
 * <p>
 * Example Group Count: the key and its occurrences are provided in the output.
 * </p>
 * <pre>
 * class GroupCount extends SumFields {
 *     int count;
 *     private String keyOf(String line) { ... }
 *
 *     &#64;Override
 *     protected void reset() {
 *         count = 0;
 *     }
 *
 *     &#64;Override
 *     protected void add(String line) {
 *         count++;
 *     }
 *
 *     &#64;Override
 *     protected String getSummary(String line) {
 *         return keyOf(line)+String.format("%03d", count);
 *     }
 * }
 * </pre>
 */
public abstract class SumFields {
    private static class SumFieldsNone {
        private static final SumFields NONE_INSTANCE = new SumFields() {
            @Override
            protected void reset() {
                // dummy method
            }

            @Override
            protected void add(String line) {
                // dummy method
            }

            @Override
            protected String getSummary(String line) {
                return line;
            }
        };
    }

    /**
     * Provides a trivial implementation of the field accumulator rule
     *
     * <p>
     * The options
     * <code>.sum(SumFields.none())</code>
     * and
     * <code>{@link LayerPostSort#first() .first()}</code>
     * have the same effect.
     * </p>
     *
     * <p>
     *     The implementation is trivially:
     * </p>
     * <pre>
     * = new SumFields() {
     *     &#64;Override
     *     protected void reset() { }   // none
     *
     *     &#64;Override
     *     protected void add(String line) { } none
     *
     *     &#64;Override
     *     protected String getSummary(String line) {
     *         return line;
     *     }
     * };
     * </pre>
     *
     * @return {@link SumFields} instance for <i>none</i> rule
     */
    public static SumFields none() {
        return SumFieldsNone.NONE_INSTANCE;
    }

    /**
     * Resets the accumulator function
     */
    protected abstract void reset();

    /**
     * adds the supplied record to the accumulator function
     * @param line current record
     */
    protected abstract void add(String line);

    /**
     * Provides the summary (sum) of the accumulated records
     * @param line first record of the group with the same KEY-SORT
     * @return summary of the accumulated records
     */
    protected abstract String getSummary(String line);
}
