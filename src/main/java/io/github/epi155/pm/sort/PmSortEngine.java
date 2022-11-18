package io.github.epi155.pm.sort;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

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

@Slf4j
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

    private class PmSortIn implements LayerSkipRecord {
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
        public @NotNull LayerOutRec sort(@NotNull Comparator<String> comparator) {
            return this.new PmSort(comparator);
        }

        @Override
        public @NotNull LayerInRec stopAfter(int nmStop) {
            this.nmStop = nmStop;
            return this;
        }

        private class PmSort implements LayerOutRec {
            private final Comparator<String> comparator;
            private RecordEditor outRecFcn = null;
            private File target;

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
                    throw new SortException(e, "Error reading the file {}", source);
                }
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
                    log.trace("Merging {}/{} ...", src1.getName(), src2.getName());
                    try {
                        performMerge();
                        log.trace("Merged into {}", dest.getName());
                        try {
                            Files.delete(src1.toPath());
                        } catch (IOException e) {
                            log.error("Error deleting {}", src1.getName(), e);
                        }
                        try {
                            Files.delete(src2.toPath());
                        } catch (IOException e) {
                            log.error("Error deleting {}", src2.getName(), e);
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
                    } catch (IOException e) {
                        throw new SortException(e, "Error merging file {},{} -> {}", src1.getName(), src2.getName(), dest.getName());
                    }
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
                    if (outRecFcn != null)
                        line = outRecFcn.apply(line);
                    super.writeLn(wrt, line);
                }
            }

            private class Splitter {
                private final List<String> data = new ArrayList<>(maxNumRecord);
                private final List<File> splitFiles = new LinkedList<>();

                public boolean process(String line) {
                    if (inRecFcn != null)
                        line = inRecFcn.apply(line);
                    if (includeFilter == null || includeFilter.test(line)) {
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
                        throw new SortException(e, "Error writing the file {}", file.getName());
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
                    if (outRecFcn != null) {
                        List<String> good = new ArrayList<>();
                        for(String line: data) {
                            good.add(outRecFcn.apply(line));
                        }
                        data = good;
                    }
                    save(data, file);
                }

                public List<File> files() {
                    if (data.isEmpty()) {
                        // no data pending (no save required)
                        if (splitFiles.isEmpty()) {
                            // no file at all
                            return Collections.emptyList();
                        } else {
                            // all file filled completely
                            return splitFiles;
                        }
                    } else {
                        // some pending data
                        if (splitFiles.isEmpty()) {
                            // no file by now -> all one file
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
        }
    }
}
