package com.filmeckiy.bot.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author egor
 */
public abstract class Option<T> {
    public abstract T get();
    public abstract boolean isDefined();

    public boolean isEmpty() {
        return !isDefined();
    }

    public static <T> Option<T> some(T value) {
        return new NotEmptyOption<>(value);
    }

    public static <T> Option<T> none() {
        return new EmptyOption<>();
    }

    @Override
    public String toString() {
        return isDefined()
                ? String.format("Option[%s]", get().toString())
                : "Option[NONE]";
    }

    private static class EmptyOption<T> extends Option<T> {
        @Override
        public T get() {
            throw new RuntimeException("Empty option");
        }

        @Override
        public boolean isDefined() {
            return false;
        }
    }

    private static class NotEmptyOption<T> extends Option<T> {
        private final T value;

        public NotEmptyOption(T value) {
            this.value = value;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public boolean isDefined() {
            return true;
        }
    }
}
