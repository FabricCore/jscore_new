package ws.siri.jscore.runtime;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class Module extends ScriptableObject {
    private Scriptable scope;
    private Context cx;

    private final String[] path;

    public Module(String[] path) {
        cx = Context.getCurrentContext();
        if(cx == null) cx = Context.enter();

        scope = cx.initStandardObjects();

        this.path = path;
    }

    public Object evaluate(String expr) {
        Object res = cx.evaluateString(scope, expr, String.join(".", path), 1, null);
        return Context.jsToJava(res, Object.class);
    }

    @Override
    public String getClassName() {
        return String.format("JSScope(%s)", String.join(".", path));
    }
}
