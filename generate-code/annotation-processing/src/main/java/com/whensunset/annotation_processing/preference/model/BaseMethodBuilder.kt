package com.whensunset.annotation_processing.preference.model

import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier

abstract class BaseMethodBuilder(
        val mType: TypeSpec.Builder,
        val mSavers: MutableMap<TypeName, MethodSpec.Builder>
) {

    internal fun appendSaver(clazz: TypeName, type: TypeName,
                             fieldName: String, realkey: CodeBlock) {
        var realType = buildRealType(type)
        var realValue = buildRealValue("${PreferenceHelperBuilder.sParamObject}.$fieldName")

        mSavers.computeIfAbsent(clazz, PreferenceGenUtils.buildSaveMethod)
                .addStatement("\$L", PreferenceGenUtils.genPutCode(realType.getName(), realkey, realValue))

    }

    internal fun appendOperator(clazz: TypeName, type: TypeName, fieldName: String,
                                realkey: CodeBlock, pureKey: String, removable: Boolean, def: String) {
        var realType = buildRealType(type)
        var realValue = buildRealValue(PreferenceHelperBuilder.sParamValue)
        var realDefault = buildRealDefault(def)
        val getter =
                buildGet(MethodSpec.methodBuilder("get$pureKey")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .addJavadoc("@see \$T#\$L", clazz, fieldName)
                        , pureKey, realkey, realDefault, realType.getName(), type, realType).build()
        mType.addMethod(getter)

        val setter = MethodSpec.methodBuilder("set$pureKey")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(type, PreferenceHelperBuilder.sParamValue)
                .addStatement("\$T ${PreferenceHelperBuilder.sVarEditor} = ${PreferenceHelperBuilder.sFieldPreference}.edit()", PreferenceHelperBuilder.sTypeEditor)
                .addStatement("\$L", PreferenceGenUtils.genPutCode(realType.getName(), realkey, realValue))
                .addStatement("${PreferenceHelperBuilder.sVarEditor}.apply()")
                .build()
        mType.addMethod(setter)

        if (removable) {
            val remover = MethodSpec.methodBuilder("remove$pureKey")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addStatement("${PreferenceHelperBuilder.sFieldPreference}.edit().remove($realkey).apply()")
                    .build()
            mType.addMethod(remover)
        }
    }

    internal abstract fun buildRealDefault(def: String): String

    internal abstract fun buildRealType(rawType: TypeName): TypeName

    internal abstract fun buildRealValue(param: String): CodeBlock

    internal abstract fun buildGet(getter: MethodSpec.Builder, pureName: String, realkey: CodeBlock,
                                   def: String, type: String, rawType: TypeName, realType: TypeName): MethodSpec.Builder
}