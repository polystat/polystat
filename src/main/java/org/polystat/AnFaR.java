/*
 * SPDX-FileCopyrightText: Copyright (c) 2020-2022 Polystat.org
 * SPDX-License-Identifier: MIT
 */
package org.polystat;

import com.jcabi.xml.XML;
import org.cactoos.Func;
import org.cactoos.iterable.IterableOf;
import org.polystat.far.FaR;

/**
 * Bridge to FaR analysis module.
 * @see <a href="https://github.com/polystat/far">GitHub</a>
 * @since 0.4
 */
public final class AnFaR implements Analysis {

    /**
     * A rule id for AnFaR analysis.
     */
    private static final String RULE_ID = "DIV0";

    /**
     * Ctor.
     */
    public AnFaR() {
        // nothing
    }

    @Override
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public Iterable<Result> errors(final Func<String, XML> xmir,
        final String locator) {
        Result result;
        try {
            result = new Result.Completed(
                AnFaR.class,
                new FaR().errors(xmir, locator),
                AnFaR.RULE_ID
            );
        // @checkstyle IllegalCatchCheck (1 line)
        } catch (final Exception ex) {
            result = new Result.Failed(AnFaR.class, ex, AnFaR.RULE_ID);
        }
        return new IterableOf<>(result);
    }
}
