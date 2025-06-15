package ws.siri.jscore.mapping;

import java.util.Optional;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import ws.siri.yarnwrap.mapping.JavaLike;
import ws.siri.yarnwrap.mapping.JavaPackage;
import ws.siri.yarnwrap.mapping.MappingTree;

public class JSPackage extends ScriptableObject {
    public final JavaPackage internal;

    public JSPackage(JavaPackage internal) {
        this.internal = internal;
    }

    @Override
    public String getClassName() {
        return internal.stringQualifier().replace('/', '.');
    }
    
    @Override
    public Object get(String name, Scriptable start) {
        Optional<JavaLike> res = internal.getRelative(name);

        if (res.isPresent())
            return ws.siri.jscore.runtime.Runtime.asJS(res.get());
        else
            return NOT_FOUND;
    }

    public static JSPackage getRoot() {
        return new JSPackage(MappingTree.getRoot());
    }

    @Override
    public String toString() {
        return String.format("JSPackage(%s)", internal.toString());
    }
}
