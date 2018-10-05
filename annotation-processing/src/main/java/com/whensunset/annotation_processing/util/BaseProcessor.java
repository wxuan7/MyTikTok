package com.whensunset.annotation_processing.util;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.Writer;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public abstract class BaseProcessor extends AbstractProcessor {
  protected Filer mFiler;
  protected Elements mUtils;
  protected Types mTypes;
  protected Messager mMessager;
  
  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    mFiler = processingEnv.getFiler();
    mUtils = processingEnv.getElementUtils();
    mTypes = processingEnv.getTypeUtils();
    mMessager = processingEnv.getMessager();
  }
  
  protected void writeClass(String pkg, String name, TypeSpec.Builder type) {
    try {
      Writer writer = mFiler.createSourceFile(pkg + "." + name).openWriter();
      JavaFile.builder(pkg, type.build()).build().writeTo(writer);
      writer.flush();
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  protected void writeClass(ClassBuilder builder) {
    writeClass(builder.getPackage(), builder.getClassName(), builder.build());
  }
}
