package io.github.epi155.pm.sort;

import java.util.Comparator;

abstract class PmLayerSort implements LayerSort {
    private static final Comparator<String> naturalOrder = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    };
    public LayerPostSort sort() {
        return sort(naturalOrder);
    }
}
