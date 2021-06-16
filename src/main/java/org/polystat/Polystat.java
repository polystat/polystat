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
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * Main entrance.
 *
 * @since 1.0
 */
public final class Polystat {

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
        new Polystat(System.out).exec(args);
    }

    /**
     * Run it.
     * @param args The args
     * @throws Exception If fails
     */
    public void exec(final String... args) throws Exception {
        if (args.length == 2) {
            final Collection<String> errors = new Reverses(
                new XMIR(Paths.get(args[0]), Paths.get(args[1]))
            ).errors("\\Phi.foo");
            Logger.info(this, "%d errors found", errors.size());
            for (final String error : errors) {
                this.stdout.println(error);
            }
        } else {
            this.stdout.println("Hello, world!");
        }
    }

}
