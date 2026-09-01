/*
 * SPDX-FileCopyrightText: Copyright (c) 2020-2022 Polystat.org
 * SPDX-License-Identifier: MIT
 */

package org.polystat;

import com.jcabi.manifests.Manifests;
import java.util.List;
import org.cactoos.iterable.IterableOf;
import org.cactoos.iterable.Repeated;
import org.cactoos.list.ListOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link AsSarif}.
 * @since 1.0
 */
final class AsSarifTest {

    /**
     * Sample value for a ruleId argument.
     */
    private static final String SAMPLE_RULEID = "SAMPLE_RULEID";

    @BeforeAll
    static void addPolystatVersion() {
        Manifests.DEFAULT.put("Polystat-Version", "1.0-SNAPSHOT");
    }

    @Test
    void addsResults() {
        final List<String> errors = new ListOf<>("x", "y", "z");
        MatcherAssert.assertThat(
            new AsSarif(
                new IterableOf<>(
                    new Result.Completed(
                        Analysis.class,
                        errors,
                        AsSarifTest.SAMPLE_RULEID
                    )
                )
            ).get(),
            Matchers.stringContainsInOrder(
                errors
            )
        );
    }

    @Test
    void addsExceptions() {
        final String msg = "OK";
        MatcherAssert.assertThat(
            new AsSarif(
                new IterableOf<>(
                    new Result.Failed(
                        Analysis.class,
                        new UnsupportedOperationException(msg),
                        AsSarifTest.SAMPLE_RULEID
                    )
                )
            ).get(),
            Matchers.stringContainsInOrder(
                "exception",
                msg
            )
        );
    }

    @Test
    void addsResultsWithRuleId() {
        final List<String> errors = new ListOf<>("a", "b", "c");
        MatcherAssert.assertThat(
            new AsSarif(
                new IterableOf<>(
                    new Result.Completed(
                        Analysis.class,
                        errors,
                        AsSarifTest.SAMPLE_RULEID
                    )
                )
            ).get(),
            Matchers.stringContainsInOrder(
                new Repeated<>(errors.size(), AsSarifTest.SAMPLE_RULEID)
            )
        );
    }
}
