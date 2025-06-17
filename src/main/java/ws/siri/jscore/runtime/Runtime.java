package ws.siri.jscore.runtime;

import java.util.HashMap;

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
    private static HashMap<String[], Module> modules = new HashMap<>();

    public static Object evaluate(String expr, String label) throws Exception {
        return evaluateAt(expr, new String[] { "evaluator" });
    }

    public static Object evaluateAt(String expr, String[] path) {
        return getModule(path).evaluate(expr);
    }

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

    private static Module getModule(String[] path) {
        if (!modules.containsKey(path))
            modules.put(path, new Module(path));

        return modules.get(path);
    }
}
