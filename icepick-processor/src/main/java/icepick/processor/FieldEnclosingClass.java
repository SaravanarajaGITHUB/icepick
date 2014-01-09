package icepick.processor;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.Collection;

class FieldEnclosingClass {

    public final TypeElement type;
    public final String className;
    public final String packageName;
    public final String parentFqcn;

    FieldEnclosingClass(TypeElement type, String className, String packageName, String parentFqcn) {
        this.type = type;
        this.className = className;
        this.packageName = packageName;
        this.parentFqcn = parentFqcn;
    }

    static class Factory {

        private final Types typeUtils;

        Factory(Types typeUtils) {
            this.typeUtils = typeUtils;
        }

        public FieldEnclosingClass from(TypeElement classType, Collection<TypeMirror> parents) {
            String className = findName(classType);
            String packageName = findPackage(classType);
            String parentFqcn = findParentFqcn(classType, parents);
            return new FieldEnclosingClass(classType, className, packageName, parentFqcn);
        }

        private String findName(TypeElement classType) {
            return classType.getSimpleName().toString();
        }

        private String findPackage(TypeElement classType) {
            return classType.getQualifiedName().toString().replace("." + classType.getSimpleName(), "");
        }

        private String findParentFqcn(TypeElement classType, Collection<TypeMirror> parents) {
            TypeMirror type;
            while (true) {
                type = classType.getSuperclass();
                if (type.getKind() == TypeKind.NONE) {
                    return null;
                }
                classType = (TypeElement) ((DeclaredType) type).asElement();
                if (containsTypeMirror(parents, type)) {
                    return classType.getQualifiedName().toString();
                }
            }
        }

        private boolean containsTypeMirror(Collection<TypeMirror> mirrors, TypeMirror query) {
            // Ensure we are checking against a type-erased version for normalization purposes.
            TypeMirror erasure = typeUtils.erasure(query);
            for (TypeMirror mirror : mirrors) {
                if (typeUtils.isSameType(mirror, erasure)) {
                    return true;
                }
            }
            return false;
        }
    }
}