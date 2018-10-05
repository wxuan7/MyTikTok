package com.whensunset.annotation_processing.preference.model

import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import com.whensunset.annotation.preference.PreferenceContext
import java.lang.reflect.Type

class OtherObjectMethodBuilder(var type: TypeSpec.Builder, var savers: MutableMap<TypeName,
        MethodSpec.Builder>) : BaseMethodBuilder(type, savers) {
    override fun buildRealDefault(def: String): String = def
    override fun buildRealType(rawType: TypeName): TypeName =
            PreferenceHelperBuilder.sTypeString

    companion object {
        private const val sParamClass = "clazz"
        private const val sVarValue = "value"
    }

    override fun buildRealValue(param: String): CodeBlock =
            CodeBlock.of("\$T.serialize($param)", PreferenceContext::class.java)

    override fun buildGet(getter: MethodSpec.Builder, pureName: String, realkey: CodeBlock,
                          def: String, type: String, rawType: TypeName, realType: TypeName): MethodSpec.Builder =
            getter.addParameter(Type::class.java, sParamClass)
                    .addStatement("\$T ${sVarValue} = ${PreferenceHelperBuilder.sFieldPreference}.getString" +
                            "(\$L, \$S)", String::class.java, realkey, def)
                    .beginControlFlow("if(${sVarValue} == null)")
                    .addStatement("return null")
                    .endControlFlow()
                    .addStatement("return \$T.deserialize(${sVarValue}, ${sParamClass})", PreferenceContext::class
                            .java)
                    .returns(rawType)
}