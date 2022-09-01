package com.supermartijn642.core.extensions;

import com.google.gson.JsonObject;

/**
 * Created 01/09/2022 by SuperMartijn642
 */
public interface ICriterionInstanceExtension {

    default void coreLibSerialize(JsonObject json){
        throw new RuntimeException("ICriterionInstance class '" + this.getClass().getCanonicalName() + "' does not override ICriterionInstanceExtension#coreLibSerialize and thus is not supported!");
    }
}
