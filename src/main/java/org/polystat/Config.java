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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.cactoos.list.ListOf;

/**
 * Stores command line options from Polystat.
 * @since 1.0
 */
public final class Config implements Iterable<String> {

    /**
     * Mapping between config options and their values.
     */
    private final List<String> args;

    /**
     * Ctor.
     * @param config Map with the config options and their values.
     */
    public Config(final List<String> config) {
        this.args = config;
    }

    /**
     * Ctor.
     * @param path Path to the file to read configs from.
     */
    public Config(final Path path) {
        this(parseConfig(path));
    }

    @Override
    public Iterator<String> iterator() {
        return this.args.iterator();
    }

    /**
     * Reads command-line arguments from the ".polystat" file.
     * @param path Path to ".polystat" file.
     * @return A list with successfully parsed options. If an error occurs, return an empty list.
     */
    private static List<String> parseConfig(final Path path) {
        List<String> result;
        try (Stream<String> st = Files.lines(path, StandardCharsets.UTF_8)) {
            result = st.flatMap(line -> new ListOf<String>(line.trim().split("\\s+")).stream())
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
        } catch (final IOException ex) {
            result = new LinkedList<>();
        }
        return result;
    }
}
