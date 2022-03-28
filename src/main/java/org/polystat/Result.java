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

import java.util.Iterator;
import java.util.Optional;

/**
 * Analysis result.
 *
 * @since 1.0
 */
public interface Result extends Iterable<String> {

    /**
     * Analysis type.
     * @return Class.
     */
    Class<? extends Analysis> analysis();

    /**
     * Rule id.
     * @return Id of a rule executed in the analysis
     */
    String ruleId();

    /**
     * Failure if occurred.
     * @return Present if analysis failed.
     */
    Optional<? extends Throwable> failure();

    /**
     * Completed analysis result.
     *
     * @since 1.0
     */
    final class Completed implements Result {

        /**
         * Type of Analysis.
         */
        private final Class<? extends Analysis> type;

        /**
         * Found errors.
         */
        private final Iterable<String> errors;

        /**
         * ID of the rule that was run.
         */
        private final String ruleid;

        /**
         * Ctor.
         * @param type Type.
         * @param errors Errors.
         * @param ruleid ID of the rule that was run.
         */
        public Completed(
            final Class<? extends Analysis> type,
            final Iterable<String> errors,
            final String ruleid
        ) {
            this.type = type;
            this.errors = errors;
            this.ruleid = ruleid;
        }

        @Override
        public Class<? extends Analysis> analysis() {
            return this.type;
        }

        @Override
        public Optional<? extends Throwable> failure() {
            return Optional.empty();
        }

        @Override
        public Iterator<String> iterator() {
            return this.errors.iterator();
        }

        @Override
        public String ruleId() {
            return this.ruleid;
        }
    }

    /**
     * Failed Analysis result.
     *
     * @since 1.0
     */
    final class Failed implements Result {

        /**
         * Type of Analysis.
         */
        private final Class<? extends Analysis> type;

        /**
         * Failure.
         */
        private final Throwable error;

        /**
         * Rule id.
         */
        private final String ruleid;

        /**
         * Ctor.
         * @param type Analysis.
         * @param error Exception.
         * @param ruleid ID of the rule that was run.
         */
        public Failed(
            final Class<? extends Analysis> type,
            final Throwable error,
            final String ruleid
        ) {
            this.type = type;
            this.error = error;
            this.ruleid = ruleid;
        }

        @Override
        public Class<? extends Analysis> analysis() {
            return this.type;
        }

        @Override
        public Optional<? extends Throwable> failure() {
            return Optional.of(this.error);
        }

        @Override
        public Iterator<String> iterator() {
            throw new IllegalStateException(this.error);
        }

        @Override
        public String ruleId() {
            return this.ruleid;
        }
    }
}
