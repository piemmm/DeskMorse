package org.prowl.deskmorse.config;

import java.io.IOException;

public class BadConfigException extends IOException {

    public BadConfigException(String message) {
        super(message);
    }
}
