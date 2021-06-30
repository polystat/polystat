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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import org.cactoos.io.ResourceOf;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.polystat.XMIR;

/**
 * Test case for {@link Reverses}.
 *
 * @since 0.1
 */
public final class ReversesTest {

    @Test
    public void findsReversesInSimpleXml() throws Exception {
        final Path sources = Files.createTempDirectory("sources");
        Files.write(
            sources.resolve("test.eo"),
            new TextOf(
                new ResourceOf("org/polystat/tests/div-by-zero.eo")
            ).asString().getBytes(StandardCharsets.UTF_8)
        );
        final Path temp = Files.createTempDirectory("temp");
        final Reverses reverses = new Reverses(new XMIR(sources, temp));
        final Collection<String> bugs = reverses.errors("\\Phi.foo");
        MatcherAssert.assertThat(
            bugs,
            Matchers.iterableWithSize(Matchers.not(Matchers.greaterThan(3)))
        );
        Logger.debug(this, "Bugs found: %s", bugs);
    }

}
