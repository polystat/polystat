/*
 * SPDX-FileCopyrightText: Copyright (c) 2020-2022 Polystat.org
 * SPDX-License-Identifier: MIT
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
