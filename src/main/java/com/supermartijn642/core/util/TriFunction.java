package com.supermartijn642.core.util;

import java.util.Objects;
import java.util.function.Function;

/**
 * Created 16/08/2022 by SuperMartijn642
 */
@FunctionalInterface
public interface TriFunction<R, S, T, U> {

    U apply(R r, S s, T t);

    default <V> TriFunction<R,S,T,V> andThen(Function<? super U,? extends V> after){
        Objects.requireNonNull(after);
        return (r, s, t) -> after.apply(this.apply(r, s, t));
    }
}
