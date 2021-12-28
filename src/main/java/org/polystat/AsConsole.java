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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Turn list of errors into a JSON report in SARIF format.
 *
 * @since 1.0
 */
final class AsConsole implements Supplier<String> {

    /**
     * Errors.
     */
    private final Map<Analysis, List<String>> errors;

    /**
     * Ctor.
     * @param errs Errors
     */
    AsConsole(final Map<Analysis, List<String>> errs) {
        this.errors = Collections.unmodifiableMap(errs);
    }

    @Override
    public String get() {
        final List<String> lines = new LinkedList<>();
        for (final Map.Entry<Analysis, List<String>> ent : this.errors.entrySet()) {
            for (final String error : ent.getValue()) {
                lines.add(
                    String.format(
                        "RESULT BY %s:\n\t%s",
                        ent.getKey().getClass().getSimpleName(),
                        error.replace("\n", "\n\t")
                    )
                );
            }
        }
        if (lines.isEmpty()) {
            lines.add("No errors found");
        }
        return String.join("\n", lines);
    }
}
