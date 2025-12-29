package com.supermartijn642.core.generator.aggregator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created 06/05/2023 by SuperMartijn642
 */
public class AtlasSourceAggregator implements ResourceAggregator<Set<Identifier>,Set<Identifier>> {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final AtlasSourceAggregator INSTANCE = new AtlasSourceAggregator();

    @Override
    public Set<Identifier> initialData(){
        return new LinkedHashSet<>();
    }

    @Override
    public Set<Identifier> combine(Set<Identifier> data, Set<Identifier> newData){
        data.addAll(newData);
        return data;
    }

    @Override
    public void write(OutputStream stream, Set<Identifier> data) throws IOException{
        // Create a json array with all the textures
        JsonArray sources = new JsonArray();
        data.forEach(texture -> {
            JsonObject object = new JsonObject();
            object.addProperty("type", "minecraft:single");
            object.addProperty("resource", texture.toString());
            sources.add(object);
        });

        JsonObject json = new JsonObject();
        json.add("sources", sources);
        try(Writer writer = new OutputStreamWriter(stream)){
            GSON.toJson(json, writer);
        }
    }
}
