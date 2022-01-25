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

import java.util.List;
import org.cactoos.iterable.IterableOf;
import org.cactoos.list.ListOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link AsSarif}.
 *
 * @since 1.0
 */
final class AsSarifTest {

    @Test
    void addsResults() {
        final List<String> errors = new ListOf<>("x", "y", "z");
        MatcherAssert.assertThat(
            new AsSarif(
                new IterableOf<>(
                    new Result.Completed(
                        Analysis.class,
                        errors
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
                        new UnsupportedOperationException(msg)
                    )
                )
            ).get(),
            Matchers.stringContainsInOrder(
                "exception",
                msg
            )
        );
    }
}
