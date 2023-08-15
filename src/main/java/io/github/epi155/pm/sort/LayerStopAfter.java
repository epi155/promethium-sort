package io.github.epi155.pm.sort;

/**
 * Layer to set STOPAFT, INREC options and define sorting
 */
public interface LayerStopAfter extends LayerInRec {
    /**
     * Can be used to specify the maximum number of records you want the
     * subtask for the input file to accept for sorting (accepted means read
     * from the input file and not deleted by INCLUDE
     *
     * @param nmStop maximum number of records
     * @return {@link LayerInRec} instance
     */
    LayerInRec stopAfter(int nmStop);
}
