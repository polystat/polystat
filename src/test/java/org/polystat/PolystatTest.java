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

import com.jcabi.log.VerboseProcess;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import org.cactoos.io.InputOf;
import org.cactoos.io.OutputTo;
import org.cactoos.io.ResourceOf;
import org.cactoos.io.TeeInput;
import org.cactoos.scalar.LengthOf;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test case for {@link Polystat}.
 *
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class PolystatTest {

    @Test
    void printsVersion() throws Exception {
        MatcherAssert.assertThat(
            PolystatTest.exec("--version"),
            Matchers.allOf(
                Matchers.containsString("."),
                Matchers.not(Matchers.containsString(" "))
            )
        );
    }

    @Test
    void printsHelp() throws Exception {
        MatcherAssert.assertThat(
            PolystatTest.exec("--help"),
            Matchers.containsString("Usage: ")
        );
    }

    @Test
    void analyzesOneEolangProgram(@TempDir final Path sources,
        @TempDir final Path temp) throws Exception {
        Files.write(
            sources.resolve("test.eo"),
            new TextOf(
                new ResourceOf("org/polystat/test.eo")
            ).asString().getBytes(StandardCharsets.UTF_8)
        );
        MatcherAssert.assertThat(
            PolystatTest.exec(
                "--sarif",
                sources.toAbsolutePath().toString(),
                temp.toAbsolutePath().toString()
            ),
            Matchers.containsString("\\perp")
        );
    }

    /**
     * Execute it.
     * @param cmds Opts
     * @return Output
     * @throws Exception If fails
     */
    private static String exec(final String... cmds) throws Exception {
        final Collection<String> args = new LinkedList<>();
        args.add("java");
        args.add("-Dfile.encoding=utf-8");
        args.add("-cp");
        args.add(System.getProperty("java.class.path"));
        args.add(Polystat.class.getCanonicalName());
        args.addAll(Arrays.asList(cmds));
        final Process proc = new ProcessBuilder().command(
            args.toArray(new String[0])
        ).redirectErrorStream(true).start();
        final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        try (VerboseProcess vproc = new VerboseProcess(proc)) {
            new LengthOf(
                new TeeInput(
                    new InputOf(vproc.stdout()),
                    new OutputTo(stdout)
                )
            ).value();
        }
        return new String(stdout.toByteArray(), StandardCharsets.UTF_8)
            .replaceFirst("Picked up .*\n", "");
    }

}
