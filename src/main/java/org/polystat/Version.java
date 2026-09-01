/*
 * SPDX-FileCopyrightText: Copyright (c) 2020-2022 Polystat.org
 * SPDX-License-Identifier: MIT
 */
package org.polystat;

import com.jcabi.manifests.Manifests;
import picocli.CommandLine;

/**
 * Versions of Polystat and EO, as the command line prints them.
 * @since 1.0
 */
final class Version implements CommandLine.IVersionProvider {

    /**
     * Ctor.
     */
    Version() {
        // nothing
    }

    @Override
    public String[] getVersion() {
        return new String[]{
            Manifests.read("Polystat-Version"),
            Manifests.read("EO-Version"),
        };
    }
}
