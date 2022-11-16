package io.github.epi155.pm.sort;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;

/**
 * Sort Factory
 */
public interface SortEngine {
    /**
     * Set the sort context
     *
     * @param maxNumRecord max record sorted in memory
     * @param charset      file charset
     * @param maxThread    max thread for merge split file
     * @return {@link LayerSortIn} instance
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    static @NotNull LayerSortIn using(int maxNumRecord, @NotNull Charset charset, int maxThread) {
        return new PmSortEngine(maxNumRecord, charset, maxThread);
    }

    /**
     * Set the sort context (maxThread := nmCore / 2)
     *
     * @param maxNumRecord max record sorted in memory
     * @param charset      file charset
     * @return {@link LayerSortIn} instance
     */
    @Contract("_, _ -> new")
    static @NotNull LayerSortIn using(int maxNumRecord, @NotNull Charset charset) {
        return new PmSortEngine(maxNumRecord, charset);
    }

    /**
     * Set the sort context (charset := UTF-8; maxThread := nmCore / 2)
     *
     * @param maxNumRecord max record sorted in memory
     * @return {@link LayerSortIn} instance
     */
    @Contract("_ -> new")
    static @NotNull LayerSortIn using(int maxNumRecord) {
        return new PmSortEngine(maxNumRecord);
    }
}
