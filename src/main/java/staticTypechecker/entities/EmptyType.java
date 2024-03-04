package staticTypechecker.entities;

import java.util.IdentityHashMap;

import jolie.lang.parse.context.ParsingContext;

public class EmptyType extends Type{

    public static boolean isEmptyType(Type type) {
        return type instanceof EmptyType;
    }

    @Override
    public boolean isSubtypeOf(Type other) {
        return false;
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof ChoiceType && ((ChoiceType)other).choices().isEmpty()) {
            return true;
        }
        return other instanceof EmptyType;
    }

    @Override
    public ParsingContext context() {
        throw new UnsupportedOperationException("Unsupported method");
    }

    @Override
    public Type shallowCopy() {
        return copy();
    }

    @Override
    public Type shallowCopyExcept(Path p) {
        throw new UnsupportedOperationException("Unsupported method");
    }

    @Override
    public Type copy() {
        return new EmptyType();
    }

    @Override
    public Type copy(IdentityHashMap<Type, Type> rec) {
        return copy();
    }

    @Override
    public String prettyString() {
        return "Ø";
    }

    @Override
    public String prettyString(int level) {
        throw new UnsupportedOperationException("Unimplemented method 'prettyString'");
    }

    @Override
    public String prettyString(int level, IdentityHashMap<Type, Void> recursive) {
        throw new UnsupportedOperationException("Unimplemented method 'prettyString'");
    }
    
}
