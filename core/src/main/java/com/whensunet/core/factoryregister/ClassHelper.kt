package com.whensunet.core.factoryregister

object ClassHelper {

    fun isNumber(clss: Class<*>): Boolean {
        return isInteger(clss) || isFloatingPoint(clss)
    }

    fun isInteger(clss: Class<*>): Boolean {
        return (clss == Byte::class.javaPrimitiveType || clss == Char::class.javaPrimitiveType || clss == Short::class.javaPrimitiveType || clss == Int::class.javaPrimitiveType
                || clss == Long::class.javaPrimitiveType || clss == Byte::class.java || clss == Char::class.java
                || clss == Short::class.java || clss == Int::class.java || clss == Long::class.java)
    }

    fun isFloatingPoint(clss: Class<*>): Boolean {
        return (clss == Float::class.javaPrimitiveType || clss == Double::class.javaPrimitiveType || clss == Float::class.java
                || clss == Double::class.java)
    }

    fun isBoolean(clss: Class<*>): Boolean {
        return clss == Boolean::class.javaPrimitiveType || clss == Boolean::class.java
    }

    fun isVoid(clss: Class<*>): Boolean {
        return clss == Void.TYPE || clss == Void::class.java
    }

    fun isString(clss: Class<*>): Boolean {
        return clss == String::class.java
    }
}
