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

import com.jcabi.log.VerboseProcess;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.cactoos.io.InputOf;
import org.cactoos.io.OutputTo;
import org.cactoos.io.ResourceOf;
import org.cactoos.io.TeeInput;
import org.cactoos.list.ListOf;
import org.cactoos.scalar.LengthOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test case for {@link Polystat}.
 * @since 0.1
 * @todo #63:1h/DEV Almost all PolystatITCase tests are broken and don't start inside maven jobs.
 *  These tests affirm that end-2-end usage is working, thus we should enable it and make
 *  sure everything is working properly.
 */
final class PolystatITCase {

    /**
     * Directory of resource EO files.
     */
    private static final String DIR = "org/polystat/";

    @Test
    void saysHello() throws Exception {
        MatcherAssert.assertThat(
            PolystatITCase.exec(),
            Matchers.containsString("README")
        );
    }

    @Test
    void printsVersion() throws Exception {
        MatcherAssert.assertThat(
            PolystatITCase.exec("--version"),
            Matchers.allOf(
                Matchers.containsString("."),
                Matchers.not(Matchers.containsString(" "))
            )
        );
    }

    @Test
    void printsHelp() throws Exception {
        MatcherAssert.assertThat(
            PolystatITCase.exec("--help"),
            Matchers.containsString("Usage: ")
        );
    }

    @Test
    void analyzesOneEolangProgram(
        @TempDir final Path sources,
        @TempDir final Path temp
    ) throws Exception {
        final String file = "test.eo";
        PolystatITCase.writeFile(
            sources, file, String.format("%s%s", PolystatITCase.DIR, file)
        );
        MatcherAssert.assertThat(
            PolystatITCase.exec(
                "--sarif",
                sources.toAbsolutePath().toString(),
                temp.toAbsolutePath().toString()
            ),
            Matchers.containsString("\\perp")
        );
    }

    @Test
    void analyzesMultipleEolangPrograms(
        @TempDir final Path temp,
        @TempDir final Path sources
    ) throws Exception {
        for (final String file : new ListOf<>("test.eo", "five.eo")) {
            PolystatITCase.writeFile(
                sources, file, String.format("%s%s", PolystatITCase.DIR, file)
            );
        }
        PolystatITCase.exec(
            "--files", sources.toString(), "--tmp", temp.toString()
        );
        MatcherAssert.assertThat(
            sources.toFile().list().length,
            Matchers.equalTo(temp.toFile().list().length)
        );
    }

    private static String exec(final String... cmds) throws Exception {
        final List<String> args = new ArrayList<>(0);
        args.add("java");
        args.add("-Dfile.encoding=utf-8");
        args.add("-cp");
        args.add(System.getProperty("java.class.path"));
        args.add(Polystat.class.getCanonicalName());
        args.addAll(Arrays.asList(cmds));
        final Process proc = new ProcessBuilder()
            .command(args)
            .redirectErrorStream(true)
            .start();
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
            .replaceFirst("Picked up .*\\R", "");
    }

    private static void writeFile(
        final Path path,
        final String name,
        final String resource
    ) throws Exception {
        new LengthOf(
            new TeeInput(
                new ResourceOf(resource),
                new OutputTo(path.resolve(name))
            )
        ).value();
    }
}
