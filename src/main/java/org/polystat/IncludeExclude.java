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

import java.util.Collection;
import picocli.CommandLine;

/**
 * Mutually exclusive arguments --include and --exclude.
 * @since 1.0
 */
final class IncludeExclude {

    /**
     * These rules will be excluded from the output.
     */
    @CommandLine.Option(names = "--exclude", split = ",", required = true)
    private Collection<String> exclude;

    /**
     * Only these rules will be included in the output.
     */
    @CommandLine.Option(names = "--include", split = ",", required = true)
    private Collection<String> include;

    /**
     * Ctor.
     */
    IncludeExclude() {
        // nothing
    }

    /**
     * Must this result reach the output?
     * @param res The result of one analysis
     * @return TRUE if the rule of the result is not filtered out
     */
    boolean allows(final Result res) {
        final boolean allows;
        if (this.exclude == null) {
            allows = this.include.stream().anyMatch(
                rule -> res.ruleId().equals(rule)
            );
        } else {
            allows = this.exclude.stream().anyMatch(
                rule -> !res.ruleId().equals(rule)
            );
        }
        return allows;
    }
}
