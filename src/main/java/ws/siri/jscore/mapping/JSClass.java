package ws.siri.jscore.mapping;

import java.util.Optional;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import ws.siri.yarnwrap.mapping.JavaClass;
import ws.siri.yarnwrap.mapping.JavaFunction;
import ws.siri.yarnwrap.mapping.JavaLike;
import ws.siri.yarnwrap.mapping.JavaObject;

public class JSClass extends ScriptableObject implements Function {
    public final JavaClass internal;

    public JSClass(JavaClass internal) {
        this.internal = internal;
    }

    @Override
    public String getClassName() {
        return String.format("JSClass(%s)", internal.stringQualifier().replace('/', '.'));
    }

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        throw new UnsupportedOperationException("attempted cast? casting is not supported");
    }

    @Override
    public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
        try {
            return new JSObject(new JavaObject(((JavaFunction) internal.getRelative("<init>").get()).run(args)));
        } catch (Exception e) {
            throw new RuntimeException("Error when executing function: " + e);
        }
    }

    @Override
    public Object get(String name, Scriptable start) {
        Optional<JavaLike> res = internal.getRelative(name);

        if (res.isPresent())
            return ws.siri.jscore.runtime.Runtime.asJS(res.get());
        else
            return super.get(name, start);
    }

    @Override
    public boolean has(String name, Scriptable start) {
        return internal.getRelative(name).isPresent() || super.has(name, start);
    }

    @Override
    public String toString() {
        return String.format("JSClass(%s)", internal.toString());
    }
}
