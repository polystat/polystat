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

import com.jcabi.xml.XML;
import java.nio.file.Path;
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
 *
 * @since 0.1
 */
final class ProgramTest {

    @Test
    void interpretOneEolangProgram(
        @TempDir final Path sources,
        @TempDir final Path temp
    ) throws Exception {
        this.writeSourceFile(sources);
        this.assertOutput(sources, temp);
    }

    @Test
    void recompilesIfExpired(
        @TempDir final Path sources,
        @TempDir final Path temp
    ) throws Exception {
        this.writeSourceFile(sources);
        final Path xml = temp.resolve("foo.xml");
        this.writeFile(new TextOf("INVALID"), xml);
        Assertions.assertTrue(
            xml.toFile().setLastModified(0L)
        );
        this.assertOutput(sources, temp);
    }

    /**
     * Write to file.
     * @param data Data.
     * @param file File.
     */
    private void writeFile(final Text data, final Path file) {
        new Unchecked<>(new LengthOf(new TeeInput(data, file))).value();
    }

    /**
     * Run program & check output.
     * @param sources Source dir.
     * @param temp Temp dir.
     * @throws Exception If program fails.
     */
    private void assertOutput(final Path sources, final Path temp) throws Exception {
        final Program program = new Program(sources, temp);
        final XML test = program.apply("\\Phi.test");
        MatcherAssert.assertThat(
            test.xpath("@name").get(0),
            Matchers.equalTo("test")
        );
    }

    /**
     * Write source code.
     * @param dir Output dir.
     */
    private void writeSourceFile(final Path dir) {
        this.writeFile(
            new TextOf(
                new ResourceOf("org/polystat/test.eo")
            ),
            dir.resolve("test.eo")
        );
    }
}
