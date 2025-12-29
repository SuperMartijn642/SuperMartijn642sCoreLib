package com.supermartijn642.core.generator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.supermartijn642.core.generator.aggregator.AtlasSourceAggregator;
import com.supermartijn642.core.registry.RegistryUtil;
import com.supermartijn642.core.render.TextureAtlases;
import com.supermartijn642.core.util.Pair;
import net.minecraft.resources.Identifier;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created 20/01/2023 by SuperMartijn642
 */
public abstract class AtlasSourceGenerator extends ResourceGenerator {

    private static final Gson GSON = new GsonBuilder().setLenient().create();
    private final Map<Identifier,AtlasBuilder> builders = new HashMap<>();

    public AtlasSourceGenerator(String modid, ResourceCache cache){
        super(modid, cache);
    }

    @Override
    public void save(){
        for(Map.Entry<Identifier,AtlasBuilder> atlas : this.builders.entrySet()){
            Set<Identifier> textures = this.gatherTextures(atlas.getValue());

            // Save the object to the cache
            Identifier identifier = atlas.getKey();
            this.cache.saveResource(ResourceType.ASSET, AtlasSourceAggregator.INSTANCE, textures, identifier.getNamespace(), "atlases", identifier.getPath(), ".json");
        }
    }

    private Set<Identifier> gatherTextures(AtlasBuilder builder){
        // Add the regular textures
        Set<Identifier> textures = new HashSet<>(builder.textures);

        // Gather all parent models
        Set<Identifier> parents = new HashSet<>();
        for(Pair<Identifier,Boolean> model : builder.models){
            Identifier parent = this.readModelData(model.left(), true, textures);
            if(parent != null && model.right())
                parents.add(parent);
        }

        // Keep track of which models have already been processed
        Set<Identifier> done = new HashSet<>();
        builder.models.forEach(pair -> done.add(pair.left()));
        // Add the textures from the parent models
        while(!parents.isEmpty()){
            Identifier model = parents.iterator().next();
            parents.remove(model);
            done.add(model);
            Identifier parent = this.readModelData(model, false, textures);
            if(parent != null && !done.contains(parent))
                parents.add(parent);
        }
        return textures;
    }

    private Identifier readModelData(Identifier model, boolean forced, Set<Identifier> textures){
        // Try to read the file
        Optional<InputStream> optional = this.cache.getExistingResource(ResourceType.ASSET, model.getNamespace(), "models", model.getPath(), ".json");
        if(optional.isEmpty()){
            if(forced)
                throw new RuntimeException("Could not find model '" + model + "' to read textures from!");
            return null;
        }
        // Try reading the model
        Identifier parent = null;
        try{
            JsonObject json = GSON.fromJson(new InputStreamReader(optional.get()), JsonObject.class);
            // Assume the model uses the default model format
            if(json.has("parent") && json.get("parent").isJsonPrimitive() && json.getAsJsonPrimitive("parent").isString()){
                String identifier = json.get("parent").getAsString();
                if(RegistryUtil.isValidIdentifier(identifier))
                    parent = Identifier.parse(identifier);
            }
            if(json.has("textures") && json.get("textures").isJsonObject()){
                for(Map.Entry<String,JsonElement> texture : json.getAsJsonObject("textures").entrySet()){
                    if(texture.getValue().isJsonPrimitive() && texture.getValue().getAsJsonPrimitive().isString()){
                        String identifier = texture.getValue().getAsString();
                        if(RegistryUtil.isValidIdentifier(identifier))
                            textures.add(Identifier.parse(identifier));
                    }
                }
            }
        }catch(Exception ignore){
            return null;
        }
        return parent;
    }

    /**
     * Gets an atlas builder for the given location. The returned atlas builder may be a new atlas builder or an existing one if requested before by any {@code AtlasSourceGenerator} with the same modid.
     * @param identifier location of the atlas
     */
    protected AtlasBuilder atlas(Identifier identifier){
        if(identifier.getPath().startsWith("textures/atlas/") && identifier.getPath().endsWith(".png"))
            identifier = Identifier.fromNamespaceAndPath(identifier.getNamespace(), identifier.getPath().substring("textures/atlas/".length(), identifier.getPath().length() - ".png".length()));
        return this.builders.computeIfAbsent(identifier, i -> new AtlasBuilder(this.modid, i));
    }

    /**
     * Gets an atlas builder for the given location. The returned atlas builder may be a new atlas builder or an existing one if requested before by any {@code AtlasSourceGenerator} with the same modid.
     * @param namespace  namespace of the atlas location
     * @param identifier identifier of the atlas location
     */
    protected AtlasBuilder atlas(String namespace, String identifier){
        return this.atlas(Identifier.fromNamespaceAndPath(namespace, identifier));
    }

    /**
     * Gets an atlas builder for the given location. The returned atlas builder may be a new atlas builder or an existing one if requested before by any {@code AtlasSourceGenerator} with the same modid.
     * @param identifier location of the atlas
     */
    protected AtlasBuilder atlas(String identifier){
        return this.atlas(this.modid, identifier);
    }

    /**
     * Gets an atlas builder for the 'blocks' atlas. The returned atlas builder may be a new atlas builder or an existing one if requested before by any {@code AtlasSourceGenerator} with the same modid.
     */
    protected AtlasBuilder blockAtlas(){
        return this.atlas(TextureAtlases.getBlocks());
    }

    /**
     * Gets an atlas builder for the 'gui' atlas. The returned atlas builder may be a new atlas builder or an existing one if requested before by any {@code AtlasSourceGenerator} with the same modid.
     */
    protected AtlasBuilder guiAtlas(){
        return this.atlas(TextureAtlases.getGUI());
    }

    @Override
    public String getName(){
        return this.modName + " Atlas Source Generator";
    }

    public static class AtlasBuilder {

        private final String modid;
        private final Identifier identifier;
        private final Set<Identifier> textures = new HashSet<>();
        private final List<Pair<Identifier,Boolean>> models = new ArrayList<>();

        private AtlasBuilder(String modid, Identifier identifier){
            this.modid = modid;
            this.identifier = identifier;
        }

        /**
         * Adds the texture at the given location to the texture atlas.
         * @param location location of the texture
         */
        public AtlasBuilder texture(Identifier location){
            this.textures.add(location);
            return this;
        }

        /**
         * Adds the texture at the given location to the texture atlas.
         * @param namespace namespace of the texture location
         * @param path      path of the texture location
         */
        public AtlasBuilder texture(String namespace, String path){
            return this.texture(Identifier.fromNamespaceAndPath(namespace, path));
        }

        /**
         * Adds the texture at the given location to the texture atlas.
         * @param path path of the texture location
         */
        public AtlasBuilder texture(String path){
            return this.texture(this.modid, path);
        }

        /**
         * Adds all textures used by the model with the given path to the texture atlas.
         * If {@code includeParents} is true, parent model's texture will also be added.
         * @param model          location of the model
         * @param includeParents whether the parents of the given model should also be added
         */
        public AtlasBuilder texturesFromModel(Identifier model, boolean includeParents){
            this.models.add(Pair.of(model, includeParents));
            return this;
        }

        /**
         * Adds all textures used by the model with the given path to the texture atlas.
         * If {@code includeParents} is true, parent model's texture will also be added.
         * @param namespace      namespace of the model location
         * @param path           path of the model location
         * @param includeParents whether the parents of the given model should also be added
         */
        public AtlasBuilder texturesFromModel(String namespace, String path, boolean includeParents){
            return this.texturesFromModel(Identifier.fromNamespaceAndPath(namespace, path), includeParents);
        }

        /**
         * Adds all textures used by the model with the given path to the texture atlas.
         * If {@code includeParents} is true, parent model's texture will also be added.
         * @param path           path of the model location
         * @param includeParents whether the parents of the given model should also be added
         */
        public AtlasBuilder texturesFromModel(String path, boolean includeParents){
            return this.texturesFromModel(this.modid, path, includeParents);
        }

        /**
         * Adds all textures used by the model with the given path and its parents to the texture atlas.
         * @param model location of the model
         */
        public AtlasBuilder texturesFromModel(Identifier model){
            return this.texturesFromModel(model, true);
        }

        /**
         * Adds all textures used by the model with the given path and its parents to the texture atlas.
         * @param namespace namespace of the model location
         * @param path      path of the model location
         */
        public AtlasBuilder texturesFromModel(String namespace, String path){
            return this.texturesFromModel(namespace, path, true);
        }

        /**
         * Adds all textures used by the model with the given path and its parents to the texture atlas.
         * @param path path of the model location
         */
        public AtlasBuilder texturesFromModel(String path){
            return this.texturesFromModel(path, true);
        }
    }
}
