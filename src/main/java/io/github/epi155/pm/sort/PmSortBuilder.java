package io.github.epi155.pm.sort;

import java.io.File;
import java.nio.charset.Charset;

class PmSortBuilder implements SortBuilderRecord {
    private int maxRecord;
    private Charset charset;
    private int maxThread;
    private File swap;

    @Override
    public LayerSortIn build() {
        return new PmSortEngine(maxRecord, charset, maxThread, swap);
    }

    @Override
    public SortBuilderCharset withMaxRecord(int maxRecord) {
        this.maxRecord = maxRecord;
        return this;
    }

    @Override
    public SortBuilderThread withCharset(Charset charset) {
        this.charset = charset;
        return this;
    }

    @Override
    public SortBuilderWork withMaxThread(int maxThread) {
        this.maxThread = maxThread;
        return this;
    }

    @Override
    public SortBuilder withTempDirectory(File tempDirectory) {
        this.swap = tempDirectory;
        return this;
    }

    @Override
    public SortBuilder withTempDirectory(String tempDirectory) {
        this.swap = new File(tempDirectory);
        return this;
    }
}
