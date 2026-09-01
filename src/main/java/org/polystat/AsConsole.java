/*
 * SPDX-FileCopyrightText: Copyright (c) 2020-2022 Polystat.org
 * SPDX-License-Identifier: MIT
 */
package org.polystat;

import com.jcabi.log.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Turn list of errors into a console report.
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
        final List<String> lines = new ArrayList<>(0);
        for (final Result ent : this.errors) {
            if (ent.failure().isPresent()) {
                Logger.warn(Polystat.class, "%[exception]s", ent.failure().get());
            } else {
                for (final String error : ent) {
                    lines.add(
                        String.format(
                            "RESULT BY %s:%n\t%s",
                            ent.analysis().getSimpleName(),
                            error.replaceAll("\\R", String.format("%n\t"))
                        )
                    );
                }
            }
        }
        if (lines.isEmpty()) {
            lines.add("No errors found by Polystat analyzers");
        }
        return String.join(System.lineSeparator(), lines);
    }
}
