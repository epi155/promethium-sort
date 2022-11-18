package io.github.epi155.pm.sort;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

abstract class PmLayerSort implements LayerSort {
    private static final Comparator<String> naturalOrder = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    };

    @NotNull
    public LayerOutRec sort() {
        return sort(naturalOrder);
    }
}
