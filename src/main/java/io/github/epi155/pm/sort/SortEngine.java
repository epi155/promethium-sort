package io.github.epi155.pm.sort;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Comparator;

/**
 * Sort Factory
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
 * The utility uses a DSL style, the available options are:
 * {@link LayerSortIn#sortIn(File) sortIn},
 * {@link LayerSkipRecord#skipRecord(int) skipRecord},
 * {@link LayerInclude#include(SortFilter) include},
 * {@link LayerStopAfter#stopAfter(int) stopAfter},
 * {@link LayerInRec#inRec(RecordEditor) inRec},
 * {@link LayerSort#sort() sort} (natural),
 * {@link LayerSort#sort(Comparator) sort }(custom),
 * {@link LayerPostSort#allDups() allDups},
 * {@link LayerPostSort#first() first},
 * {@link LayerPostSort#firstDup() firstDup},
 * {@link LayerPostSort#last() last},
 * {@link LayerPostSort#lastDup() lastDup} ,
 * {@link LayerPostSort#noDups() noDups},
 * {@link LayerPostSort#sum(SumFields) sum} 
 * {@link LayerPostSort#reduce(RecordAccumulator) <i>reduce</i>},
 * {@link LayerOutRec#outRec(RecordEditor) outRec},
 * {@link LayerSortOut#sortOut(File) sortOut}.
 * </p>
 * <p>
 * The file is split into small, sorted files.
 * Small files are taken in pairs and balanced, resulting in a sorted file with the same records as the pair.
 * This operation is repeated until a single ordered file is obtained.
 * The generated temporary files are deleted at each cycle.
 * Free disk space of at least twice the size of the original file is required.
 * </p>
 */
public class SortEngine {
    private SortEngine() {
    }

    /**
     * Set the sort context
     *
     * @param maxNumRecord max record sorted in memory
     * @param charset      file charset
     * @param maxThread    max thread for merge split file
     * @return {@link LayerSortIn} instance
     */
    public static LayerSortIn using(int maxNumRecord, Charset charset, int maxThread) {
        return new PmSortEngine(maxNumRecord, charset, maxThread);
    }

    /**
     * Set the sort context (maxThread = nmCore / 2)
     *
     * @param maxNumRecord max record sorted in memory
     * @param charset      file charset
     * @return {@link LayerSortIn} instance
     */
    public static LayerSortIn using(int maxNumRecord, Charset charset) {
        return new PmSortEngine(maxNumRecord, charset);
    }

    /**
     * Set the sort context (charset = UTF-8; maxThread = nmCore / 2)
     *
     * @param maxNumRecord max record sorted in memory
     * @return {@link LayerSortIn} instance
     */
    public static LayerSortIn using(int maxNumRecord) {
        return new PmSortEngine(maxNumRecord);
    }
}
