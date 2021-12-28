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

import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import org.cactoos.Func;
import org.cactoos.list.ListOf;

/**
 * Main entrance.
 *
 * @since 1.0
 * @todo #1:1h Let's use some library for command line arguments parsing.
 *  The current implementation in this class is super primitive and must
 *  be replaced by something decent.
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class Polystat {

    /**
     * Analyzers.
     */
    private static final Analysis[] ALL = {
        new AnFaR(),
        new AnOdin(),
    };

    /**
     * Main entrance for Java command line.
     * @param args The args
     * @throws Exception If fails
     */
    public static void main(final String... args) throws Exception {
        new Polystat().exec(args);
    }

    /**
     * Run it.
     * @param args The args
     * @throws Exception If fails
     * @todo #1:1h For some reason, the Logger.info() doesn't print anything
     *  to the console when the JAR is run via command line. It does print
     *  during testing, but doesn't show anything when being run as
     *  a JAR-with-dependencies. I didn't manage to find the reason, that's
     *  why added these "printf" instructions. Let's fix it, make sure
     *  Logger works in the JAR-with-dependencies, and remove this.stdout
     *  from this class at all.
     * @checkstyle ExecutableStatementCountCheck (200 lines)
     * @checkstyle ReturnCountCheck (200 lines)
     */
    @SuppressWarnings("PMD.OnlyOneReturn")
    public void exec(final String... args) throws Exception {
        final List<String> opts = new ArrayList<>(args.length);
        opts.addAll(Arrays.asList(args));
        boolean sarif = false;
        while (!opts.isEmpty()) {
            final String opt = opts.get(0);
            if ("--version".equals(opt)) {
                Logger.info(this, Polystat.version());
                return;
            }
            if ("--help".equals(opt)) {
                Logger.info(
                    this,
                    String.join(
                        "\n",
                        "Usage: java -jar polystat.jar [option...] <src> <temp>",
                        "  src: Directory with .eo sources",
                        "  tmp: Directory for temporary .xmir and other files",
                        "  options:",
                        "    --help     Print this documentation and exit",
                        "    --version  Print the version of this JAR and exit",
                        "    --sarif    Print JSON output in SARIF 2.0 format"
                    )
                );
                return;
            }
            if ("--sarif".equals(opt)) {
                sarif = true;
            }
            if (!opt.startsWith("--")) {
                break;
            }
            opts.remove(0);
        }
        if (opts.size() != 2) {
            Logger.error(
                this, "Two directory names required as arguments, run with --help"
            );
            return;
        }
        final Map<Analysis, List<String>> errors =
            Polystat.scan(Paths.get(opts.get(0)), Paths.get(opts.get(1)));
        final Supplier<String> out;
        if (sarif) {
            out = new AsSarif(errors);
        } else {
            out = new AsConsole(errors);
        }
        Logger.info(this, "%s\n", out.get());
    }

    /**
     * Scan.
     * @param src Path with sources
     * @param tmp Path with temp files
     * @return Errors
     * @throws Exception If fails
     */
    private static Map<Analysis, List<String>> scan(final Path src, final Path tmp)
        throws Exception {
        final Func<String, XML> xmir = new Program(src, tmp);
        final Map<Analysis, List<String>> errors = new HashMap<>(Polystat.ALL.length);
        for (final Analysis analysis : Polystat.ALL) {
            errors.put(
                analysis,
                new ListOf<>(analysis.errors(xmir, "\\Phi.test"))
            );
        }
        return errors;
    }

    /**
     * Read the version from resources and prints it.
     * @return Version as string
     * @throws IOException If fails
     */
    private static String version() throws IOException {
        try (BufferedReader input =
            new BufferedReader(
                new InputStreamReader(
                    Objects.requireNonNull(
                        Polystat.class.getResourceAsStream("version.txt")
                    ),
                    StandardCharsets.UTF_8
                )
            )
        ) {
            return input.lines().findFirst().get();
        }
    }

}
