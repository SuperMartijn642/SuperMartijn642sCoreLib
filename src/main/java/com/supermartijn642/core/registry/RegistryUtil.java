package com.supermartijn642.core.registry;

/**
 * Created 14/07/2022 by SuperMartijn642
 */
public class RegistryUtil {

    /**
     * Checks whether the given namespace contains illegal characters
     * @param namespace namespace to be checked
     * @return {@code true} if the namespace is valid
     */
    public static boolean isValidNamespace(String namespace){
        return namespace != null && namespace.length() > 0 && namespace.matches("[a-z0-9_.-]*");
    }

    /**
     * Checks whether the given path contains illegal characters
     * @param path identifier path to be checked
     * @return {@code true} if the path is valid
     */
    public static boolean isValidPath(String path){
        return path != null && path.length() > 0 && path.matches("[a-z0-9_./-]*");
    }

    /**
     * Checks whether given identifier contains illegal characters
     * @param namespace identifier namespace to be checked
     * @param path      identifier path to be checked
     * @return {@code true} if the identifier is valid
     */
    public static boolean isValidIdentifier(String namespace, String path){
        return isValidNamespace(namespace) && isValidPath(path);
    }
}
