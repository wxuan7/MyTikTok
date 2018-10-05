package com.whensunset.annotation_processing.preference.model

import com.squareup.javapoet.*
import com.whensunset.annotation.preference.PreferenceContext
import com.whensunset.annotation_processing.preference.proxy.DefaultValue
import com.whensunset.annotation_processing.preference.proxy.DefaultValueType
import com.whensunset.annotation_processing.util.ClassBuilder
import java.util.*
import javax.lang.model.element.Modifier
import kotlin.collections.HashMap

fun TypeName.getName(): String {
    if (isPrimitive || isBoxedPrimitive) {
        return unbox().toString().capitalize()
    } else {
        // Class 的结尾会是 >
        return toString().substringAfterLast('.').removeSuffix(">");
    }
}

class PreferenceHelperBuilder : ClassBuilder {
    private val mConstClass: TypeSpec.Builder by lazy {
        TypeSpec.classBuilder(sClassConstants)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
    }
    private val mKeys: MutableSet<CodeBlock> by lazy {
        HashSet<CodeBlock>()
    }
    private val mSavers: MutableMap<TypeName, MethodSpec.Builder> by lazy {
        LinkedHashMap<TypeName, MethodSpec.Builder>()
    }

    private val mDebugMapBuilder: MethodSpec.Builder by lazy {
        MethodSpec.methodBuilder(sMethodDebugMap)
                .addStatement("\$T ${sVarMap} = new \$T<\$T, \$T>()",
                        HashMap::class.java, HashMap::class.java, String::class.java, Object::class.java)
                .returns(HashMap::class.java)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
    }

    private val mPrimitiverBuilder: BuiltInMethodBuilder  by lazy {
        BuiltInMethodBuilder(mType, mSavers, mDebugMapBuilder)
    }

    private val mOtherObjectBuilder: OtherObjectMethodBuilder by lazy {
        OtherObjectMethodBuilder(mType, mSavers)
    }

    constructor(className: String, pkg: String) : super() {
        mClassName = className
        mPackage = pkg
        mType = TypeSpec.classBuilder(mClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        var preference =
                FieldSpec.builder(sTypePreference, sFieldPreference,
                        Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                        .initializer("(\$T) \$T.get(\$S)",
                                sTypePreference, PreferenceContext::class.java, mClassName)
        mType.addField(preference.build())
    }

    fun onNewField(clazz: TypeName, type: TypeName, key: String, prefix: String?, fieldName:
    String, removable: Boolean, def: DefaultValue, needSaver: Boolean) {

        var pureKey = formatKey(fieldName, key)
        var realKey = getKey(key, pureKey, prefix)

        if (type.isBoxedPrimitive() || type.isPrimitive() || type == sTypeString) {
            mPrimitiverBuilder.appendOperator(clazz, type, fieldName, realKey, pureKey, removable,
                    def.value)
            if (needSaver) {
                mPrimitiverBuilder.appendSaver(clazz, type, fieldName, realKey)
            }
        } else {
            mOtherObjectBuilder.appendOperator(clazz, type, fieldName, realKey, pureKey, removable, def.value)
            if (needSaver) {
                mOtherObjectBuilder.appendSaver(clazz, type, fieldName, realKey)
            }
        }

        if (def.type == DefaultValueType.ASSIGNED) {
            mConstClass.addField(FieldSpec
                    .builder(type, "${sFieldPrefix}${pureKey.toUpperCase()}",
                            Modifier.PUBLIC, Modifier.STATIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer(def.value)
                    .addJavadoc("@see \$T#$fieldName", clazz)
                    .build())
        }
    }

    private fun getKey(key: String, pureKey: String, prefix: String?): CodeBlock {
        var realKey = fullKey(key, pureKey, prefix)
        if (!mKeys.add(realKey)) {
            throw IllegalStateException("key clashed " + realKey)
        }
        return realKey
    }

    override fun build(): TypeSpec.Builder {
        for (method in mSavers.values) {
            mType.addMethod(
                    method.addStatement("${sVarEditor}.apply()")
                            .build())
        }
        mType.addType(mConstClass.build())
        mType.addMethod(
                mDebugMapBuilder
                        .addStatement("return ${sVarMap}")
                        .build())
        return mType
    }

    companion object {
        private const val sClassConstants = "Constants"
        private const val sDefaultFileName = "DefaultPreferenceHelper"

        internal const val sFieldPrefix = "DEFAULT_"
        internal const val sFieldPreference = "sPreferences"
        internal const val sVarEditor = "editor"
        internal const val sVarMap = "map"
        internal const val sParamValue = "value"
        internal const val sParamObject = "object"
        internal const val sMethodDebugMap = "primitiveMap"

        internal val sTypePreference: ClassName by lazy {
            ClassName.get("android.content", "SharedPreferences")
        }
        internal val sTypeEditor: ClassName by lazy {
            ClassName.get("android.content", "SharedPreferences", "Editor")
        }

        internal val sTypeString = ClassName.get(String::class.java)

        private fun fullKey(key: String, formatedKey: String, prefix: String?): CodeBlock {
            var keyForPref = formatedKey
            if (!"".equals(key)) {
                keyForPref = key
            }
            if (prefix == null || "".equals(prefix)) {
                return CodeBlock.of("\$S", keyForPref)
            } else {
                return CodeBlock.of("\$T.getPreferenceKeyPrefix(\$S) + \$S", PreferenceContext::class.java,
                        prefix, keyForPref)
            }
        }

        private fun formatKey(field: String, rawKey: String): String {
            var key = rawKey
            // 默认用field name
            if ("" == key) {
                key = field
            }
            val parts = key.split("_".toRegex())
            if (parts.size == 1) {
                if (key.startsWith("m") && key[1] >= 'A' && key[1] <= 'Z') {
                    key = key.substring(1)
                }
                if (key[0] >= 'a' && key[0] <= 'z') {
                    key = key.substring(0, 1).toUpperCase() + key.substring(1)
                }
                return key
            }
            val builder = StringBuilder()
            for (part in parts) {
                if (part.length != 0) {
                    var partVar = part.toLowerCase()
                    builder.append(partVar.substring(0, 1).toUpperCase()).append(partVar.substring(1))
                }
            }
            return builder.toString()
        }

        fun realName(name: String): String {
            if ("".equals(name)) {
                return sDefaultFileName
            } else {
                return name
            }
        }
    }
}
