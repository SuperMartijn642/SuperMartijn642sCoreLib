package com.supermartijn642.core.generator;

/**
 * Created 04/08/2022 by SuperMartijn642
 */
public enum ResourceType {

    DATA("data"), ASSET("assets");

    private final String directory;

    ResourceType(String directory){
        this.directory = directory;
    }

    public String getDirectoryName(){
        return this.directory;
    }
}
