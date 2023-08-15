package io.github.epi155.pm.sort;

/**
 * Layer to set SKIPREC, INCLUDE, STOPAFT, INREC options and define sorting
 */
public interface LayerSkipRecord extends LayerInclude {
    /**
     * Sets the number of records to skip from the beginning of the file
     *
     * @param nmSkip number of records to skip
     * @return {@link LayerInclude} instance
     */
    LayerInclude skipRecord(int nmSkip);
}
