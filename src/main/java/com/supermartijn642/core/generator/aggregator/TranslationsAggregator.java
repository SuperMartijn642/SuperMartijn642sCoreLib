package com.supermartijn642.core.generator.aggregator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created 06/05/2023 by SuperMartijn642
 */
public class TranslationsAggregator implements ResourceAggregator<Map<String,String>,Map<String,String>> {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final TranslationsAggregator INSTANCE = new TranslationsAggregator();

    private TranslationsAggregator(){
    }

    @Override
    public Map<String,String> initialData(){
        return new LinkedHashMap<>();
    }

    @Override
    public Map<String,String> combine(Map<String,String> data, Map<String,String> newData){
        newData.forEach((key, translation) -> {
            String oldValue = data.put(key, translation);
            if(oldValue != null && !oldValue.equals(translation))
                throw new IllegalArgumentException("Conflicting translations for key '" + key + "'!");
        });
        return data;
    }

    @Override
    public void write(OutputStream stream, Map<String,String> data) throws IOException{
        JsonObject json = new JsonObject();
        data.forEach(json::addProperty);
        try(Writer writer = new OutputStreamWriter(stream)){
            GSON.toJson(json, writer);
        }
    }
}
