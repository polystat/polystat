/*
 * SPDX-FileCopyrightText: Copyright (c) 2020-2022 Polystat.org
 * SPDX-License-Identifier: MIT
 */
package org.polystat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.cactoos.list.ListOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test cases for {@code Config} object.
 * @since 1.0
 */
final class ConfigTest {

    /**
     * Name of the configuration file.
     */
    private static final String CONFIG_FILENAME = ".polystat";

    @Test
    void readsOptionsCorrectly(@TempDir final Path tmp) throws IOException {
        final Path file = tmp.resolve(ConfigTest.CONFIG_FILENAME);
        Files.write(
            file,
            String.format("--files sandbox%n--tmp tmp%n--sarif%n")
                .getBytes(StandardCharsets.UTF_8)
        );
        Assertions.assertEquals(
            new ListOf<>("--files", "sandbox", "--tmp", "tmp", "--sarif"),
            new ListOf<>(new Config(file))
        );
    }

    @Test
    void ignoresEmptyLines(@TempDir final Path tmp) throws IOException {
        final Path file = tmp.resolve(ConfigTest.CONFIG_FILENAME);
        Files.write(
            file,
            String.format("%n%n%n%n--includeRules a b c d")
                .getBytes(StandardCharsets.UTF_8)
        );
        Assertions.assertEquals(
            new ListOf<>("--includeRules", "a", "b", "c", "d"),
            new ListOf<>(new Config(file))
        );
    }

    @Test
    void fileDoesNotExist(@TempDir final Path tmp) throws IOException {
        MatcherAssert.assertThat(
            new ListOf<>(new Config(tmp.resolve("foo"))),
            Matchers.empty()
        );
    }
}
