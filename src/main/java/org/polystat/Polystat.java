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
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.List;
import org.cactoos.Func;
import org.cactoos.list.ListOf;
import org.polystat.far.Reverses;

/**
 * Main entrance.
 *
 * @since 1.0
 * @todo #1:1h Let's use some library for command line arguments parsing.
 *  The current implementation in this class is super primitive and must
 *  be replaced by something decent.
 */
public final class Polystat {

    /**
     * Analyzers.
     */
    private static final Analysis[] ALL = {
        new Reverses(),
    };

    /**
     * The stream to print to.
     */
    private final PrintStream stdout;

    /**
     * Ctor.
     * @param out The stream to print to
     */
    public Polystat(final PrintStream out) {
        this.stdout = out;
    }

    /**
     * Main entrance for Java command line.
     * @param args The args
     * @throws Exception If fails
     */
    public static void main(final String... args) throws Exception {
        Logger.info(Polystat.class, "Polystat (c) 2021");
        new Polystat(System.out).exec(args);
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
     */
    public void exec(final String... args) throws Exception {
        if (args.length == 2) {
            final Func<String, XML> xmir = new XMIR(
                Paths.get(args[0]), Paths.get(args[1])
            );
            for (final Analysis analysis : Polystat.ALL) {
                final List<String> errors = new ListOf<>(
                    analysis.errors(xmir, "\\Phi.foo")
                );
                Logger.info(
                    this, "%d errors found by %s",
                    errors.size(), analysis.getClass()
                );
                for (final String error : errors) {
                    Logger.info(this, "Error: %s", error);
                    this.stdout.printf("Error: %s%n", error);
                }
                if (errors.isEmpty()) {
                    Logger.info(this, "No errors found");
                    this.stdout.println("No errors");
                }
            }
        } else {
            this.stdout.println("Read our README in GitHub");
        }
    }

}
