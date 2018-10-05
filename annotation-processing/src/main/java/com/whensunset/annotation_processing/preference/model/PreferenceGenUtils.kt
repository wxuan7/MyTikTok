package com.whensunset.annotation_processing.preference.model

import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import javax.lang.model.element.Modifier

internal object PreferenceGenUtils {
    internal val buildSaveMethod = { clazz: TypeName ->
        MethodSpec.methodBuilder("save")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(clazz, PreferenceHelperBuilder.sParamObject)
                .addStatement("\$T ${PreferenceHelperBuilder.sVarEditor} =" +
                        " ${PreferenceHelperBuilder.sFieldPreference}.edit()", PreferenceHelperBuilder.sTypeEditor)
    }

    internal fun genPutCode(type: String, key: CodeBlock, value: CodeBlock): CodeBlock {
        return CodeBlock.of("${PreferenceHelperBuilder.sVarEditor}.put$type(\$L, \$L)", key, value)
    }
}