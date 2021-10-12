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

import com.jcabi.log.Logger;
import com.jcabi.xml.ClasspathSources;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.jcabi.xml.XSL;
import com.jcabi.xml.XSLDocument;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.xml.transform.stream.StreamSource;
import org.cactoos.Func;
import org.cactoos.io.InputStreamOf;
import org.cactoos.io.OutputTo;
import org.cactoos.io.ResourceOf;
import org.cactoos.list.ListOf;
import org.cactoos.list.Mapped;
import org.cactoos.text.TextOf;
import org.cactoos.text.UncheckedText;
import org.eolang.parser.Spy;
import org.eolang.parser.Xsline;
import org.polystat.Analysis;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Finding bugs via reverses.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class Reverses implements Analysis {

    // @todo #1:1h The current implementation of the method is not perfect,
    //  because Xsline doesn't allow anything aside from XSL to be inside it.
    //  I suggest we add new functionality to Xsline and let it have
    //  not only XSL but also Cactoos Func-s inside.
    //
    @Override
    public Collection<String> errors(final Func<String, XML> xmir,
        final String locator) throws Exception {
        final Collection<String> bugs = new LinkedList<>();
        final XML obj = xmir.apply(locator);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new Xsline(obj, new OutputTo(baos), new Spy.Verbose(), new ListOf<>())
            .with(Reverses.xsl("expected.xsl").with("expected", "\\perp"))
            .with(Reverses.xsl("data-to-attrs.xsl"))
            .with(Reverses.xsl("reverses.xsl"))
            .with(
                Reverses.xsl("calculate.xsl").with(
                    (href, base) -> new StreamSource(
                        new InputStreamOf(
                            new Calc(
                                new UncheckedText(
                                    new TextOf(
                                        new ResourceOf(
                                            "org/polystat/far/rules.txt"
                                        )
                                    )
                                ).asString().trim()
                            ).xsl().toString()
                        )
                    )
                ),
                (before, after) -> !after.nodes("//r").isEmpty()
            )
            .with(Reverses.xsl("cleanup-outsiders.xsl"))
            .with(Reverses.xsl("taus-to-tree.xsl"))
            .with(Reverses.xsl("unmatch-data.xsl").with("never", Expr.NEVER))
            .with(
                Reverses.xsl("cleanup-conflicts.xsl"),
                (before, after) -> !before.toString().equals(after.toString())
            )
            .with(Reverses.xsl("opts-to-expressions.xsl"))
            .with(Reverses.xsl("expressions-to-inputs.xsl"))
            .with(Reverses.xsl("cleanup-perps.xsl"))
            .pass();
        XML out = new XMLDocument(
            baos.toString(StandardCharsets.UTF_8.name())
        );
        final Directives dirs = new Directives();
        final List<XML> inputs = out.nodes("/o/input");
        for (int idx = 0; idx < inputs.size(); ++idx) {
            final String found = new Expr(
                inputs.get(idx).xpath("expr/text()").get(0)
            ).find();
            dirs.xpath(String.format("/o/input[%d]", idx + 1));
            dirs.attr("found", found);
        }
        out = Reverses.xsl("remove-false-inputs.xsl").transform(
            new XMLDocument(new Xembler(dirs).applyQuietly(out.node()))
        );
        Logger.debug(this, "XML output:%n%s", out);
        for (final XML bug : out.nodes("/o/input[@found]")) {
            bugs.add(
                String.format(
                    "\\perp at {%s}",
                    String.join(
                        ", ",
                        new Mapped<>(
                            attr -> String.format(
                                "%s=%s", attr.xpath("@attr").get(0),
                                attr.xpath("@x").get(0)
                            ),
                            bug.nodes("a")
                        )
                    )
                )
            );
        }
        return bugs;
    }

    /**
     * Make XSL.
     * @param name Name of it
     * @return A new XSL
     * @throws IOException If fails
     */
    private static XSL xsl(final String name) throws IOException {
        return new XSLDocument(
            new TextOf(
                new ResourceOf(
                    String.format("org/polystat/far/%s", name)
                )
            ).asString()
        ).with(new ClasspathSources());
    }

}
