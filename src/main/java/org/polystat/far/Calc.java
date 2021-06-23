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

import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.jcabi.xml.XSL;
import com.jcabi.xml.XSLDocument;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.cactoos.io.ResourceOf;
import org.cactoos.iterable.IterableOf;
import org.cactoos.iterable.Joined;
import org.cactoos.iterable.Mapped;
import org.cactoos.text.TextOf;
import org.cactoos.text.UncheckedText;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Make XSL from rules.txt.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class Calc {

    /**
     * Left part of a rule.
     */
    private static final Pattern LEFT = Pattern.compile(
        "^([a-z.]+)\\(([^)]+)\\)$"
    );

    /**
     * Right part of a rule.
     */
    private static final Pattern RIGHT = Pattern.compile(
        "\\{([^}^{]+)}"
    );

    /**
     * Text rules.
     */
    private final String rules;

    /**
     * Ctor.
     * @param rls Rules
     */
    public Calc(final String rls) {
        this.rules = rls;
    }

    /**
     * Make it.
     * @return The XSL
     */
    public XSL xsl() {
        final XML xml = new XMLDocument(
            new Xembler(
                new Directives().add("rules").append(
                    new Joined<>(
                        new Mapped<>(
                            Calc::toDirs,
                            new IterableOf<>(this.rules.split("\n"))
                        )
                    )
                )
            ).domQuietly()
        );
        return new XSLDocument(
            new XSLDocument(
                new UncheckedText(
                    new TextOf(
                        new ResourceOf(
                            "org/polystat/far/build-calc-function.xsl"
                        )
                    )
                ).asString()
            ).applyTo(xml)
        );
    }

    /**
     * Turn a rule into Xembly directives.
     * @param rule The rule in text
     * @return Directives
     */
    private static Directives toDirs(final String rule) {
        final String[] parts = rule.split(" -> ");
        final Matcher left = Calc.LEFT.matcher(parts[0]);
        if (!left.matches()) {
            throw new IllegalStateException(
                String.format("Wrong left part in line '%s'", parts[0])
            );
        }
        final Matcher right = Calc.RIGHT.matcher(parts[1]);
        final Directives dirs = new Directives().add("rule")
            .add("f").set(left.group(1)).up()
            .add("y").set(left.group(2)).up()
            .add("inputs");
        while (right.find()) {
            dirs.add("input");
            final String[] inputs = right.group(1).split(" ");
            for (final String input : inputs) {
                dirs.add("x").set(input).up();
            }
            dirs.up();
        }
        return dirs.up().up();
    }

}
