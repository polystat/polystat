/*
 * SPDX-FileCopyrightText: Copyright (c) 2020-2022 Polystat.org
 * SPDX-License-Identifier: MIT
 */
package org.polystat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.cactoos.Text;
import org.cactoos.io.ResourceOf;
import org.cactoos.io.TeeInput;
import org.cactoos.scalar.LengthOf;
import org.cactoos.scalar.Unchecked;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test case for {@link Program}.
 * @since 0.1
 */
final class ProgramTest {

    @Test
    void interpretOneEolangProgram(@TempDir final Path temp) throws Exception {
        this.writeSources(temp);
        this.assertOutput(temp, temp);
    }

    @Test
    void recompilesIfExpired(@TempDir final Path temp) throws Exception {
        this.writeSources(temp);
        final Path xml = temp.resolve("foo.xml");
        this.writeFile(new TextOf("INVALID"), xml);
        Assertions.assertTrue(
            xml.toFile().setLastModified(0L)
        );
        this.assertOutput(temp, temp);
    }

    private void writeFile(final Text data, final Path file) {
        new Unchecked<>(new LengthOf(new TeeInput(data, file))).value();
    }

    private void assertOutput(final Path sources, final Path temp) throws Exception {
        MatcherAssert.assertThat(
            new Program(sources, temp).apply("\\Phi.test.fv").xpath("@name").get(0),
            Matchers.equalTo("fv")
        );
    }

    private void writeSources(final Path dir) {
        final Iterable<String> sources = Arrays.asList(
            "org/polystat/test.eo",
            "org/polystat/five.eo"
        );
        for (final String src : sources) {
            this.writeFile(
                new TextOf(
                    new ResourceOf(src)
                ),
                dir.resolve(Paths.get(src).getFileName())
            );
        }
    }
}
