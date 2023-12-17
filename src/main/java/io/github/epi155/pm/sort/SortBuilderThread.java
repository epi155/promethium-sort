package io.github.epi155.pm.sort;

/**
 * Partial builder (Max Thread)
 */
public interface SortBuilderThread extends SortBuilderWork {
    /**
     * Set max thread (merge phase) (default numCore/2)
     *
     * @param maxThread max thread
     * @return instance of {@link SortBuilderWork}
     */
    SortBuilderWork withMaxThread(int maxThread);
}
