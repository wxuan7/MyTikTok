package com.whensunset.annotation_processing.util;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.whensunset.annotation.invoker.InvokeBy;

import java.util.List;

import javax.lang.model.element.TypeElement;
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
}
