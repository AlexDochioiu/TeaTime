package com.github.alexdochioiu.teaspoonprocesor;

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
