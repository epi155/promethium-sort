package io.github.epi155.test;

import io.github.epi155.pm.sort.RecordEditor;
import io.github.epi155.pm.sort.SortEngine;
import io.github.epi155.pm.sort.SortException;
import io.github.epi155.pm.sort.SortFilter;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Random;

public class TestSort {
    @Test
    public void testNumSort() throws IOException {
        final File source = File.createTempFile("rand-", ".txt");
        Random random = new Random();
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(source.getAbsolutePath()), StandardCharsets.UTF_8)) {
            for (int k = 0; k < 1000; k++) {
                int n = random.nextInt(100_000);
                bw.write(String.format("%10d", n));
                bw.newLine();
            }
            bw.write("X");
            bw.newLine();
        }
        final File target = File.createTempFile("sort-", ".txt");
        SortEngine.using(256)
            .sortIn(source).sort(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            }).sortOut(target);
        verifyOrder(target);
    }

    private void verifyOrder(File target) throws IOException {
        try(BufferedReader br = Files.newBufferedReader(Paths.get(target.getAbsolutePath()), StandardCharsets.ISO_8859_1)) {
            String oldLine = null;
            String line;
            while ((line = br.readLine()) != null) {
                if (oldLine != null) {
                    Assert.assertTrue("error: \n"+oldLine+"\n"+line, oldLine.compareTo(line) <= 0);
                }
                oldLine = line;
            }
        }
    }

    @Test
    public void testNum() throws IOException {
        final File source = File.createTempFile("bef-", ".txt");
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(source.getAbsolutePath()), StandardCharsets.US_ASCII)) {
            for (int k = 0; k < 100; k++) {
                bw.write(String.format("%010d", k));
                bw.newLine();
            }
            bw.write("\u001a");
            bw.newLine();
        }
        final File target = File.createTempFile("aft-", ".txt");
        final RecordEditor editIn = new RecordEditor() {
            @Override
            public String apply(String line) {
                return line.substring(1);
            }
        };
        final RecordEditor editOut = new RecordEditor() {
            @Override
            public String apply(String line) {
                return line + "A";
            }
        };
        final SortFilter filter = new SortFilter() {
            @Override
            public boolean test(String line) {
                return (((int) line.charAt(line.length() - 1)) & 0x01) == 0;
            }
        };
        SortEngine.using(12, StandardCharsets.US_ASCII)
            .sortIn(source)
            .skipRecord(100)
            .include(filter)
            .stopAfter(30)
            .inRec(editIn)
            .sort()
            .outRec(editOut)
            .sortOut(target);
        verifyOrder(target);

        SortEngine.using(12, StandardCharsets.US_ASCII)
            .sortIn(source)
            .skipRecord(5)
            .include(filter)
            .stopAfter(30)
            .inRec(editIn)
            .sort()
            .outRec(editOut)
            .sortOut(target);
        verifyOrder(target);

        SortEngine.using(100, StandardCharsets.US_ASCII)
            .sortIn(source)
            .skipRecord(5)
            .include(filter)
            .stopAfter(30)
            .inRec(editIn)
            .sort()
            .outRec(editOut)
            .sortOut(target);
        verifyOrder(target);
    }

    @Test
    public void testNum2() throws IOException {
        final File source = File.createTempFile("bef-", ".txt");
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(source.getAbsolutePath()), StandardCharsets.US_ASCII)) {
            for (int k = 0; k < 200; k++) {
                bw.write(String.format("%010d", k));
                bw.newLine();
            }
        }
        final File target = File.createTempFile("00-", ".txt");
        SortEngine.using(256, StandardCharsets.US_ASCII, 2)
            .sortIn(source)
            .sort(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.substring(5).compareTo(o2.substring(5));
                }
            })
            .sortOut(target);
        verifyOrder(target);
    }
    @Test(expected = SortException.class)
    public void testNum3() throws IOException {
        final File source = File.createTempFile("bef-", ".txt");
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(source.getAbsolutePath()), StandardCharsets.US_ASCII)) {
            for (int k = 0; k < 200; k++) {
                bw.write(String.format("%010d", k));
                bw.newLine();
            }
        }
        final File target = new File("/dev/full");
        SortEngine.using(256, StandardCharsets.US_ASCII, 2)
            .sortIn(source)
            .sort(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.substring(5).compareTo(o2.substring(5));
                }
            })
            .sortOut(target);
        verifyOrder(target);
    }
    @Test
    public void testNum4() throws IOException {
        final File source = File.createTempFile("bef-", ".txt");
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(source.getAbsolutePath()), StandardCharsets.US_ASCII)) {
            for (int k = 0; k < 200; k++) {
                bw.write(String.format("%010d", k));
                bw.newLine();
            }
        }
        final File target = File.createTempFile("00-", ".txt");
        Comparator<String> comp = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.substring(0,5).compareTo(o2.substring(0,5));
            }
        };
        SortEngine.using(20, StandardCharsets.US_ASCII, 2)
            .sortIn(source)
            .sort(comp)
            .sortOut(target);
        verifyOrder(target);
    }
    @Test
    public void testNum5() throws IOException {
        final File source = File.createTempFile("bef-", ".txt");
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(source.getAbsolutePath()), StandardCharsets.US_ASCII)) {
            for (int k = 0; k < 200; k++) {
                bw.write(String.format("%010d", k));
                bw.newLine();
            }
        }
        final File target = File.createTempFile("00-", ".txt");
        Comparator<String> comp = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o2.compareTo(o1);
            }
        };
        SortEngine.using(20, StandardCharsets.US_ASCII, 2)
            .sortIn(source)
            .sort(comp)
            .sortOut(target);
        verifyOrderReverse(target);
    }

    private void verifyOrderReverse(File target) throws IOException {
        try(BufferedReader br = Files.newBufferedReader(Paths.get(target.getAbsolutePath()), StandardCharsets.ISO_8859_1)) {
            String oldLine = null;
            String line;
            while ((line = br.readLine()) != null) {
                if (oldLine != null) {
                    Assert.assertTrue("error: \n"+oldLine+"\n"+line, oldLine.compareTo(line) >= 0);
                }
                oldLine = line;
            }
        }
    }
}
