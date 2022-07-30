package com.supermartijn642.core.util;

/**
 * Simply holds an object.
 * <p>
 * Created 26/07/2022 by SuperMartijn642
 */
public class Holder<T> {

    private T object;

    public Holder(T object){
        this.object = object;
    }

    public Holder(){
    }

    /**
     * Sets the held object.
     * @param object object to be held
     */
    public void set(T object){
        this.object = object;
    }

    /**
     * Gets the held object.
     */
    public T get(){
        return this.object;
    }
}
