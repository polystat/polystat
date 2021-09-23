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
package org.polystat;

import com.jcabi.xml.XML;
import java.util.List;

/**
 * XMIR representation of the EO program.
 * @since 1.0
 */
public final class Xmir implements EoRepresentation<XML> {

    /**
     * Where to look for EO sources.
     */
    private final EoSource src;

    /**
     * Ctor.
     * @param src Where to look for source code.
     */
    public Xmir(final EoSource src) {
        this.src = src;
    }

    @Override
    public XML repr(final String locator) throws Exception {
        XML obj = this.src.xmir(locator).nodes("/program/objects").get(0);
        final String[] parts = locator.split("\\.");
        for (int idx = 1; idx < parts.length; ++idx) {
            final List<XML> objs = obj.nodes(
                String.format("o[@name='%s']", parts[idx])
            );
            obj = objs.get(0);
        }
        return obj;
    }
}
