package io.github.epi155.pm.sort;

/**
 * Record editor - UnaryFunction&lt;String&gt; replacement
 */
public interface RecordEditor {
    /**
     * Replace original record line
     * @param line original line
     * @return edited line
     */
    String apply(String line);
}
