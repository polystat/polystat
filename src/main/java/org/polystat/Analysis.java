/*
 * SPDX-FileCopyrightText: Copyright (c) 2020-2022 Polystat.org
 * SPDX-License-Identifier: MIT
 */

package org.polystat;

import com.jcabi.xml.XML;
import org.cactoos.Func;

/**
 * An interface every analysis method has to implement.
 * @since 0.2
 */
@FunctionalInterface
public interface Analysis {

    /**
     * Analyse the specified object in the provided XMIR
     * and return the list of errors found.
     * @param xmir The XMIR
     * @param locator The locator of the object, for example "\\Phi.foo"
     * @return List of exceptions
     * @throws Exception If fails
     */
    Iterable<Result> errors(Func<String, XML> xmir,
        String locator) throws Exception;
}
