package com.supermartijn642.core.util;

import java.util.Objects;

/**
 * Created 24/07/2022 by SuperMartijn642
 */
@FunctionalInterface
public interface TriPredicate<X, Y, Z> {

    boolean test(X x, Y y, Z z);

    default TriPredicate<X,Y,Z> and(TriPredicate<? super X,? super Y,? super Z> other){
        Objects.requireNonNull(other);
        return (x, y, z) -> this.test(x, y, z) && other.test(x, y, z);
    }

    default TriPredicate<X,Y,Z> negate(){
        return (x, y, z) -> !this.test(x, y, z);
    }

    default TriPredicate<X,Y,Z> or(TriPredicate<? super X,? super Y,? super Z> other){
        Objects.requireNonNull(other);
        return (x, y, z) -> this.test(x, y, z) || other.test(x, y, z);
    }
}
