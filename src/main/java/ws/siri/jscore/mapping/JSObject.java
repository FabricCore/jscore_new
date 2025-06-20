package ws.siri.jscore.mapping;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import ws.siri.jscore.runtime.Runtime;
import ws.siri.yarnwrap.mapping.JavaObject;

public class JSObject extends ScriptableObject {
    public final JavaObject internal;

    public JSObject(JavaObject internal) {
        this.internal = internal;
    }

    @Override
    public String getClassName() {
        return String.format("JSObject(%s)", internal.stringQualifier().replace('/', '.'));
    }

    @Override
    public Object get(String name, Scriptable start) {
        Optional<Object> res = internal.getRelative(name);

        if (res.isPresent())
            return Runtime.asJS(res.get());
        else
            return super.get(name, start);
    }

    @Override
    public Object get(int index, Scriptable start) {
        List<?> list;

        if (internal.internal.getClass().isArray()) {
            list = Arrays.asList((Object[]) internal.internal);
        } else if (internal.internal instanceof List) {
            list = (List<?>) internal.internal;
        } else {
            return NOT_FOUND;
        }

        if (0 <= index && index < list.size())
            return Runtime.asJS(list.get(index));
        else
            return NOT_FOUND;
    }

    @Override
    public boolean has(String name, Scriptable start) {
        return internal.getRelative(name).isPresent() || super.has(name, start);
    }

    @Override
    public boolean has(int index, Scriptable start) {
        List<?> list;

        if (internal.internal.getClass().isArray()) {
            list = Arrays.asList((Object[]) internal.internal);
        } else if (internal.internal instanceof List) {
            list = (List<?>) internal.internal;
        } else {
            return false;
        }

        return 0 <= index && index < list.size();
    }

    @Override
    public void put(String name, Scriptable start, Object value) {
        value = Runtime.unwrap(value);
        if(!internal.setField(name, value))
            super.put(name, start, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void put(int index, Scriptable start, Object value) {
        value = Runtime.unwrap(value);
        if (internal.internal.getClass().isArray()) {
            ((Object[]) internal.internal)[index] = value;
        } else if (internal.internal instanceof List) {
            ((List<Object>) internal.internal).set(index, value);
        }
    }

    @Override
    public String toString() {
        return String.format("JSObject(%s)", internal.toString());
    }
}
