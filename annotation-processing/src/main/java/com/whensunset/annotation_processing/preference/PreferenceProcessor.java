package com.whensunset.annotation_processing.preference;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.TypeName;
import com.sun.source.util.Trees;
import com.whensunset.annotation.preference.BuiltInObjectPreference;
import com.whensunset.annotation.preference.OtherObjectPreference;
import com.whensunset.annotation.preference.PreferenceAnnotation;
import com.whensunset.annotation_processing.preference.model.PreferenceHelperBuilder;
import com.whensunset.annotation_processing.preference.proxy.AnnotationProxy;
import com.whensunset.annotation_processing.preference.proxy.BuiltInObjectProxy;
import com.whensunset.annotation_processing.preference.proxy.OtherObjectProxy;
import com.whensunset.annotation_processing.util.BaseProcessor;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import static com.whensunset.annotation_processing.util.SortedElement.COMPARATOR;

/**
 * Created by whensunset on 2018/10/5.
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes({"com.whensunset.annotation.preference.PreferenceAnnotation",
    "com.whensunset.annotation.preference.PrimitivePreference",
    "com.whensunset.annotation.preference.ObjectPreference",
    "com.whensunset.annotation.preference.AsBoolean"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class PreferenceProcessor extends BaseProcessor {
  public final static String PACKAGE_NAME = "preferenceHelperPackageName";
  public final static String DEFAULT_PACKAGE = "com.whensunset";
  public final static String DEFAULT_PREFERENCE_NAME = "DefaultPreferenceHelper";
  
  private Trees mTrees;
  private Map<String, PreferenceHelperBuilder> mPrefs = new HashMap<String, PreferenceHelperBuilder>();
  private Boolean mHasProcessed = false;
  private String mPkgName;
  
  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    String packageName = processingEnv.getOptions().get(PACKAGE_NAME);
    if (packageName == null || "" == packageName) {
      packageName = DEFAULT_PACKAGE;
    }
    mPkgName = packageName;
  }
  
  @Override
  public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
    if (mHasProcessed) {
      return false;
    }
    mTrees = Trees.instance(processingEnv);
    List<Element> classes = new ArrayList<Element>(roundEnvironment.getElementsAnnotatedWith(PreferenceAnnotation.class));
    
    classes.sort(COMPARATOR);
    for (Element rootClass : classes) {
      if (rootClass.getKind() != ElementKind.CLASS) {
        continue;
      }
      PreferenceAnnotation annotation = rootClass.getAnnotation(PreferenceAnnotation.class);
      Boolean needSaver = annotation.generateSaver();
      String prefixForAll = annotation.prefixKeyForAllFields();
      List<Element> fields = new ArrayList<>(rootClass.getEnclosedElements());
      fields.sort(COMPARATOR);
      for (Element field : fields) {
        if (field == null || field.getKind() != ElementKind.FIELD) {
          continue;
        }
        Annotation fieldAnnotation;
        fieldAnnotation = field.getAnnotation(BuiltInObjectPreference.class);
        if (fieldAnnotation == null) {
          fieldAnnotation = field.getAnnotation(OtherObjectPreference.class);
        }
        onNewField((TypeElement) rootClass, field, prefixForAll, fieldAnnotation, needSaver);
      }
    }
    
    writeToFile();
    mHasProcessed = true;
    return false;
  }
  
  
  private void writeToFile() {
    for (PreferenceHelperBuilder pref : mPrefs.values()) {
      writeClass(pref);
    }
  }
  
  private void onNewField(TypeElement clazz, Element field, String allPrefix,
                          Annotation annotation, Boolean needSaver) {
    AnnotationProxy annotationProxy = null;
    if (annotation instanceof BuiltInObjectPreference) {
      annotationProxy = new BuiltInObjectProxy((BuiltInObjectPreference) annotation, field, mTrees);
    } else if (annotation instanceof OtherObjectPreference) {
      annotationProxy = new OtherObjectProxy((OtherObjectPreference) annotation);
    }
    if (annotationProxy == null) {
      return;
    }
    
    String prefName = PreferenceHelperBuilder.Companion.realName(annotationProxy.prefFile());
    PreferenceHelperBuilder builder = mPrefs.computeIfAbsent(prefName, name -> new PreferenceHelperBuilder(name, mPkgName));
    String prefix = annotationProxy.prefix();
    if ("" == prefix) {
      prefix = allPrefix;
    }
    builder.onNewField(TypeName.get(clazz.asType()), TypeName.get(field.asType()), annotationProxy.key(),
        prefix, field.getSimpleName().toString(), annotationProxy.removable(), annotationProxy.def(), needSaver);
  }
  
}
