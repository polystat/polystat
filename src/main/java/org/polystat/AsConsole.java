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

import com.jcabi.log.Logger;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Turn list of errors into a console report.
 *
 * @since 1.0
 */
final class AsConsole implements Supplier<String> {

    /**
     * Errors.
     */
    private final Iterable<Result> errors;

    /**
     * Ctor.
     * @param errs Errors
     */
    AsConsole(final Iterable<Result> errs) {
        this.errors = errs;
    }

    @Override
    public String get() {
        final List<String> lines = new LinkedList<>();
        for (final Result ent : this.errors) {
            if (ent.failure().isPresent()) {
                Logger.warn(Polystat.class, "%[exception]s", ent.failure().get());
            } else {
                for (final String error : ent) {
                    lines.add(
                        String.format(
                            "RESULT BY %s:\n\t%s",
                            ent.analysis().getSimpleName(),
                            error.replace("\n", "\n\t")
                        )
                    );
                }
            }
        }
        if (lines.isEmpty()) {
            lines.add("No errors found by Polystat analyzers");
        }
        return String.join("\n", lines);
    }
}
