package ws.siri.jscore.runtime;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jetbrains.annotations.Nullable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

import ws.siri.jscore.mapping.JSFunction;
import ws.siri.jscore.mapping.JSObject;
import ws.siri.yarnwrap.mapping.JavaObject;

/**
 * module object in script
 */
public class Module extends ScriptableObject {
    /**
     * File scope
     */
    private Scriptable scope;

    /**
     * Module.exports object
     */
    public Object exports = Undefined.instance;
    /**
     * module.require()
     */
    public JSFunction require = (JSFunction) new JSObject(new JavaObject(new Require())).get("call", null);

    /**
     * Lock on when evaluating an expression
     */
    private Lock lock = new ReentrantLock(true);

    /**
     * If current evaluation is lazy
     */
    private boolean isLazy;

    /**
     * path to current module (file path)
     */
    private final List<String> path;

    public Module(List<String> path) {
        scope = Runtime.getContext().initStandardObjects();
        scope.put("module", scope, this);

        this.path = path;

        Optional<String> prelude = Runtime.getPrelude();

        if (prelude.isPresent())
            appendPrelude(prelude.get());
    }

    private void appendPrelude(String content) {
        evaluate(content, true, true);
    }

    /**
     * evaluate an expression in module
     * 
     * @param expr
     * @return
     */
    public Object evaluate(String expr, boolean isLazy) {
        return evaluate(expr, isLazy, false);
    }

    private Object evaluate(String expr, boolean isLazy, boolean prelude) {
        this.isLazy = isLazy;

        Exception maybeError = null;
        Object res = null;
        try {
            lock.lock();
            res = Runtime.getContext().evaluateString(scope, expr,
                    String.join("/", path) + (prelude ? "(prelude)" : ""),
                    1, null);
        } catch (Exception e) {
            maybeError = e;
        } finally {
            lock.unlock();
        }

        if (maybeError == null) {
            return Context.jsToJava(res, Object.class);
        } else {
            throw new RuntimeException(maybeError);
        }
    }

    @Override
    public String getClassName() {
        return String.format("JSScope(%s)", String.join(".", path));
    }

    @Override
    public Object get(String name, Scriptable start) {
        switch (name) {
            case "exports":
                if (exports == null)
                    return NOT_FOUND;
                return exports;
            case "require":
                return require;
            case "path":
                return path;
            default:
                return NOT_FOUND;
        }
    }

    @Override
    public void put(String name, Scriptable start, Object value) {
        switch (name) {
            case "exports":
                exports = value;
                break;
            default:
                throw new UnsupportedOperationException(String.format("Cannot change value of module.%s", name));
        }
    }

    @Override
    public boolean has(String name, Scriptable start) {
        switch (name) {
            case "exports":
                return exports != null;
            case "require":
            case "path":
                return true;
            default:
                return false;
        }
    }

    @Nullable
    public Object exports() {
        return exports;
    }

    /**
     * restrict path to be within .minecraft/config/jscore,
     * and remove ../ and ./ accordingly
     * 
     * @param path
     * @return
     */
    public static Path normalisePath(Path path) {
        path = path.normalize();

        if (path.isAbsolute()) {
            path = path.subpath(0, path.getNameCount());
        }

        while (path.getNameCount() > 0 && path.getName(0).toString().equals("..")) {
            path = path.subpath(1, path.getNameCount());
        }

        return path;
    }

    /**
     * Object containing overloaded function for modules.require
     */
    public class Require {
        private Path getRelativePath(String relativePath) {
            return normalisePath(Path.of(String.join("/", path)).resolveSibling(relativePath));
        }

        public Object call(String relativePath, String mode, String content) {
            return Runtime.call(getRelativePath(relativePath), mode, content);
        }

        public Object call(String relativePath, String mode) {
            return Runtime.call(getRelativePath(relativePath), mode, null);
        }

        public Object call(String relativePath) {
            System.out.println(isLazy);
            return Runtime.call(getRelativePath(relativePath), isLazy ? "lazy" : "strict", null);
        }
    }
}
