package com.whensunset.invoker

import com.google.gson.reflect.TypeToken
import com.whensunset.invoker.model.Invocation
import com.whensunset.invoker.model.InvokerInfos

import java.util.jar.JarEntry
import java.util.jar.JarFile

public class Scanner {
    InvokerInfos mInfos = new InvokerInfos()
    Set<String> mUnmatchedClasses = new HashSet<>()
    String mFile

    public Scanner(String file) {
        mFile = file
    }

    public void scan(File file, int round) {
        JarFile jar = new JarFile(file)
        if (round == 0) {
            scanMetaInfo(jar, file.getAbsolutePath())
        }
        scanClasses(jar, file)

        if (jar != null) {
            try {
                jar.close()
            } catch (IOException var3) {
                println("invoker:" + var3.getCause())
                println("invoker:" + var3.getMessage())
                println("invoker:" + var3.getLocalizedMessage())
            }
        }
    }

    public boolean hasFinished() {
        return mUnmatchedClasses.isEmpty()
    }

    private void scanMetaInfo(JarFile jar, String path) {
        JarEntry entry = jar.getJarEntry(mFile)
        if (entry != null) {
            println("invoker got meta from {$path}")
            jar.getInputStream(entry).withReader { reader ->
                reader.eachLine { String line ->
                    List<Invocation> invocations =
                            InvokerTransform.GSON.fromJson(line,
                                    new TypeToken<List<Invocation>>() {}.getType())
                    invocations.each { Invocation invocation ->
                        mUnmatchedClasses.add(invocation.mTarget.className)
                        mUnmatchedClasses.add(invocation.mInvoker.className)
                    }
                    mInfos.mInvocations.addAll(invocations)
                }
            }
        }
    }

    private void scanClasses(JarFile jar, File file) {
        Set<String> matched = new HashSet<>()
        mUnmatchedClasses.each { String clazz ->
            def path = clazz.replace(".", "/") + ".class"
            if (jar.getJarEntry(path) != null) {
                mInfos.mClassContainers.put(clazz, file)
                matched.add(clazz)
            }
        }
        mUnmatchedClasses.removeAll(matched)
    }

    public void addToPath(File file) {
        Set<String> matched = new HashSet<>()
        mUnmatchedClasses.each { String clazz ->
            def path = clazz.replace(".", "/") + ".class"
            if (new File(file, path).exists()) {
                mInfos.mClassContainers.put(clazz, file)
                matched.add(clazz)
            }
        }
        mUnmatchedClasses.removeAll(matched)
    }
}