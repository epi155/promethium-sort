package io.github.epi155.test;

import io.github.epi155.pm.sort.RecordAccumulator;
import io.github.epi155.pm.sort.SortEngine;
import io.github.epi155.pm.sort.SumFields;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;

public class TestSortOpt {
    Comparator<String> com = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return o1.substring(0, 3).compareTo(o2.substring(0, 3));
        }
    };
    private static String[] arrayFromFile(File t1) {
        String[] s1 = new String[0];
        try {

            byte[] bytes = Files.readAllBytes(t1.toPath());
            s1 = new String (bytes).split("\n");
        } catch (IOException e) {
            //handle exception
        }
        return s1;
    }
    private static void arrayToFile(String[] as, File source) throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(source.getAbsolutePath()), StandardCharsets.UTF_8)) {
            for (String a: as) {
                bw.write(a);
                bw.newLine();
            }
        }
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
        arrayToFile(as, source);
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

    @Test
    public void testNumSortMix2() throws IOException {
        final File source = File.createTempFile("rand-", ".txt");
        String[] as = {
            "000000",
            "001001",
            "001002",
            "002003",
            "003004",
            "004005",
            "004006",
            "004007",
            "005008",
        };
        arrayToFile(as, source);
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
            "000000",
            "001001",
            "002003",
            "003004",
            "004005",
            "005008",
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
            "000000",
            "001002",
            "002003",
            "003004",
            "004007",
            "005008",
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
            "000000",
            "002003",
            "003004",
            "005008",
        };
        Assert.assertArrayEquals(e6, s6);
    }

    static class GroupCount implements RecordAccumulator {
        int count = 0;
        private String cacheKey = null;
        private String keyOf(String line) {
            return line.substring(0,3);
        }
        @Override
        public String reduce(String line) {
            String out;
            String key = keyOf(line);
            if (cacheKey == null) {
                out = null;
                cacheKey = key;
            } else if (cacheKey.compareTo(key) == 0) {
                out = null;
            } else {
                out = cacheKey+String.format("%03d", count);
                count = 0;
                cacheKey = key;
            }
            count++;
            return out;
        }

        @Override
        public String flush() {
            return cacheKey+String.format("%03d", count);
        }
    }
    @Test
    public void testNumSortMix3() throws IOException {
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
        arrayToFile(as, source);
        final File t1 = File.createTempFile("grp-", ".txt");
        SortEngine.using(256)
            .sortIn(source)
            .sort(com)
            .reduce(new GroupCount())
            .sortOut(t1);
        String[] s1 = arrayFromFile(t1);
        String[] e1 = {
            "001002",
            "002001",
            "003001",
            "004003",
        };
        Assert.assertArrayEquals(e1, s1);
    }
    static class GroupCount2 extends SumFields {
        int count;
        private String keyOf(String line) {
            return line.substring(0,3);
        }
        @Override
        protected void reset() {
            count = 0;
        }

        @Override
        protected void add(String line) {
            count++;
        }

        @Override
        protected String getSummary(String line) {
            return keyOf(line)+String.format("%03d", count);
        }
    }
    @Test
    public void testNumSortMix4() throws IOException {
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
        arrayToFile(as, source);
        final File t1 = File.createTempFile("grp-", ".txt");
        SortEngine.using(256)
            .sortIn(source)
            .sort(com)
            .sum(new GroupCount2())
            .sortOut(t1);
        String[] s1 = arrayFromFile(t1);
        String[] e1 = {
            "001002",
            "002001",
            "003001",
            "004003",
        };
        Assert.assertArrayEquals(e1, s1);

        final File t2 = File.createTempFile("sfn-", ".txt");
        SortEngine.using(256)
            .sortIn(source)
            .sort(com)
            .sum(SumFields.none())
            .sortOut(t2);
        String[] s2 = arrayFromFile(t2);
        final File t3 = File.createTempFile("lst-", ".txt");
        SortEngine.using(256)
            .sortIn(source)
            .sort(com)
            .first()
            .sortOut(t3);
        String[] s3 = arrayFromFile(t3);
        Assert.assertArrayEquals(s2, s3);

    }
}
