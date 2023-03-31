package com.supermartijn642.core.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created 17/07/2022 by SuperMartijn642
 */
public abstract class Maybe<T> {

    private static final Maybe<?> EMPTY = new Empty();

    /**
     * Create a new maybe instance with a present object. The object may be {@code null}.
     * @param object the present object, may be {@code null}
     * @param <T>    type of the object
     * @return the created instance
     */
    public static <T> Maybe<T> of(T object){
        return new Present<>(object);
    }

    /**
     * Creates an empty maybe instance.
     * @param <T> type of the maybe object
     * @return the empty maybe instance
     */
    public static <T> Maybe<T> empty(){
        //noinspection unchecked
        return (Maybe<T>)EMPTY;
    }

    private Maybe(){
    }

    /**
     * Whether the maybe is present or not. Note that the maybe may be present even when its object is {@code null}.
     * @return whether the maybe is present
     */
    public abstract boolean isPresent();

    public abstract <S> Maybe<S> map(Function<T,S> mapper);

    /**
     * Returns the object held by this maybe. If this maybe is empty, this will throw an {@link IllegalStateException}.
     * @return the object held by this maybe
     * @throws IllegalStateException if the maybe is empty
     */
    public abstract T get() throws IllegalStateException;

    /**
     * Returns the object held by this maybe. If this maybe is empty, the given alternative will be returned.
     */
    public abstract T orElse(T other);

    /**
     * Returns the object held by this maybe. If this maybe is empty, the given alternative will be resolved and returned.
     */
    public abstract T orElseGet(Supplier<T> other);

    /**
     * Applies the given consumer if this maybe is present.
     */
    public abstract void ifPresent(Consumer<T> consumer);

    private static class Present<T> extends Maybe<T> {

        private final T object;

        private Present(T object){
            this.object = object;
        }

        @Override
        public boolean isPresent(){
            return true;
        }

        @Override
        public <S> Maybe<S> map(Function<T,S> mapper){
            return Maybe.of(mapper.apply(this.object));
        }

        @Override
        public T get(){
            return this.object;
        }

        @Override
        public T orElse(T other){
            return this.object;
        }

        @Override
        public T orElseGet(Supplier<T> other){
            return this.object;
        }

        @Override
        public void ifPresent(Consumer<T> consumer){
            consumer.accept(this.object);
        }
    }

    private static class Empty extends Maybe<Object> {

        @Override
        public boolean isPresent(){
            return false;
        }

        @Override
        public <S> Maybe<S> map(Function<Object,S> mapper){
            //noinspection unchecked
            return (Maybe<S>)this;
        }

        @Override
        public Object get(){
            throw new IllegalStateException("Cannot call get on an empty maybe!");
        }

        @Override
        public Object orElse(Object other){
            return other;
        }

        @Override
        public Object orElseGet(Supplier<Object> other){
            return other.get();
        }

        @Override
        public void ifPresent(Consumer<Object> consumer){
        }
    }
}
