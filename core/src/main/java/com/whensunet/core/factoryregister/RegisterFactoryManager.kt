package com.whensunet.core.factoryregister

import android.os.Build
import android.support.annotation.NonNull
import com.google.common.reflect.Reflection
import com.whensunset.annotation.singleton.Factory
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by whensunset on 2018/10/2.
 */
object RegisterFactoryManager {

    private val TAG = "PluginManager"

    // 所有对这个map的操作都要锁这个map
    private val FACTORY_MAP = HashMap<Class<*>, Factory<*>>()
    private val IMPLEMENT_MAP = ConcurrentHashMap<Class<*>, Any>()

    private lateinit var invocationHandler: InvocationHandler
    private var mappingMap: Map<Class<Any>, List<RegisterCall.RegisterFactoryMapping>>? = null
    @Volatile private var initDone = false

    fun init() {
        synchronized(FACTORY_MAP) {
            if (initDone) {
                return
            }

            invocationHandler = object : InvocationHandler {
                @Throws(Throwable::class)
                override fun invoke(proxy: Any, method: Method, args: Array<Any>): Any? {
                    val retType = method.returnType
                    when {
                        ClassHelper.isInteger(retType) -> return 0
                        ClassHelper.isBoolean(retType) -> return false
                        ClassHelper.isString(retType) -> return ""
                        retType.isInterface -> {
                            val anno = method.getAnnotation<NonNull>(NonNull::class.java)
                            return if (anno != null) Reflection.newProxy<Any>(retType as Class<Any>?, this) else null
                        }
                        else -> return null
                    }
                }
            }
            mappingMap = RegisterCall.getConfig()
            processRegisterInfo()
            initDone = true
        }
    }

    operator fun <T> get(c: Class<T>): T? {
        if (!initDone) {
            init()
        }
        var implement: Any? = IMPLEMENT_MAP[c]
        if (implement == null) {
            if (FACTORY_MAP.containsKey(c)) {
                implement = FACTORY_MAP[c]?.newInstance()
            }
            if (implement == null) {
                if (c.isInterface) {
                    // 解决类型推断
                    implement = getDefaultImpl(c)
                }
            }
            if (implement != null) {
                IMPLEMENT_MAP.put(c, implement)
            }
        }
        return implement as T?
    }

    fun <T> create(c: Class<T>): T {
        if (!initDone) {
            init()
        }

        return if (FACTORY_MAP.containsKey(c)) {
            FACTORY_MAP[c]?.newInstance() as T
        } else getDefaultImpl(c)
    }

    private fun processRegisterInfo() {
        synchronized(FACTORY_MAP) {
            for (interfaceClass in mappingMap!!.keys) {
                val candidates = mappingMap!![interfaceClass]
                val factory = findBestFitImpl(candidates)
                if (factory != null) {
                    FACTORY_MAP.put(interfaceClass, factory)
                }
            }
        }
    }

    private fun <T> getDefaultImpl(clss: Class<T>): T {
        return Reflection.newProxy(clss, invocationHandler)
    }

    private fun findBestFitImpl(list: List<RegisterCall.RegisterFactoryMapping>?): Factory<*>? {
        Collections.sort(list) { o1, o2 -> o2.mMinSdk - o1.mMinSdk }
        return list!!
                .firstOrNull { Build.VERSION.SDK_INT >= it.mMinSdk }
                ?.mFactory
    }

}