package io.github.epi155.pm.sort;

/**
 * Layer to set the ALLDUPS, NODUPS, FIRST, OUTREC options and define the output file
 */
public interface LayerPostSort extends LayerOutRec {
    /**
     * Limits the records selected to those with KEY-SORT values that occur more than once.
     * <p>
     *     You can use this operand to keep just those records with duplicate field values.
     * </p>
     *
     * @return {@link LayerOutRec} instance
     */
    LayerOutRec allDups();

    /**
     * Limits the records selected to those with KEY-SORT values that occur only once.
     * <p>
     *     You can use this operand to keep just those records with no duplicate field values.
     * </p>
     *
     * @return {@link LayerOutRec} instance
     */
    LayerOutRec noDups();

    /**
     * Limits the records selected to those with KEY-SORT values that occur only once
     * and the first record of those with KEY-SORT values that occur more than once.
     * <p>
     *     You can use this operand to keep just the first record for each <b>unique</b> field value.
     * </p>
     *
     * @return {@link LayerOutRec} instance
     */
    LayerOutRec first();
}
