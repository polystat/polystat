/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2022 Polystat.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.polystat;

import com.jcabi.log.Logger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;

/**
 * Stores configuration for Polystat.
 * @since 1.0
 */
public class Config {

    /**
     * Mapping between config options and their values.
     */
    private final Map<String, String> values;

    /**
     * Ctor.
     * @param config Map with the config options and their values.
     */
    public Config(final Map<String, String> config) {
        this.values = config;
    }

    /**
     * Ctor.
     * @param path Path to the file to read configs from.
     */
    public Config(final Path path) {
        this(parseConfig(path));
    }

    /**
     * Returns the value of the config option if present.
     * @param key Config option.
     * @return The value of the config option or {@code null}.
     */
    @Nullable
    public String get(final String key) {
        return this.values.get(key);
    }

    /**
     * Reads command-line arguments from the ".polystat" file.
     * @param path Path to ".polystat" file.
     * @return A map with successfully parsed options. If an error occurs, return an empty Map.
     * @todo #56:1h The current implementation is very prone to errors and
     *  should be replaced with a more robust solution.
     */
    private static Map<String, String> parseConfig(final Path path) {
        final Map<String, String> result = new HashMap<>();
        try (Stream<String> st = Files.lines(path)) {
            st.forEachOrdered(
                line -> {
                    final String[] parts = line.trim().split(" ", 2);
                    if (parts.length == 2) {
                        result.put(parts[0], parts[1]);
                    } else if (parts.length == 1) {
                        result.put(parts[0], "true");
                    }
                }
            );
        } catch (final IOException ex) {
            Logger.warn(
                Polystat.class,
                String.format(
                    "Could not read config file %s. Using options from command line...",
                    path.toString()
                )
            );
        }
        return result;
    }
}
