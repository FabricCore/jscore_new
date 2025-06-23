package ws.siri.jscore.mapping;

import org.mozilla.javascript.ConsString;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import ws.siri.jscore.runtime.Runtime;
import ws.siri.yarnwrap.common.ScriptFunction;
import ws.siri.yarnwrap.mapping.JavaFunction;

public class JSFunction extends ScriptableObject implements Function, ScriptFunction {
    public final JavaFunction internal;

    public JSFunction(JavaFunction internal) {
        this.internal = internal;
    }

    @Override
    public Object run(Object... args) throws Exception {
        return internal.run(args);
    }

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        for (int i = 0; i < args.length; i++) {
            args[i] = Runtime.unwrap(args[i]);
            if (args[i] instanceof Scriptable) {
                args[i] = Context.jsToJava(args[i], Object.class);
            } else if(args[i] instanceof ConsString) {
                args[i] = args[i].toString();
            }
        }
        try {
            Object res = run(args);
            return Runtime.asJS(res);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Exception when running function %s: %s", internal.toString(), e));
        }
    }

    @Override
    public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
        throw new UnsupportedOperationException("A JSFunction cannot be used as a constructor.");
    }

    @Override
    public String getClassName() {
        return String.format("JSFunction(%s)", internal.stringQualifier().replace('/', '.'));
    }

    @Override
    public String toString() {
        return String.format("JSFunction(%s)", internal.toString());
    }
}
