package io.github.epi155.test;

import io.github.epi155.pm.sort.SortEngine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.Random;

class TestSort {
    @Test
    void testNumSort() throws IOException {
        File source = File.createTempFile("rand-", ".txt");
        Random random = new Random();
        try (BufferedWriter bw = Files.newBufferedWriter(source.toPath())) {
            for (int k = 0; k < 1000; k++) {
                int n = random.nextInt(100_000);
                bw.write(String.format("%10d", n));
                bw.newLine();
            }
        }
        File target = File.createTempFile("sort-", ".txt");
        Assertions.assertDoesNotThrow(() -> SortEngine.using(256)
            .sortIn(source).sort().sortOut(target));
    }

    @Test
    void testNum() throws IOException {
        File source = File.createTempFile("bef-", ".txt");
        try (BufferedWriter bw = Files.newBufferedWriter(source.toPath())) {
            for (int k = 0; k < 100; k++) {
                bw.write(String.format("%010d", k));
                bw.newLine();
            }
        }
        File target = File.createTempFile("aft-", ".txt");
        Assertions.assertDoesNotThrow(() -> SortEngine.using(12, StandardCharsets.US_ASCII)
            .sortIn(source)
            .skipRecord(5)
            .include(it -> (((int) it.charAt(it.length() - 1)) & 0x01) == 0)
            .stopAfter(20)
            .inRec(it -> it + "A")
            .sort()
            .outRec(it -> it.substring(1))
            .sortOut(target));
    }

    @Test
    void testNum2() throws IOException {
        File source = File.createTempFile("bef-", ".txt");
        try (BufferedWriter bw = Files.newBufferedWriter(source.toPath())) {
            for (int k = 0; k < 200; k++) {
                bw.write(String.format("%010d", k));
                bw.newLine();
            }
        }
        File target = File.createTempFile("00-", ".txt");
        Assertions.assertDoesNotThrow(() -> SortEngine.using(256, StandardCharsets.US_ASCII, 2)
            .sortIn(source)
            .sort(Comparator.comparing(a -> a.substring(5)))
            .sortOut(target));
    }
}
