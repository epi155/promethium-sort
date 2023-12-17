package io.github.epi155.pm.sort;

import java.io.File;
import java.util.Comparator;

/**
 * <p>
 * Utility for sorting large positional files, without having to load them entirely into memory.
 * The options are similar to the IBM DFSORT.
 * </p>
 * <p>
 * The simplest way to invoke sort is
 * </p>
 * <pre>
 *      SortEngine.using(256)       // Max Records in Memory
 *          .sortIn(sourceFile)
 *          .sort()                 // Comparator&lt;String&gt; can be added
 *          .sortOut(targetFile);
 * </pre>
 * <p>
 * The utility uses a DSL style, the available options, see <a href="#detail">detail</a>.
 * </p>
 * <p>
 * The file is split into small, sorted files.
 * Small files are taken in pairs and balanced, resulting in a sorted file with the same records as the pair.
 * This operation is repeated until a single ordered file is obtained.
 * The generated temporary files are deleted at each cycle.
 * Free disk space of at least twice the size of the original file is required.
 * </p>
 * <h2>Option <a id="detail">detail</a></h2>
 * <dl>
 *     <dt><b>{@link LayerSortIn#sortIn(File) sortIn}</b></dt>
 *     <dd>Set the file to be sorted.</dd>
 *
 *     <dt>{@link LayerSkipRecord#skipRecord(int) skipRecord}</dt>
 *     <dd>Sets the number of records to skip from the beginning of the file.</dd>
 *
 *     <dt>{@link LayerInclude#include(SortFilter) include}</dt>
 *     <dd>Set the condition to include (or discard) records.</dd>
 *
 *     <dt>{@link LayerStopAfter#stopAfter(int) stopAfter}</dt>
 *     <dd>Can be used to specify the maximum number of records you want the subtask for the input file to accept for sorting (accepted means read from the input file and not deleted by INCLUDE.</dd>
 *
 *     <dt>{@link LayerInRec#inRec(RecordEditor) inRec}</dt>
 *     <dd>Edit the input record before sorting.</dd>
 *
 *     <dt><b>{@link LayerSort#sort() sort()}</b></dt>
 *     <dd>Sort the records using natural order.</dd>
 *
 *     <dt><b>{@link LayerSort#sort(Comparator) sort(Comparator)}</b></dt>
 *     <dd>Sort the records using the given comparator.</dd>
 *
 *     <dt>{@link LayerPostSort#allDups() allDups}</dt>
 *     <dd>Limits the records selected to those with KEY-SORT values that occur more than once.</dd>
 *
 *     <dt>{@link LayerPostSort#first() first}</dt>
 *     <dd>Limits the records selected to those with KEY-SORT values that occur only once and the first record of those with KEY-SORT values that occur more than once.</dd>
 *
 *     <dt>{@link LayerPostSort#firstDup() firstDup}</dt>
 *     <dd>Limits the records selected to the first record of those with KEY-SORT values that occur more than once.</dd>
 *
 *     <dt>{@link LayerPostSort#last() last}</dt>
 *     <dd>Limits the records selected to those with KEY-SORT values that occur only once and the last record of those with KEY-SORT values that occur more than once.</dd>
 *
 *     <dt>{@link LayerPostSort#lastDup() lastDup}</dt>
 *     <dd>Limits the records selected to the last record of those with KEY-SORT values that occur more than once.</dd>
 *
 *     <dt>{@link LayerPostSort#noDups() noDups}</dt>
 *     <dd>Limits the records selected to those with KEY-SORT values that occur only once.</dd>
 *
 *     <dt>{@link LayerPostSort#sum(SumFields) sum}</dt>
 *     <dd>This control statement maps each group of records with equal KEY-SORT in their summary.</dd>
 *
 *     <dt>{@link LayerPostSort#reduce(RecordAccumulator) reduce}</dt>
 *     <dd>Accumulates a group of records into a single record using a custom accumulation method. The first, firstDup, last, lastDup , noDups and sum options are special cases of reduce.</dd>
 *
 *     <dt>{@link LayerOutRec#outRec(RecordEditor) outRec}</dt>
 *     <dd>Edit the output record after sorting.</dd>
 *
 *     <dt><b>{@link LayerSortOut#sortOut(File) sortOut}</b></dt>
 *     <dd>Set the sorted file.</dd>
 * </dl>
 */
public class SortEngine {
    private SortEngine() {
    }

    /**
     * Sort builder creator
     * @return instance of {@link SortBuilderRecord}
     */
    public static SortBuilderRecord builder() {
        return new PmSortBuilder();
    }

    /**
     * Set the sort context (charset = UTF-8; maxThread = nmCore / 2, temp = ${java.io.tmpdir})
     *
     * @param maxNumRecord max record sorted in memory
     * @return {@link LayerSortIn} instance
     */
    public static LayerSortIn using(int maxNumRecord) {
        return new PmSortEngine(maxNumRecord, null, 0,  null);
    }

}
