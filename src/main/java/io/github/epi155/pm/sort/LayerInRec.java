package io.github.epi155.pm.sort;

/**
 * Layer to set INREC option and define sorting
 */
public interface LayerInRec extends LayerSort {
    /**
     * Edit the input record before sorting
     *
     * @param inFcn edit function
     * @return {@link LayerSort} instance
     */
    LayerSort inRec(RecordEditor inFcn);
}
