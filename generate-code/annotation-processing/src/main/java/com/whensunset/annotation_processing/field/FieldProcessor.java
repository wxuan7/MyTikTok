package com.whensunset.annotation_processing.field;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.whensunset.annotation.field.Field;
import com.whensunset.annotation.field.FieldGetter;
import com.whensunset.annotation.field.FieldGetters;
import com.whensunset.annotation.inject.ProviderHolder;
import com.whensunset.annotation_processing.util.BaseProcessor;
import com.whensunset.annotation_processing.util.SortedElement;
import com.whensunset.annotation_processing.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import static com.whensunset.annotation_processing.field.FieldProcessor.CLASS_NAME;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"com.whensunset.annotation.field.Field"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(CLASS_NAME)
public class FieldProcessor extends BaseProcessor {
  public static final String CLASS_NAME = "providerInterfaceName";
  private boolean mHasProcessed;
  private String mPackage;
  private String mClassName;
  
  private DuplicationChecker mChecker = new DuplicationChecker();
  
  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    String fullName = processingEnv.getOptions().get(CLASS_NAME);
    if (fullName == null) {
      mHasProcessed = true;
      return;
    }
    int lastDot = fullName.lastIndexOf('.');
    mClassName = fullName.substring(lastDot + 1);
    mPackage = fullName.substring(0, lastDot);
  }
  
  @Override
  public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {
    if (mHasProcessed) {
      return false;
    }
    AnnotationSpec invokeBy =
        Util.invokeBy(ClassName.get(FieldGetters.class),
            CodeBlock.of("$T.INVOKER_ID", FieldGetters.class));
    MethodSpec.Builder init =
        MethodSpec.methodBuilder("init")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
            .addAnnotation(invokeBy);
    TypeSpec.Builder feildGetterInitClass =
        TypeSpec.classBuilder(mClassName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
    SortedElement sortedElement = SortedElement.fromRoundEnv(roundEnv, Field.class);
    for (Map.Entry<TypeElement, List<Element>> type : sortedElement) {
      List<Element> fields = type.getValue();
      if (fields == null) {
        continue;
      }
      generateForClass(init, type.getKey(), fields);
    }
    feildGetterInitClass.addMethod(init.build());
    writeClass(mPackage, mClassName, feildGetterInitClass);
    mHasProcessed = true;
    return false;
  }
  
  private void generateForClass(MethodSpec.Builder init,
                                Element rootClass, List<Element> elements) {
    if (rootClass == null || rootClass.getKind() != ElementKind.CLASS) {
      return;
    }
    String className = rootClass.getSimpleName().toString() + "FieldGetter";
    className = className.replace("$", "_");
    TypeName rootType = TypeName.get(mTypes.erasure(rootClass.asType()));
    TypeName fieldNamesType =
        ParameterizedTypeName.get(ClassName.get(Set.class), TypeName.get(String.class));
    TypeName fieldTypesType =
        ParameterizedTypeName.get(ClassName.get(Set.class), TypeName.get(Class.class));
    TypeName allFieldsReturn =
        ParameterizedTypeName.get(ClassName.get(Set.class), TypeName.get(Object.class));
    TypeName fieldGetterType = ParameterizedTypeName.get(ClassName.get(FieldGetter.class),
        rootType);
    TypeSpec.Builder fieldGetterClass =
        TypeSpec.classBuilder(className)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addField(fieldNamesType, "mAccessibleNames", Modifier.PRIVATE, Modifier.FINAL)
            .addField(fieldTypesType, "mAccessibleTypes", Modifier.PRIVATE, Modifier.FINAL)
            .addField(FieldGetter.class, "mSuperFieldGetter", Modifier.PRIVATE)
            .addSuperinterface(fieldGetterType);
    MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PUBLIC)
        .addStatement("mAccessibleNames = new $T<$T>()", HashSet.class, String.class)
        .addStatement("mAccessibleTypes = new $T<$T>()", HashSet.class, Class.class);
    TypeVariableName typeT = TypeVariableName.get("T");
    MethodSpec.Builder getByNameMethod = MethodSpec.methodBuilder("get")
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addTypeVariable(typeT)
        .addParameter(rootType, "target")
        .addParameter(String.class, "fieldName")
        .returns(typeT);
    MethodSpec.Builder setByNameMethod = MethodSpec.methodBuilder("set")
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addTypeVariable(typeT)
        .addParameter(rootType, "target")
        .addParameter(String.class, "fieldName")
        .addParameter(typeT, "value");
    MethodSpec.Builder allFields = MethodSpec.methodBuilder("allFields")
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addParameter(rootType, "target")
        .returns(allFieldsReturn)
        .addStatement("$T result = new $T()", allFieldsReturn, HashSet.class);
    MethodSpec.Builder allFieldNames = MethodSpec.methodBuilder("allFieldNames")
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addParameter(rootType, "target")
        .addStatement("$T result = new $T<$T>(mAccessibleNames)", fieldNamesType, HashSet.class,
            String.class)
        .returns(fieldNamesType);
    MethodSpec.Builder allTypes = MethodSpec.methodBuilder("allTypes")
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addParameter(rootType, "target")
        .addStatement("$T result = new $T<$T>(mAccessibleTypes)", fieldTypesType, HashSet.class,
            Class.class)
        .returns(fieldTypesType);
    
    for (Element field : elements) {
      if (field.getKind() != ElementKind.FIELD || field.getModifiers().contains(Modifier.STATIC)) {
        continue;
      }
      genByField(constructor, getByNameMethod, setByNameMethod,
          allFields, field, rootClass.asType());
      checkForAdditionalGet(getByNameMethod, setByNameMethod,
          allFields, allFieldNames, allTypes, field, typeT);
    }
    getByNameMethod.addStatement("return (T) mSuperFieldGetter.get(target, fieldName)");
    setByNameMethod.addStatement("mSuperFieldGetter.set(target, fieldName, value)");
    allFields
        .addStatement("result.addAll(mSuperFieldGetter.allFields(target))")
        .addStatement("return result");
    allFieldNames
        .addStatement("result.addAll(mSuperFieldGetter.allFieldNames(target))")
        .addStatement("return result");
    allTypes
        .addStatement("result.addAll(mSuperFieldGetter.allTypes(target))")
        .addStatement("return result");
    fieldGetterClass
        .addMethod(constructor.build())
        .addMethod(buildInit(fieldGetterType, rootType))
        .addMethod(getByNameMethod.build())
        .addMethod(setByNameMethod.build())
        .addMethod(allFieldNames.build())
        .addMethod(allTypes.build())
        .addMethod(allFields.build());
    String pkg = mUtils.getPackageOf(rootClass).toString();
    writeClass(pkg, className, fieldGetterClass);
    init.addStatement("$T.put($T.class, new $T())", FieldGetters.class,
        mTypes.erasure(rootClass.asType()), ClassName.get(pkg, className));
  }
  
  private void checkForAdditionalGet(MethodSpec.Builder getByNameMethod,
                                     MethodSpec.Builder setByNameMethod, MethodSpec.Builder allFields,
                                     MethodSpec.Builder allFieldNames, MethodSpec.Builder allTypes, Element field,
                                     TypeVariableName typeT) {
    if (!Optional.ofNullable(field.getAnnotation(Field.class))
        .map(Field::doAdditionalGet).orElse(false)) {
      return;
    }
    String fieldName = field.getSimpleName().toString();
    getByNameMethod.addStatement("$T result$L = ($T) $T.get(target.$L, fieldName)", typeT,
        fieldName, typeT, ProviderHolder.class, fieldName);
    getByNameMethod.addCode("if(result$L != null) {\n" +
        "return result$L;\n"
        + "}\n", fieldName, fieldName);
    setByNameMethod.addStatement("$T.set(target.$L, fieldName, value)", ProviderHolder.class,
        fieldName);
    
    allFieldNames.addStatement("result.addAll($T.allFieldNames(target.$L))", ProviderHolder.class,
        fieldName);
    allTypes.addStatement("result.addAll($T.allTypes(target.$L))", ProviderHolder.class, fieldName);
    allFields.addStatement("result.addAll($T.allFields(target.$L))", ProviderHolder.class,
        fieldName);
  }
  
  private MethodSpec buildInit(TypeName fieldGetterType, TypeName rootType) {
    String loopCode = "if(mSuperFieldGetter != null){\n" +
        "  return this;\n" +
        "}\n" +
        "mSuperFieldGetter = $T.superFieldGetterOrNoop($T.class);\n" +
        "return this;\n";
    return MethodSpec.methodBuilder("init")
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addCode(loopCode, FieldGetters.class, rootType)
        .returns(fieldGetterType)
        .build();
  }
  
  private void genByField(MethodSpec.Builder constructor, MethodSpec.Builder getByNameMethod,
                          MethodSpec.Builder setByNameMethod, MethodSpec.Builder allFields,
                          Element field, TypeMirror rootClass) {
    Field fieldAnnotation = field.getAnnotation(Field.class);
    if (fieldAnnotation == null) {
      return;
    }
    String fieldKey = fieldAnnotation.value();
    String fieldName = field.getSimpleName().toString();
    if ("".equals(fieldKey)) {
      throw new RuntimeException("字段的 key 是必填项!");
    } else {
      getByName(constructor, getByNameMethod, setByNameMethod, field, fieldKey, fieldName);
      mChecker.onField(fieldKey, rootClass);
    }
    TypeMirror fieldType = mTypes.erasure(field.asType());
    TypeName fieldTypeName = TypeName.get(fieldType);
    if (fieldTypeName.isPrimitive()) {
      allFields.addStatement("result.add(target.$L)", fieldName);
    } else {
      allFields.beginControlFlow("if (target.$L != null)", fieldName)
          .addStatement("result.add(target.$L)", fieldName)
          .endControlFlow();
    }
  }
  
  private void getByName(MethodSpec.Builder constructor, MethodSpec.Builder getByNameMethod,
                         MethodSpec.Builder setByNameMethod, Element field, String fieldKey, String fieldName) {
    TypeMirror fieldType = mTypes.erasure(field.asType());
    TypeName fieldTypeName = TypeName.get(fieldType);
    if (fieldTypeName.isPrimitive()) {
      getByNameMethod.beginControlFlow("if (\"$L\".equals(fieldName))", fieldKey)
          .addStatement("return (T)$T.valueOf(target.$L)", fieldTypeName.box(), fieldName)
          .endControlFlow();
      setByNameMethod.beginControlFlow("if (\"$L\".equals(fieldName))", fieldKey)
          .addStatement("target.$L = ($T)value", fieldName, fieldTypeName.box())
          .addStatement("return")
          .endControlFlow();
    } else {
      getByNameMethod.beginControlFlow("if (\"$L\".equals(fieldName))", fieldKey)
          .addStatement("return (T)target.$L", fieldName)
          .endControlFlow();
      setByNameMethod.beginControlFlow("if (\"$L\".equals(fieldName))", fieldKey)
          .addStatement("target.$L = ($T)value", fieldName, fieldType)
          .addStatement("return")
          .endControlFlow();
    }
    constructor.addStatement("mAccessibleNames.add(\"$L\")", fieldKey);
  }
  
  private final class DuplicationChecker {
    private Map<String, List<TypeMirror>> mFieldNameMapping = new HashMap<>();
    private Map<TypeMirror, List<TypeMirror>> mFieldTypeMapping = new HashMap<>();
    
    public void onField(String fieldName, TypeMirror type) {
      List<TypeMirror> mirrors =
          mFieldNameMapping.computeIfAbsent(fieldName, k -> new ArrayList<>());
      type = mTypes.erasure(type);
      for (TypeMirror existing : mirrors) {
        if (mTypes.isSubtype(existing, type) || mTypes.isSubtype(type, existing)) {
          mMessager.printMessage(Diagnostic.Kind.ERROR,
              "Field key " + fieldName + " 在定义中冲突，类：" + type.toString() + "与类："
                  + existing.toString());
          return;
        }
      }
      mirrors.add(mTypes.erasure(type));
    }
  }
}
