/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2021 Polystat.org
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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.cactoos.io.ResourceOf;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test case for {@link Polystat}.
 *
 * @since 0.1
 */
final class PolystatTest {

    @Test
    void publicEntrance() throws Exception {
        Polystat.main();
    }

    @Test
    void saysHello() throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        new Polystat(new PrintStream(out), "\\Phi.foo").exec();
        MatcherAssert.assertThat(
            out.toString(),
            Matchers.containsString("README")
        );
    }

    @Test
    void analyzesOneEolangProgram(@TempDir final Path sources,
        @TempDir final Path temp) throws Exception {
        Files.write(
            sources.resolve("foo.eo"),
            new TextOf(
                new ResourceOf("org/polystat/tests/div-by-zero.eo")
            ).asString().getBytes(StandardCharsets.UTF_8)
        );
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        new Polystat(new PrintStream(out), "\\Phi.foo").exec(
            sources.toAbsolutePath().toString(),
            temp.toAbsolutePath().toString()
        );
        MatcherAssert.assertThat(
            out.toString(),
            Matchers.notNullValue()
        );
    }

}
