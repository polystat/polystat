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
import org.cactoos.list.ListOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
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
        Files.write(file, "--files sandbox\n--tmp tmp\n--sarif\n".getBytes(StandardCharsets.UTF_8));
        final Config config = new Config(file);
        MatcherAssert.assertThat(config.get("--files"), Matchers.equalTo("sandbox"));
        MatcherAssert.assertThat(config.get("--tmp"), Matchers.equalTo("tmp"));
        MatcherAssert.assertThat(config.get("--sarif"), Matchers.equalTo("true"));
        MatcherAssert.assertThat(config.get("null"), Matchers.nullValue());
    }

    @Test
    void ignoresEmptyLines(@TempDir final Path tmp) throws IOException {
        final Path file = tmp.resolve(ConfigTest.CONFIG_FILENAME);
        Files.write(file, "\n\n\n\n--includeRules a b c d".getBytes(StandardCharsets.UTF_8));
        final Config config = new Config(file);
        MatcherAssert.assertThat(new ListOf<>(config), Matchers.hasSize(1));
        MatcherAssert.assertThat(config.get("--includeRules"), Matchers.equalTo("a b c d"));
    }

    @Test
    void fileDoesNotExist(@TempDir final Path tmp) throws IOException {
        final Path file = tmp.resolve("foo");
        final Config config = new Config(file);
        MatcherAssert.assertThat(new ListOf<>(config), Matchers.empty());
    }

}
