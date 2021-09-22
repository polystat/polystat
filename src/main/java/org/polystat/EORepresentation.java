package org.polystat;

import org.cactoos.Func;


public interface EORepresentation<R> {
    R repr(String locator) throws Exception;
}
