package com.supermartijn642.core.util;

/**
 * Created 23/07/2022 by SuperMartijn642
 */
public class Pair<X, Y> {

    public static <X,Y> Pair<X,Y> of(X left, Y right){
        return new Pair<>(left, right);
    }

    private final X left;
    private final Y right;

    public Pair(X left, Y right){
        this.left = left;
        this.right = right;
    }

    public X left(){
        return this.left;
    }

    public Y right(){
        return this.right;
    }
}
