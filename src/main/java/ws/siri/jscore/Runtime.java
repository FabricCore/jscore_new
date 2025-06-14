package ws.siri.jscore;

import java.util.HashMap;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import ws.siri.jscore.mapping.JSObject;
import ws.siri.yarnwrap.mapping.JavaLike;
import ws.siri.yarnwrap.mapping.JavaObject;

public class Runtime {
    private static Context cx = Context.enter();
    private static HashMap<String[], Scriptable> scopes = new HashMap<>();

    public static Object evaluate(String expr, String label) throws Exception {
        String[] path = new String[] { "repl" };
        newScope(path);
        Object res = cx.evaluateString(scopes.get(path), expr, label, 1, null);
        return Context.jsToJava(res, Object.class);
    }

    public static Scriptable asJS(Object source) {
        if (source instanceof JavaLike) {
            if (source instanceof JavaObject)
                return new JSObject((JavaObject) source);
        }

        return new JSObject(new JavaObject(source));
    }

    public static Object unwrap(Object source) {
        if(source instanceof JSObject) {
            return ((JSObject) source).internal.internal;
        } else if(source instanceof JavaObject) {
            return ((JavaObject) source).internal;
        } else {
            return source;
        }
    }

    private static boolean newScope(String[] path) {
        if (scopes.containsKey(path))
            return false;
        else
            scopes.put(path, cx.initStandardObjects());

        return true;
    }
}
