package ws.siri.jscore.runtime;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.mozilla.javascript.Context;

import net.fabricmc.loader.api.FabricLoader;
import ws.siri.jscore.Core;
import ws.siri.jscore.mapping.JSClass;
import ws.siri.jscore.mapping.JSFunction;
import ws.siri.jscore.mapping.JSObject;
import ws.siri.jscore.mapping.JSPackage;
import ws.siri.yarnwrap.mapping.JavaClass;
import ws.siri.yarnwrap.mapping.JavaFunction;
import ws.siri.yarnwrap.mapping.JavaLike;
import ws.siri.yarnwrap.mapping.JavaObject;
import ws.siri.yarnwrap.mapping.JavaPackage;

public class Runtime {
    private static HashMap<List<String>, Module> modules = new HashMap<>();

    public static Object evaluate(String expr, List<String> path, boolean isLazy) {
        try {
            return getModule(path).evaluate(expr, isLazy);
        } catch (Exception e) {
            throw new RuntimeException("caught " + e);
        }

    }

    public static Optional<String> getPrelude() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(Core.MOD_ID).resolve("prelude.js");

        if (Files.exists(path))
            try {
                return Optional.of(Files.readString(path));
            } catch (IOException e) {
                throw new RuntimeException("Error getting prelude: " + e);
            }

        return Optional.empty();
    }

    /**
     * Convert a JavaLike to a JSLike
     * 
     * @param source
     * @return
     */
    public static Object asJS(Object source) {
        if (source instanceof JavaLike) {
            if (source instanceof JavaObject)
                return new JSObject((JavaObject) source);
            if (source instanceof JavaClass)
                return new JSClass((JavaClass) source);
            if (source instanceof JavaFunction)
                return new JSFunction((JavaFunction) source);
            if (source instanceof JavaPackage)
                return new JSPackage((JavaPackage) source);
        }

        return source;
        // return new JSObject(new JavaObject(source));
    }

    /**
     * Unwrap one layer of wrapper
     * 
     * @param source
     * @return
     */
    public static Object unwrap(Object source) {
        if (source instanceof JSObject)
            return ((JSObject) source).internal.internal;
        if (source instanceof JavaObject)
            return ((JSFunction) source).internal;
        if (source instanceof JSClass)
            return ((JSClass) source).internal;
        if (source instanceof JSPackage)
            return ((JSPackage) source).internal;

        return source;

    }

    /**
     * Return the module at path, create a new one if does not exist
     * 
     * @param path
     * @return
     */
    private static Module getModule(List<String> path) {
        if (!modules.containsKey(path))
            modules.put(path, new Module(path));

        return modules.get(path);
    }

    /**
     * Evaluate a file, and returns the value of module.exports
     * 
     * @param path    path from which the expression comes from
     * @param mode    mod to evaluate in: lazy or strict
     * @param content TODO provide a script instead of using the file content
     * @return module.exports
     */
    @Nullable
    public static Object call(Path path, String mode, String content) {
        return call(path, mode, content, !mode.equals("strict"));
    }

    @Nullable
    public static Object call(Path path, String mode, String content, boolean isLazy) {
        path = Module.normalisePath(path);
        List<String> pathList;

        switch (mode) {
            case "lazy":
                pathList = Arrays.asList(path.toString().split("/"));
                if (!modules.containsKey(pathList)) {
                    List<String> pathWithExtension = new ArrayList<>(pathList);
                    pathWithExtension.set(pathWithExtension.size() - 1, pathWithExtension.getLast() + ".js");

                    if (modules.containsKey(pathWithExtension)) {
                        pathList = pathWithExtension;
                    } else {
                        List<String> pathWithIndexJs = new ArrayList<>(pathList);
                        pathWithIndexJs.add("index.js");

                        if (modules.containsKey(pathWithIndexJs)) {
                            pathList = pathWithIndexJs;
                        } else {
                            return call(path, "strict", content, true);
                        }
                    }
                }
                return modules.get(pathList).exports;

            case "strict":
                Path basePath = FabricLoader.getInstance().getConfigDir().resolve(Core.MOD_ID);

                if (!Files.exists(basePath.resolve(path))) {
                    if (!path.getFileName().endsWith(".js")) {
                        Path newPath = path.resolveSibling(path.getFileName() + ".js");
                        if (Files.exists(basePath.resolve(newPath))) {
                            path = newPath;
                        } else {
                            throw new RuntimeException("Could not find file at " + newPath.toString());
                        }
                    } else {
                        throw new RuntimeException("Could not find file at " + path.toString());
                    }
                } else if (Files.isDirectory(basePath.resolve(path))) {
                    Path newPath = path.resolve("index.js");
                    if (Files.exists(basePath.resolve(newPath))) {
                        path = newPath;
                    } else {
                        throw new RuntimeException("Could not find file at " + newPath.toString());
                    }
                }

                try {
                    content = Files.readString(basePath.resolve(path));
                } catch (IOException e) {
                    throw new RuntimeException(String.format("Error reading file %s: %s", path, e));
                }

                pathList = Arrays.asList(path.toString().split("/"));
                evaluate(content, pathList, isLazy);
                return modules.get(pathList).exports;

            case "append":
                pathList = Arrays.asList(path.toString().split("/"));
                if (!modules.containsKey(pathList)) {
                    List<String> pathWithExtension = new ArrayList<>(pathList);
                    pathWithExtension.set(pathWithExtension.size() - 1, pathWithExtension.getLast() + ".js");

                    if (modules.containsKey(pathWithExtension)) {
                        pathList = pathWithExtension;
                    } else {
                        List<String> pathWithIndexJs = new ArrayList<>(pathList);
                        pathWithIndexJs.add("index.js");

                        if (modules.containsKey(pathWithIndexJs)) {
                            pathList = pathWithIndexJs;
                        } else {
                            throw new RuntimeException("Cannot find module with name: " + path);
                        }
                    }
                }

                Module module = modules.get(pathList);
                module.evaluate(content, isLazy);
            default:
                throw new UnsupportedOperationException("No require mode '" + mode + "'");
        }
    }

    public static Context getContext() {
        Context cx = Context.getCurrentContext();

        if (cx == null)
            cx = Context.enter();

        return cx;
    }
}
