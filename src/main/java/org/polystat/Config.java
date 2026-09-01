/*
 * SPDX-FileCopyrightText: Copyright (c) 2020-2022 Polystat.org
 * SPDX-License-Identifier: MIT
 */
package org.polystat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Stores command line options from Polystat.
 * @since 1.0
 */
public final class Config implements Iterable<String> {

    /**
     * Boundary between two arguments on the same line.
     */
    private static final Pattern SPACES = Pattern.compile("\\s+");

    /**
     * Mapping between config options and their values.
     */
    private final List<String> args;

    /**
     * Ctor.
     * @param path Path to the file to read configs from
     */
    public Config(final Path path) {
        this(Config.parsed(path));
    }

    /**
     * Ctor.
     * @param config Map with the config options and their values
     */
    public Config(final List<String> config) {
        this.args = config;
    }

    @Override
    public Iterator<String> iterator() {
        return this.args.iterator();
    }

    private static List<String> parsed(final Path path) {
        List<String> result;
        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
            result = lines
                .flatMap(line -> Config.SPACES.splitAsStream(line.trim()))
                .filter(arg -> !arg.isEmpty())
                .collect(Collectors.toList());
        } catch (final IOException ex) {
            result = new ArrayList<>(0);
        }
        return result;
    }
}
