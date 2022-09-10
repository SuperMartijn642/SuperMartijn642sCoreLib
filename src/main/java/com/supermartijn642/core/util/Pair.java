package com.supermartijn642.core.util;

import com.google.common.base.Objects;

/**
 * Created 23/07/2022 by SuperMartijn642
 */
public class Pair<X, Y> {

    public static <X,Y> Pair<X,Y> of(X left, Y right){
        return new Pair<>(left, right);
    }

    private final X left;
    private final Y right;

    private Pair(X left, Y right){
        this.left = left;
        this.right = right;
    }

    public X left(){
        return this.left;
    }

    public Y right(){
        return this.right;
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || this.getClass() != o.getClass()) return false;
        Pair<?,?> pair = (Pair<?,?>)o;
        return Objects.equal(this.left, pair.left) && Objects.equal(this.right, pair.right);
    }

    @Override
    public int hashCode(){
        return Objects.hashCode(this.left, this.right);
    }
}
