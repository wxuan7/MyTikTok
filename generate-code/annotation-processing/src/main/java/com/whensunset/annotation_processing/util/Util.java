package com.whensunset.annotation_processing.util;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.Trees;
import com.whensunset.annotation.invoker.InvokeBy;

import java.util.List;
import java.util.Optional;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public class Util {
  public static AnnotationSpec invokeBy(ClassName invokerClass, CodeBlock methodId) {
    return AnnotationSpec.builder(InvokeBy.class)
        .addMember("invokerClass", "$T.class", invokerClass)
        .addMember("methodId", "$L", methodId)
        .build();
  }
  
  public static TypeMirror getInterface(TypeElement rootClass) {
    List<? extends TypeMirror> interfaces = ((TypeElement) rootClass).getInterfaces();
    return interfaces.size() == 1 ? interfaces.get(0) : null;
  }
  
  public static String assignedValue(Element field, Trees trees) {
    Tree tree = trees.getTree(field);
    if (tree == null) {
      return String.valueOf(((VariableElement) field).getConstantValue());
    } else {
      ExpressionTree expression = ((VariableTree) tree).getInitializer();
      return Optional.ofNullable(expression)
          .filter(e -> e instanceof LiteralTree)
          .map(e -> e.toString()).orElse(null);
    }
  }
  
  public static String defaultValue(TypeName fieldType) {
    String defaultValue;
    if (fieldType.isPrimitive() || fieldType.isBoxedPrimitive()) {
      fieldType = fieldType.unbox();
    }
    if (fieldType.equals(TypeName.INT)
        || fieldType.equals(TypeName.SHORT)
        || fieldType.equals(TypeName.BYTE)) {
      defaultValue = "0";
    } else if (fieldType.equals(TypeName.LONG)) {
      defaultValue = "0L";
    } else if (fieldType.equals(TypeName.FLOAT)) {
      defaultValue = "0.0f";
    } else if (fieldType.equals(TypeName.DOUBLE)) {
      defaultValue = "0.0";
    } else if (fieldType.equals(TypeName.CHAR)) {
      defaultValue = "java.lang.Character.MIN_VALUE";
    } else if (fieldType.equals(TypeName.BOOLEAN)) {
      defaultValue = "false";
    } else {
      defaultValue = "null";
    }
    return defaultValue;
  }
}
