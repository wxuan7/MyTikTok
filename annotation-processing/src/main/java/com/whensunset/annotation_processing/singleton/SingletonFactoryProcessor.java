package com.whensunset.annotation_processing.singleton;


import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.whensunset.annotation.singleton.Factory;
import com.whensunset.annotation_processing.util.BaseProcessor;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

@AutoService(Processor.class)
@SupportedAnnotationTypes("com.whensunset.annotation.singleton.Singleton")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class SingletonFactoryProcessor extends BaseProcessor {
  private final Set<String> mProcessedClasses = new HashSet<>();
  private final List<ClassVisitor> mClassVisitors = Arrays.asList(new SingletonClassVisitor());

  @Override
  public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {
    for (ClassVisitor classVisitor : mClassVisitors) {
      for (Element rootClass : roundEnv.getElementsAnnotatedWith(classVisitor.getTarget())) {
        generateForClass(classVisitor.getInvokeBy(), rootClass, classVisitor);
      }
    }
    return false;
  }

  private void generateForClass(AnnotationSpec invokeBy,
      Element rootClass, ClassVisitor visitor) {
    if (rootClass == null || rootClass.getKind() != ElementKind.CLASS) {
      return;
    }
    // 获得实现对应的interface
    TypeMirror interfaceName = visitor.getTypeAsKey((TypeElement) rootClass);
    if (interfaceName == null) {
      return;
    }
    String className = rootClass.getSimpleName().toString() + "Factory";
    if (!mProcessedClasses.add(className)) {
      return;
    }
    // 处理内部类
    className = className.replace("$", "_");
    TypeName rootType = TypeName.get(rootClass.asType());
    // 构造类型
    TypeSpec.Builder factory =
        TypeSpec.classBuilder(className)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addSuperinterface(ParameterizedTypeName.get(ClassName.get(Factory.class),
                rootType));
    // 构造工厂方法
    MethodSpec.Builder newInstance = MethodSpec.methodBuilder("newInstance")
        .addModifiers(Modifier.PUBLIC)
        .returns(rootType);
    // 构造自动注册方法
    MethodSpec.Builder register = MethodSpec.methodBuilder("register")
        .addAnnotation(invokeBy)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC);
    newInstance.addStatement("return new $T()", rootType);
    visitor.onClass(rootClass, className, interfaceName, register);
    factory
        .addMethod(newInstance.build())
        .addMethod(register.build());
    String pkg = mUtils.getPackageOf(rootClass).toString();
    // 输出Factory.java文件
    writeClass(pkg, className, factory);
  }

  public interface ClassVisitor {
    void onClass(Element rootClass, String className, TypeMirror interfaceName,
                 MethodSpec.Builder register);

    AnnotationSpec getInvokeBy();

    TypeMirror getTypeAsKey(TypeElement typeElement);

    Class<? extends Annotation> getTarget();
  }
}
