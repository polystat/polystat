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
package org.polystat.far;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link Expr}.
 *
 * @since 0.1
 * @todo #1:1h I suggest we refactor this class into a parametrized unit test.
 *  As inputs we should probably have the expressions and the output
 *  should be the expected result of the Expr class.
 */
final class ExprTest {

    @Test
    void solvesSimpleExpression() {
        final Expr expr = new Expr(
            "((a=1 ∧ b=2) or (b=3)) and ((a=7) or (b=2 ∧ d=7))"
        );
        MatcherAssert.assertThat(
            expr.find(),
            Matchers.equalTo("a=1 b=2 ➜ a=1 b=2 d=7")
        );
    }

    @Test
    void failsSimpleExpression() {
        final Expr expr = new Expr(
            "((a=1 ∧ b=2) or (d=4)) and ((d=5 ∧ a=7) or (b=6 ∧ d=1))"
        );
        MatcherAssert.assertThat(
            expr.find(),
            Matchers.equalTo("")
        );
    }

    @Test
    void ignoresNever() {
        final Expr expr = new Expr(
            String.format("((a=1 ∧ b=%s)) and ((a=1))", Expr.NEVER)
        );
        MatcherAssert.assertThat(expr.find(), Matchers.equalTo(""));
    }

}
