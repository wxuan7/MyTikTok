package com.whensunset.annotation_processing.preference.proxy

import com.squareup.javapoet.TypeName
import com.sun.source.util.Trees
import com.whensunset.annotation.preference.BuiltInObjectPreference
import com.whensunset.annotation.preference.OtherObjectPreference
import com.whensunset.annotation_processing.preference.model.PreferenceHelperBuilder
import com.whensunset.annotation_processing.util.Util
import javax.lang.model.element.Element

interface AnnotationProxy {
    fun prefFile(): String
    fun key(): String
    fun prefix(): String
    fun removable(): Boolean
    fun def(): DefaultValue
}

class BuiltInObjectProxy(val mAnnotation: BuiltInObjectPreference, val mField: Element,
                         val mTrees: Trees) : AnnotationProxy {
    override fun def(): DefaultValue {
        var def = Util.assignedValue(mField, mTrees)
        if (def == null) {
            if (TypeName.get(mField.asType()).equals(PreferenceHelperBuilder.sTypeString)) {
                return DefaultValue(DefaultValueType.DEFAULT, "\"\"")
            } else {
                return DefaultValue(DefaultValueType.DEFAULT, Util.defaultValue(TypeName.get(mField
                        .asType())))
            }
        } else {
            return DefaultValue(DefaultValueType.ASSIGNED, def)
        }
    }

    override fun key() = mAnnotation.key
    override fun prefix() = mAnnotation.prefixKey
    override fun removable() = mAnnotation.removable
    override fun prefFile() = mAnnotation.prefFile

}

class OtherObjectProxy(private val mAnnotation: OtherObjectPreference) : AnnotationProxy {
    override fun key() = mAnnotation.key
    override fun prefix() = mAnnotation.prefixKey
    override fun removable() = mAnnotation.removable
    override fun prefFile() = mAnnotation.prefFile
    override fun def() = DefaultValue(DefaultValueType.DEFAULT, mAnnotation.def)
}

enum class DefaultValueType {
    DEFAULT, ASSIGNED, HACKY_ANNOTATION
}

class DefaultValue(val type: DefaultValueType, val value: String)