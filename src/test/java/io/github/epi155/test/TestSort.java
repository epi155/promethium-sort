package io.github.epi155.test;

import io.github.epi155.pm.sort.RecordEditor;
import io.github.epi155.pm.sort.SortEngine;
import io.github.epi155.pm.sort.SortException;
import io.github.epi155.pm.sort.SortFilter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        SortEngine.builder()
                .withMaxRecord(256)
                .build()
            .sortIn(source).sort(Comparator.naturalOrder()).sortOut(target);
        verifyOrder(target);
    }

    private void verifyOrder(File target) throws IOException {
        try(BufferedReader br = Files.newBufferedReader(Paths.get(target.getAbsolutePath()), StandardCharsets.ISO_8859_1)) {
            String oldLine = null;
            String line;
            while ((line = br.readLine()) != null) {
                if (oldLine != null) {
                    Assertions.assertTrue(oldLine.compareTo(line) <= 0, "error: \n"+oldLine+"\n"+line);
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
        final RecordEditor editIn = line -> line.substring(1);
        final RecordEditor editOut = line -> line + "A";
        final SortFilter filter = line -> (((int) line.charAt(line.length() - 1)) & 0x01) == 0;
        SortEngine.builder()
                .withMaxRecord(12).withCharset(StandardCharsets.US_ASCII)
                .build()
            .sortIn(source)
            .skipRecord(100)
            .include(filter)
            .stopAfter(30)
            .inRec(editIn)
            .sort()
            .outRec(editOut)
            .sortOut(target);
        verifyOrder(target);

        SortEngine.builder()
                .withMaxRecord(12).withCharset(StandardCharsets.US_ASCII)
                .build()
            .sortIn(source)
            .skipRecord(5)
            .include(filter)
            .stopAfter(30)
            .inRec(editIn)
            .sort()
            .outRec(editOut)
            .sortOut(target);
        verifyOrder(target);

        SortEngine.builder()
                .withMaxRecord(100).withCharset(StandardCharsets.US_ASCII)
                .build()
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
        SortEngine.builder()
                .withMaxRecord(256)
                .withCharset(StandardCharsets.US_ASCII)
                .withMaxThread(2)
                .build()
            .sortIn(source)
            .sort(Comparator.comparing(o -> o.substring(5)))
            .sortOut(target);
        verifyOrder(target);
    }
    @Test
    public void testNum3() throws IOException {
        final File source = File.createTempFile("bef-", ".txt");
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(source.getAbsolutePath()), StandardCharsets.US_ASCII)) {
            for (int k = 0; k < 200; k++) {
                bw.write(String.format("%010d", k));
                bw.newLine();
            }
        }
        final File target = new File("/dev/full");
        Assertions.assertThrows(SortException.class, () -> SortEngine.builder()
                .withMaxRecord(256)
                .withCharset(StandardCharsets.US_ASCII)
                .withMaxThread(2)
                .build()
                .sortIn(source)
                .sort(Comparator.comparing(o -> o.substring(5)))
                .sortOut(target));
       // verifyOrder(target);
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
        Comparator<String> comp = Comparator.comparing(o -> o.substring(0, 5));
        SortEngine.builder()
                .withMaxRecord(20)
                .withCharset(StandardCharsets.US_ASCII)
                .withMaxThread(2)
                .build()
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
        Comparator<String> comp = Comparator.reverseOrder();
        SortEngine.builder()
                .withMaxRecord(20)
                .withCharset(StandardCharsets.US_ASCII)
                .withMaxThread(2)
                .build()
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
                    Assertions.assertTrue(oldLine.compareTo(line) >= 0, "error: \n"+oldLine+"\n"+line);
                }
                oldLine = line;
            }
        }
    }

    @Test
    public void testNumSortDup() throws IOException {
        final File source = File.createTempFile("rand-", ".txt");
        Random random = new Random();
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(source.getAbsolutePath()), StandardCharsets.UTF_8)) {
            for (int k = 0; k < 1000; k++) {
                int n = random.nextInt(500);
                bw.write(String.format("%10d%10d", n, k));
                bw.newLine();
            }
        }
        final File target = File.createTempFile("sort-", ".txt");
        SortEngine.builder()
                .withMaxRecord(2048)
                .build()
            .sortIn(source)
            .sort((o1, o2) -> {
                String s1 = o1.substring(0,10);
                String s2 = o2.substring(0,10);
                return s1.compareTo(s2);
            })
            .allDups()
            .sortOut(target);
        verifyOrder(target);
    }
    @Test
    public void testNumSortNoDup() throws IOException {
        final File source = File.createTempFile("rand-", ".txt");
        Random random = new Random();
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(source.getAbsolutePath()), StandardCharsets.UTF_8)) {
            for (int k = 0; k < 1000; k++) {
                int n = random.nextInt(500);
                bw.write(String.format("%10d", n));
                bw.newLine();
            }
            bw.newLine();
        }
        final File target = File.createTempFile("sort-", ".txt");
        SortEngine.builder()
                .withMaxRecord(256)
                .build()
            .sortIn(source)
            .sort()
            .noDups()
            .sortOut(target);
        verifyOrder(target);
    }
    @Test
    public void testNumSortFirst() throws IOException {
        final File source = File.createTempFile("rand-", ".txt");
        Random random = new Random();
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(source.getAbsolutePath()), StandardCharsets.UTF_8)) {
            for (int k = 0; k < 1000; k++) {
                int n = random.nextInt(500);
                bw.write(String.format("%10d", n));
                bw.newLine();
            }
            bw.newLine();
        }
        final File target = File.createTempFile("sort-", ".txt");
        SortEngine.builder()
                .withMaxRecord(256)
                .build()
            .sortIn(source)
            .sort()
            .first()
            .sortOut(target);
        verifyOrder(target);
    }
    @Test
    public void testNumSortLast() throws IOException {
        final File source = File.createTempFile("rand-", ".txt");
        Random random = new Random();
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(source.getAbsolutePath()), StandardCharsets.UTF_8)) {
            for (int k = 0; k < 1000; k++) {
                int n = random.nextInt(500);
                bw.write(String.format("%10d", n));
                bw.newLine();
            }
            bw.newLine();
        }
        final File target = File.createTempFile("sort-", ".txt");
        SortEngine.builder()
                .withMaxRecord(256)
                .build()
            .sortIn(source)
            .sort()
            .last()
            .sortOut(target);
        verifyOrder(target);
    }
    @Test
    public void testNumSortFirstDup() throws IOException {
        final File source = File.createTempFile("rand-", ".txt");
        Random random = new Random();
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(source.getAbsolutePath()), StandardCharsets.UTF_8)) {
            for (int k = 0; k < 1000; k++) {
                int n = random.nextInt(500);
                bw.write(String.format("%10d", n));
                bw.newLine();
            }
            bw.newLine();
        }
        final File target = File.createTempFile("sort-", ".txt");
        SortEngine.builder()
                .withMaxRecord(256)
                .build()
            .sortIn(source)
            .sort()
            .firstDup()
            .sortOut(target);
        verifyOrder(target);
    }
    @Test
    public void testNumSortLastDup() throws IOException {
        final File source = File.createTempFile("rand-", ".txt");
        Random random = new Random();
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(source.getAbsolutePath()), StandardCharsets.UTF_8)) {
            for (int k = 0; k < 1000; k++) {
                int n = random.nextInt(500);
                bw.write(String.format("%10d", n));
                bw.newLine();
            }
            bw.newLine();
        }
        final File target = File.createTempFile("sort-", ".txt");
        SortEngine.builder()
                .withMaxRecord(256)
                .build()
            .sortIn(source)
            .sort()
            .lastDup()
            .outRec(line -> "0"+line)
            .sortOut(target);
        verifyOrder(target);
    }
}
