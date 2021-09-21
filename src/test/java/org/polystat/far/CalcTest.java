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

import com.jcabi.matchers.XhtmlMatchers;
import org.cactoos.io.ResourceOf;
import org.cactoos.text.TextOf;
import org.cactoos.text.UncheckedText;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link Calc}.
 *
 * @since 0.1
 */
final class CalcTest {

    @Test
    void buildsSimpleRulesXsl() {
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(new Calc("add(y) -> {{y 0}}").xsl()),
            Matchers.allOf(
                XhtmlMatchers.hasXPath("//xsl:stylesheet"),
                XhtmlMatchers.hasXPath("//xsl:function[@name='ps:calc']"),
                XhtmlMatchers.hasXPath("//xsl:when[@test=\"$y = '\\any'\"]"),
                XhtmlMatchers.hasXPath("//xsl:when[@test=\"$func = 'add'\"]")
            )
        );
    }

    @Test
    void buildsRealRulesXsl() {
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new Calc(
                    new UncheckedText(
                        new TextOf(
                            new ResourceOf(
                                "org/polystat/far/rules.txt"
                            )
                        )
                    ).asString().trim()
                ).xsl()
            ),
            XhtmlMatchers.hasXPath("//xsl:choose[count(xsl:when)=7]")
        );
    }

}
