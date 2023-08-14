package io.github.epi155.pm.sort;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

class PmSortEngine implements LayerSortIn {
    private static final String PRFX = "sort-";
    private static final String SUFX = ".tmp";
    private final int maxNumRecord;
    private final Charset charset;
    private final int maxThread;

    public PmSortEngine(int maxNumRecord, Charset charset, int maxThread) {
        this.maxNumRecord = maxNumRecord;
        this.charset = charset;
        this.maxThread = maxThread;
    }

    public PmSortEngine(int maxNumRecord, Charset charset) {
        this(maxNumRecord, charset, Math.max(1, Runtime.getRuntime().availableProcessors() / 2));
    }

    public PmSortEngine(int maxNumRecord) {
        this(maxNumRecord, StandardCharsets.UTF_8);
    }

    @Override
    public @NotNull LayerSkipRecord sortIn(@NotNull File unsortedFile) {
        return this.new PmSortIn(unsortedFile);
    }

    private class PmSortIn extends PmLayerSort implements LayerSkipRecord {
        private final File source;
        private RecordEditor inRecFcn = null;
        private SortFilter includeFilter = null;
        private int nmSkip = 0;
        private int nmStop = Integer.MAX_VALUE;

        public PmSortIn(File unsortedFile) {
            this.source = unsortedFile;
        }

        @Override
        public @NotNull LayerSort inRec(@NotNull RecordEditor inFcn) {
            this.inRecFcn = inFcn;
            return this;
        }

        @Override
        public @NotNull LayerStopAfter include(@NotNull SortFilter test) {
            this.includeFilter = test;
            return this;
        }

        @Override
        public @NotNull LayerInclude skipRecord(int nmSkip) {
            this.nmSkip = nmSkip;
            return this;
        }

        @Override
        public @NotNull LayerPostSort sort(@NotNull Comparator<String> comparator) {
            return this.new PmSort(comparator);
        }

        @Override
        public @NotNull LayerInRec stopAfter(int nmStop) {
            this.nmStop = nmStop;
            return this;
        }

        private class PmSort implements LayerPostSort {
            private final Comparator<String> comparator;
            private RecordEditor outRecFcn = null;
            private File target;
            private RecordAccumulator reorgWriter = null;

            public PmSort(Comparator<String> comparator) {
                this.comparator = comparator;
            }

            @Override
            public @NotNull LayerSortOut outRec(@NotNull RecordEditor outFcn) {
                this.outRecFcn = outFcn;
                return this;
            }

            @Override
            public void sortOut(@NotNull File sortedFile) {
                this.target = sortedFile;
                List<File> tempFiles = split();
                if (tempFiles.size() > 1) {
                    execMerge(tempFiles);
                }
            }

            private void execMerge(List<File> tempFiles) {
                ExecutorService mergerPool = Executors.newFixedThreadPool(maxThread);
                try {
                    merge(mergerPool, tempFiles);
                } finally {
                    mergerPool.shutdown();
                }
            }

            private void merge(ExecutorService mergerPool, @NotNull List<File> tempFiles) {
                List<File> mergeFiles = new ArrayList<>();
                Phaser phaser = new Phaser();
                if (tempFiles.size() == 2) {
                    MergeTask mergeTask = this.new FinalMergeTask(tempFiles.get(0), tempFiles.get(1), phaser);
                    mergerPool.submit(mergeTask);
                } else {
                    Queue<File> queue = new LinkedList<>(tempFiles);
                    while (!queue.isEmpty()) {
                        File sx = queue.poll();
                        File dx = queue.poll();
                        if (dx == null) {
                            mergeFiles.add(sx);
                        } else {
                            try {
                                File cx = File.createTempFile(PRFX, SUFX);
                                cx.deleteOnExit();
                                mergeFiles.add(cx);
                                MergeTask mergeTask = this.new MergeTask(sx, dx, cx, phaser);
                                mergerPool.submit(mergeTask);
                            } catch (IOException e) {
                                throw new SortException(e, "Temporary files error");
                            }
                        }
                    }
                }
                phaser.awaitAdvance(0);
                if (!mergeFiles.isEmpty()) {
                    merge(mergerPool, mergeFiles);
                }
            }

            private List<File> split() {
                long nmRecRd = 0;
                long nmRecWr = 0;
                Splitter splitter = this.new Splitter();
                try (BufferedReader br = Files.newBufferedReader(source.toPath(), charset)) {
                    String line;
                    while (nmRecWr < nmStop && (line = br.readLine()) != null) {
                        if (line.length() == 1 && line.charAt(0) == '\u001a')
                            break;
                        nmRecRd++;
                        if (nmRecRd > nmSkip &&
                            splitter.process(line)) {
                            nmRecWr++;
                        }
                    }
                    return splitter.files();
                } catch (IOException e) {
                    throw new SortException(e, "Error reading the file %s", source.getAbsolutePath());
                }
            }

            @Override
            public LayerOutRec allDups() {
                this.reorgWriter = this.new AllDupsReorgWriter();
                return this;
            }

            @Override
            public LayerOutRec noDups() {
                this.reorgWriter = this.new NoDupsReorgWriter();
                return this;
            }

            @Override
            public LayerOutRec first() {
                this.reorgWriter = this.new FirstReorgWriter();
                return this;
            }

            @Override
            public LayerOutRec last() {
                this.reorgWriter = this.new LastReorgWriter();
                return this;
            }

            @Override
            public LayerOutRec firstDup() {
                this.reorgWriter = this.new FirstDupReorgWriter();
                return this;
            }

            @Override
            public LayerOutRec lastDup() {
                this.reorgWriter = this.new LastDupReorgWriter();
                return this;
            }

            @Override
            public LayerOutRec sum(final SumFields rule) {
                class SumFiledsRule extends SumFields implements RecordAccumulator {
                    private String cache = null;

                    @Nullable
                    @Override
                    public String reduce(@NotNull String line) {
                        String out;
                        if (cache == null ) {
                            out = null;
                            cache = line;
                            reset();
                        } else if (comparator.compare(cache, line) == 0) {
                            out = null;
                        } else {
                            out = getSummary(cache);
                            reset();
                            cache = line;
                        }
                        add(line);
                        return out;
                    }

                    @Nullable
                    @Override
                    public String flush() {
                        return getSummary(cache);
                    }

                    @Override
                    protected void reset() {
                        rule.reset();
                    }

                    @Override
                    protected void add(@NotNull String line) {
                        rule.add(line);
                    }

                    @NotNull
                    @Override
                    protected String getSummary(@NotNull String line) {
                        return rule.getSummary(line);
                    }
                }
                this.reorgWriter = new SumFiledsRule();
                return this;
            }

            @Override
            public LayerOutRec reduce(RecordAccumulator accumulator) {
                this.reorgWriter = accumulator;
                return this;
            }

            private class MergeTask implements Runnable {
                private final File src1;
                private final File src2;
                private final File dest;
                private final Phaser phaser;

                private MergeTask(File src1, File src2, File dest, Phaser phaser) {
                    this.src1 = src1;
                    this.src2 = src2;
                    this.dest = dest;
                    this.phaser = phaser;

                    this.phaser.register();
                }

                @Override
                public void run() {
                    try {
                        performMerge();
                        try {
                            Files.delete(src1.toPath());
                        } catch (IOException e) {
                            throw new SortException(e, "Error deleting temporary file %s", src1.getAbsolutePath());
                        }
                        try {
                            Files.delete(src2.toPath());
                        } catch (IOException e) {
                            throw new SortException(e, "Error deleting temporary file %s", src2.getAbsolutePath());
                        }
                    } finally {
                        phaser.arriveAndDeregister();
                    }
                }

                public void performMerge() {
                    try (
                        BufferedReader br1 = Files.newBufferedReader(src1.toPath(), charset);
                        BufferedReader br2 = Files.newBufferedReader(src2.toPath(), charset);
                        BufferedWriter wrt = Files.newBufferedWriter(dest.toPath(), charset)
                    ) {
                        String line1 = br1.readLine();
                        String line2 = br2.readLine();
                        while (line1 != null || line2 != null) {
                            if (line1 == null) {
                                // end-of-file 1
                                writeLn(wrt, line2);
                                line2 = br2.readLine();
                            } else if (line2 == null) {
                                // end-of-file 2
                                writeLn(wrt, line1);
                                line1 = br1.readLine();
                            } else {
                                int comp = comparator.compare(line1, line2);
                                if (comp <= 0) {    // sort stability: File_k < File_{k+1} !!
                                    writeLn(wrt, line1);
                                    line1 = br1.readLine();
                                } else /* comp > 0 */ {
                                    writeLn(wrt, line2);
                                    line2 = br2.readLine();
                                }
                            }
                        }
                        flush(wrt);
                    } catch (IOException e) {
                        throw new SortException(e, "Error merging file %s,%s -> %s",
                            src1.getAbsolutePath(),
                            src2.getAbsolutePath(),
                            dest.getAbsolutePath());
                    }
                }

                protected void flush(@NotNull BufferedWriter wrt) throws IOException {
                    // could be overwritten
                }

                protected void writeLn(@NotNull BufferedWriter wrt, String line) throws IOException {
                    wrt.write(line);
                    wrt.newLine();
                }
            }

            private class FinalMergeTask extends MergeTask {
                public FinalMergeTask(File src1, File src2, Phaser phaser) {
                    super(src1, src2, target, phaser);
                }

                @Override
                protected void writeLn(@NotNull BufferedWriter wrt, String line) throws IOException {
                    String stuff = reorgWriter ==null ? line : reorgWriter.reduce(line);
                    if (stuff != null) {
                        if (outRecFcn != null)
                            stuff = outRecFcn.apply(stuff);
                        super.writeLn(wrt, stuff);
                    }
                }

                @Override
                protected void flush(@NotNull BufferedWriter wrt) throws IOException {
                    if (reorgWriter != null) {
                        String stuff = reorgWriter.flush();
                        if (stuff != null) {
                            if (outRecFcn != null)
                                stuff = outRecFcn.apply(stuff);
                            super.writeLn(wrt, stuff);
                        }
                    }
                }
            }

            private class Splitter {
                private final List<String> data = new ArrayList<>(maxNumRecord);
                private final List<File> splitFiles = new LinkedList<>();

                public boolean process(String line) {
                    if (includeFilter == null || includeFilter.test(line)) {
                        if (inRecFcn != null)
                            line = inRecFcn.apply(line);
                        data.add(line);
                        if (data.size() >= maxNumRecord) {
                            File chunk = sortAndSave(data);
                            splitFiles.add(chunk);
                            data.clear();
                        }
                        return true;
                    } else {
                        return false;
                    }
                }

                private void save(@NotNull List<String> data, File file) {
                    try (BufferedWriter bw = Files.newBufferedWriter(file.toPath(), charset)) {
                        for (String datum : data) {
                            bw.write(datum);
                            bw.newLine();
                        }
                    } catch (IOException e) {
                        throw new SortException(e, "Error writing the file %s", file.getAbsolutePath());
                    }
                }

                private File sortAndSave(@NotNull List<String> data) {
                    Collections.sort(data, comparator);
                    File file;
                    try {
                        file = File.createTempFile(PRFX, SUFX);
                        file.deleteOnExit();
                        save(data, file);
                    } catch (IOException e) {
                        throw new SortException(e, "Error creating temporary files");
                    }
                    return file;
                }

                private void sortAndFinalSave(@NotNull List<String> data, File file) {
                    Collections.sort(data, comparator);
                    if (reorgWriter != null) {
                        data = reorg(data);
                    }
                    if (outRecFcn != null) {
                        List<String> good = new ArrayList<>();
                        for(String line: data) {
                            good.add(outRecFcn.apply(line));
                        }
                        data = good;
                    }
                    save(data, file);
                }

                private List<String> reorg(List<String> data) {
                    List<String> roll = new ArrayList<>();
                    for(String line: data) {
                        String temp = reorgWriter.reduce(line);
                        if (temp != null)
                            roll.add(temp);
                    }
                    String temp = reorgWriter.flush();
                    if (temp != null)
                        roll.add(temp);
                    return roll;
                }

                public List<File> files() {
                    if (data.isEmpty()) {
                        // no data pending (no save required), return as-is
                        return splitFiles;
                    } else {
                        // some pending data
                        if (splitFiles.isEmpty()) {
                            // no file for now -> all in one file (the final one)
                            sortAndFinalSave(data, target);
                            return Collections.singletonList(target);
                        } else {
                            // add remainder
                            File chunk = sortAndSave(data);
                            splitFiles.add(chunk);
                            return splitFiles;
                        }
                    }
                }
            }

            private class FirstReorgWriter implements RecordAccumulator {
                private String cache = null;
                @Nullable
                @Override
                public String reduce(@NotNull String line) {
                    if (cache == null || comparator.compare(cache, line) != 0) {
                        cache = line;
                        return line;
                    } else {
                        return null;
                    }
                }

                @Nullable
                @Override
                public String flush() {
                    return null;
                }
            }

            private class AllDupsReorgWriter implements RecordAccumulator {
                private String cache = null;
                private boolean pendingWrite;
                @Nullable
                @Override
                public String reduce(@NotNull String line) {
                    if (cache == null) {
                        cache = line;
                        pendingWrite = false;
                        return null;
                    } else if (comparator.compare(cache, line) == 0) {
                        String out = cache;
                        cache = line;
                        pendingWrite = true;
                        return out;
                    } else {
                        String out = pendingWrite ? cache: null;
                        cache = line;
                        pendingWrite = false;
                        return out;
                    }
                }

                @Nullable
                @Override
                public String flush() {
                    if (pendingWrite) {
                        pendingWrite = false;
                        return cache;
                    } else {
                        return null;
                    }
                }
            }

            private class NoDupsReorgWriter implements RecordAccumulator {
                private String cache = null;
                private boolean pendingWrite;
                @Nullable
                @Override
                public String reduce(@NotNull String line) {
                    if (cache == null) {
                        cache = line;
                        pendingWrite = true;
                        return null;
                    } else if (comparator.compare(cache, line) == 0) {
                        pendingWrite = false;
                        return null;
                    } else {
                        String out = pendingWrite ? cache : null;
                        cache = line;
                        pendingWrite = true;
                        return out;
                    }
                }

                @Nullable
                @Override
                public String flush() {
                    if (pendingWrite) {
                        pendingWrite = false;
                        return cache;
                    } else {
                        return null;
                    }
                }
            }

            private class LastReorgWriter implements RecordAccumulator {
                private String cache = null;
                @Nullable
                @Override
                public String reduce(@NotNull String line) {
                    String out;
                    if (cache == null || comparator.compare(cache, line) == 0) {
                        out = null;
                    } else {
                        out = cache;
                    }
                    cache = line;
                    return out;
                }

                @Nullable
                @Override
                public String flush() {
                    return cache;
                }
            }

            private class FirstDupReorgWriter implements RecordAccumulator {
                private String cache = null;
                private boolean isGarbage;
                @Nullable
                @Override
                public String reduce(@NotNull String line) {
                    String out;
                    if (cache == null || comparator.compare(cache, line) != 0) {
                        isGarbage = false;
                        out = null;
                    } else if (isGarbage) {
                        out = null;
                    } else {
                        out = cache;
                        isGarbage = true;
                    }
                    cache = line;
                    return out;
                }

                @Nullable
                @Override
                public String flush() {
                    return null;
                }
            }

            private class LastDupReorgWriter implements RecordAccumulator {
                private String cache = null;
                private boolean isGarbage;
                @Nullable
                @Override
                public String reduce(@NotNull String line) {
                    String out;
                    if (cache == null) {
                        isGarbage = true;
                        out = null;
                    } else if (comparator.compare(cache, line) == 0) {
                        isGarbage = false;
                        out = null;
                    } else if (isGarbage) {
                        out = null;
                    } else {
                        isGarbage = true;
                        out = cache;
                    }
                    cache = line;
                    return out;
                }

                @Nullable
                @Override
                public String flush() {
                    return isGarbage ? null : cache;
                }
            }
        }
    }
}
