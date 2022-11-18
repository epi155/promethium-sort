package io.github.epi155.pm.sort;

import org.jetbrains.annotations.NotNull;

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
    @NotNull LayerSort inRec(@NotNull RecordEditor inFcn);
}
