package com.whensunset.annotation_processing.util;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.whensunset.annotation.invoker.InvokeBy;

public class Util {
  public static AnnotationSpec invokeBy(ClassName invokerClass, CodeBlock methodId) {
    return AnnotationSpec.builder(InvokeBy.class)
        .addMember("invokerClass", "$T.class", invokerClass)
        .addMember("methodId", "$L", methodId)
        .build();
  }
}
