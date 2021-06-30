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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Boolean Expression Solver.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class Expr {

    /**
     * The expression.
     */
    private final String expr;

    /**
     * Already defined taus.
     */
    private final Map<String, String> already;

    /**
     * Ctor.
     * @param bexpr Boolean expression
     */
    public Expr(final String bexpr) {
        this(new HashMap<>(0), bexpr);
    }

    /**
     * Ctor.
     * @param map Already defined values
     * @param bexpr Boolean expression
     */
    public Expr(final Map<String, String> map, final String bexpr) {
        this.already = Collections.unmodifiableMap(map);
        this.expr = bexpr;
    }

    /**
     * Is it possible?
     * @return TRUE if yes
     */
    public String find() {
        final String[] ands = this.expr.split(" and ", 2);
        final String[] ors = ands[0].substring(1, ands[0].length() - 1)
            .split(" or ");
        final Collection<Map<String, String>> parts =
            new ArrayList<>(ors.length);
        for (final String item : ors) {
            final String[] taus = item.substring(1, item.length() - 1)
                .split(" & ");
            final Map<String, String> opts = new HashMap<>(taus.length);
            for (final String tau : taus) {
                final String[] eqs = tau.split("=");
                opts.put(eqs[0], eqs[1]);
            }
            parts.add(opts);
        }
        String result = "";
        for (final Map<String, String> map : parts) {
            if (!this.matches(map)) {
                continue;
            }
            final Map<String, String> join = this.merge(map);
            final String out = Expr.print(join);
            if (ands.length == 1) {
                result = out;
                break;
            }
            final String kid = new Expr(join, ands[1]).find();
            if (!kid.isEmpty()) {
                result = String.format("%s > %s", out, kid);
                break;
            }
        }
        return result;
    }

    /**
     * Print the map to string.
     * @param map The map
     * @return String
     */
    private static String print(final Map<String, String> map) {
        final StringBuilder out = new StringBuilder();
        for (final Map.Entry<String, String> ent : map.entrySet()) {
            out.append(ent.getKey())
                .append('=')
                .append(ent.getValue())
                .append(' ');
        }
        return out.toString().trim();
    }

    /**
     * This map matches the already defined values.
     * @param map The map
     * @return TRUE if matches
     */
    private boolean matches(final Map<String, String> map) {
        boolean matches = true;
        for (final Map.Entry<String, String> ent : this.already.entrySet()) {
            if (map.containsKey(ent.getKey()) &&
                map.get(ent.getKey()) != ent.getValue()) {
                matches = false;
                break;
            }
        }
        return matches;
    }

    /**
     * Merge existing map with this one.
     * @param map Appendix
     * @return New map
     */
    private Map<String, String> merge(final Map<String, String> map) {
        final Map<String, String> after = new HashMap<>(
            this.already.size() + map.size()
        );
        after.putAll(this.already);
        after.putAll(map);
        return after;
    }

}
