package com.whensunset.annotation_processing.singleton;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.whensunset.annotation.singleton.Singleton;
import com.whensunset.annotation_processing.util.Util;

import java.lang.annotation.Annotation;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

public class SingletonClassVisitor implements SingletonFactoryProcessor.ClassVisitor {
  private static final ClassName SINGLETON_CALL =
      ClassName.get("com.whensunet.core.factoryregister", "SingletonCall");
  private static final AnnotationSpec sInvokeBy =
      Util.invokeBy(SINGLETON_CALL, CodeBlock.of("$T.INVOKER_ID", SINGLETON_CALL));
  
  
  @Override
  public void onClass(Element rootClass, String className, TypeMirror interfaceName,
                      MethodSpec.Builder register) {
    register.addStatement("$T.register($T.class, new $L())", SINGLETON_CALL, interfaceName,
        className);
  }
  
  @Override
  public AnnotationSpec getInvokeBy() {
    return sInvokeBy;
  }
  
  @Override
  public TypeMirror getTypeAsKey(TypeElement typeElement) {
    // 如果没有interface，则当做自己的单例
    TypeMirror typeMirror = Util.getInterface(typeElement);
    return typeMirror == null ? typeElement.asType() : typeMirror;
  }
  
  @Override
  public Class<? extends Annotation> getTarget() {
    return Singleton.class;
  }
}
