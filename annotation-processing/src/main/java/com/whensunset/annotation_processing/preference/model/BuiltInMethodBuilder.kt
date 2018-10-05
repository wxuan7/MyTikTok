package com.whensunset.annotation_processing.preference.model

import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec

class BuiltInMethodBuilder(type: TypeSpec.Builder, savers: MutableMap<TypeName, MethodSpec.Builder>,
                           val mDebugMap: MethodSpec.Builder) :
        BaseMethodBuilder(type, savers) {
    override fun buildRealDefault(def: String): String {
        return def
    }

    override fun buildRealType(rawType: TypeName): TypeName {
        return rawType
    }

    override fun buildGet(getter: MethodSpec.Builder, pureName: String, realkey: CodeBlock,
                          def: String, type: String, rawType: TypeName, realType: TypeName): MethodSpec.Builder {

        mDebugMap.addStatement("${PreferenceHelperBuilder.sVarMap}.put(\$S, get$pureName())", pureName)
        return getter.addStatement("return ${PreferenceHelperBuilder.sFieldPreference}.get${type}" +
                "(\$L, $def)", realkey)
                .returns(realType)
    }


    override fun buildRealValue(param: String): CodeBlock {
        return CodeBlock.of(param)
    }
}