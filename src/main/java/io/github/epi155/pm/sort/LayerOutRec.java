package io.github.epi155.pm.sort;

import org.jetbrains.annotations.NotNull;

/**
 * Layer to set the OUTREC option and define the output file
 */
public interface LayerOutRec extends LayerSortOut {
    /**
     * Edit the output record after sorting
     *
     * @param outFcn edit function
     * @return {@link LayerSortOut} instance
     */
    @NotNull LayerSortOut outRec(@NotNull RecordEditor outFcn);
}
