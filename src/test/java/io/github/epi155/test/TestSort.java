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
        SortEngine.using(2048)
            .sortIn(source)
            .sort(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    String s1 = o1.substring(0,10);
                    String s2 = o2.substring(0,10);
                    return s1.compareTo(s2);
                }
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
        SortEngine.using(256)
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
        SortEngine.using(256)
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
        SortEngine.using(256)
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
        SortEngine.using(256)
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
        SortEngine.using(256)
            .sortIn(source)
            .sort()
            .lastDup()
            .sortOut(target);
        verifyOrder(target);
    }
    @Test
    public void testNumSortMix() throws IOException {
        final File source = File.createTempFile("rand-", ".txt");
        String[] as = {
            "001001",
            "001002",
            "002003",
            "003004",
            "004005",
            "004006",
            "004007",
        };
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(source.getAbsolutePath()), StandardCharsets.UTF_8)) {
            for (String a: as) {
                bw.write(a);
                bw.newLine();
            }
        }
        Comparator<String> com = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.substring(0, 3).compareTo(o2.substring(0, 3));
            }
        };
        final File t1 = File.createTempFile("fd-", ".txt");
        SortEngine.using(256)
            .sortIn(source)
            .sort(com)
            .firstDup()
            .sortOut(t1);
        String[] s1 = arrayFromFile(t1);
        String[] e1 = {
            "001001",
            "004005",
        };
        Assert.assertArrayEquals(e1, s1);

        final File t2 = File.createTempFile("ld-", ".txt");
        SortEngine.using(256)
            .sortIn(source)
            .sort(com)
            .lastDup()
            .sortOut(t2);
        String[] s2 = arrayFromFile(t2);
        String[] e2 = {
            "001002",
            "004007",
        };
        Assert.assertArrayEquals(e2, s2);

        final File t3 = File.createTempFile("fx-", ".txt");
        SortEngine.using(256)
            .sortIn(source)
            .sort(com)
            .first()
            .sortOut(t3);
        String[] s3 = arrayFromFile(t3);
        String[] e3 = {
            "001001",
            "002003",
            "003004",
            "004005",
        };
        Assert.assertArrayEquals(e3, s3);

        final File t4 = File.createTempFile("lx-", ".txt");
        SortEngine.using(256)
            .sortIn(source)
            .sort(com)
            .last()
            .sortOut(t4);
        String[] s4 = arrayFromFile(t4);
        String[] e4 = {
            "001002",
            "002003",
            "003004",
            "004007",
        };
        Assert.assertArrayEquals(e4, s4);

        final File t5 = File.createTempFile("ad-", ".txt");
        SortEngine.using(256)
            .sortIn(source)
            .sort(com)
            .allDups()
            .sortOut(t5);
        String[] s5 = arrayFromFile(t5);
        String[] e5 = {
            "001001",
            "001002",
            "004005",
            "004006",
            "004007",
        };
        Assert.assertArrayEquals(e5, s5);

        final File t6 = File.createTempFile("nd-", ".txt");
        SortEngine.using(256)
            .sortIn(source)
            .sort(com)
            .noDups()
            .sortOut(t6);
        String[] s6 = arrayFromFile(t6);
        String[] e6 = {
            "002003",
            "003004",
        };
        Assert.assertArrayEquals(e6, s6);
    }

    private String[] arrayFromFile(File t1) {
        String[] s1 = new String[0];
        try {

            byte[] bytes = Files.readAllBytes(t1.toPath());
            s1 = new String (bytes).split("\n");
        } catch (IOException e) {
            //handle exception
        }
        return s1;
    }
}
