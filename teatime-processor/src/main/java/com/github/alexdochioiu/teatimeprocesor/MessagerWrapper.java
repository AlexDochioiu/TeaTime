/*
 * Copyright 2018 Alexandru Iustin Dochioiu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.alexdochioiu.teatimeprocesor;

import java.util.Locale;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;

/**
 * Created by Alexandru Iustin Dochioiu on 7/23/2018
 */
class MessagerWrapper {
    private final Messager messager;
    private static MessagerWrapper instance;

    static void initInstance(Messager messager) {
        instance = new MessagerWrapper(messager);
    }

    /**
     * Constructor
     *
     * @param messager the {@link Messager} used for logging compiling messages
     */
    private MessagerWrapper(Messager messager) {
        this.messager = messager;
    }

    /**
     * logs warning message
     *
     * @param warnMessage the message to be logged
     */
    static void logWarning(String warnMessage, Object... args) {
        instance.messager.printMessage(Diagnostic.Kind.WARNING, String.format(Locale.UK, warnMessage, args));
    }

    /**
     * Logs error message. <b>This will stop the compiler as it is seen to be un-recoverable</b>
     *
     * @param errorMessage the failure message
     */
    static void logError(String errorMessage, Object... args) {
        instance.messager.printMessage(Diagnostic.Kind.ERROR, String.format(Locale.UK, errorMessage, args));
    }

    /**
     * logs other message
     *
     * @param otherMessage the message
     */
    static void logOther(String otherMessage, Object... args) {
        instance.messager.printMessage(Diagnostic.Kind.OTHER, String.format(Locale.UK, otherMessage, args));
    }
}
