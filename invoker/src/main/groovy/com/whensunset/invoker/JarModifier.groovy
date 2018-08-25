package com.whensunset.invoker

import com.google.common.io.ByteStreams
import com.google.common.io.Closeables
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod

import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

public class JarModifier {
    private ClassPool mPool = new ClassPool(true)
    private InvokerInfos mInfos
    private Map<File, Map<String, Set<Invocation>>> mFile2InvocationMap = new HashMap<>()
    private boolean mIsDebug;

    public JarModifier(boolean debug, InvokerInfos infos) {
        mIsDebug = debug
        mInfos = infos
    }

    public void modify() {
        restructureMapping()
        mFile2InvocationMap.each { File jar, Map<String, Set<Invocation>> map ->
            println("何时夕:" + jar.getAbsolutePath())
            mPool.appendClassPath(jar.getAbsolutePath())
            Map<String, byte[]> newClasses = new HashMap<>()
            map.each { String invoker, Set<Invocation> invocations ->
                newClasses.put(invoker, modifyClass(invoker, invocations))
            }
            writeFile(jar, newClasses)
        }
    }

    private byte[] modifyClass(String invokerName, Set<Invocation> invocationSet) {
        // 增加排序, 保证每次编译生成的结果类是一样的
        List<Invocation> invocations = new ArrayList<>(invocationSet);
        Collections.sort(invocations, new Comparator<Invocation>() {
            @Override
            int compare(Invocation o1, Invocation o2) {
                return o1.mTarget.className.compareTo(o2.mTarget.className);
            }
        });
        CtClass invoker = mPool.get(invokerName)
        if (invoker.isFrozen()) {
            invoker.defrost()
        }
        invocations.each { Invocation invocation ->
            def targetClass = invocation.mTarget.className
            CtMethod invokerMethod = invoker.getDeclaredMethod(invocation.mInvoker.methodName)
            mPool.appendClassPath(mInfos.fileForClass(targetClass).getAbsolutePath())
            mPool.importPackage(targetClass)
            invokerMethod.insertAfter("${invocation.mTarget.className}.${invocation.mTarget.methodName}();")
        }
        byte[] bytes = invoker.toBytecode()
        invoker.detach()
        return bytes
    }

    private void restructureMapping() {
        mInfos.mInvocations.each { Invocation invocation ->
            File file = mInfos.mClassContainers.get(invocation.mInvoker.className)
            mFile2InvocationMap.computeIfAbsent(file,
                    { key ->
                        return new HashMap<>()
                    })
            mFile2InvocationMap.get(file)
                    .computeIfAbsent(invocation.mInvoker.className,
                    { key -> return new HashSet<>() })
            mFile2InvocationMap.get(file).get(invocation.mInvoker.className).add(invocation)
        }
    }

    private void writeFile(File file, Map<String, byte[]> newClasses) {
        println("invoker" + file.absolutePath)
        def tmpFile = new File(file.getParent(), file.getName() + ".tmp")
        if (tmpFile.exists()) {
            tmpFile.delete()
        }
        JarOutputStream output = new JarOutputStream(new FileOutputStream(tmpFile))
        def jar = new JarFile(file)
        jar.entries().each { entry ->
            InputStream input = null
            if (entry.getName().endsWith('.class')) {
                def className = pathToClass(entry.getName())
                byte[] bytes = newClasses.get(className)
                if (bytes != null) {
                    input = new ByteArrayInputStream(bytes)
                    if (mIsDebug) {
                        File out = new File(file.getParent(), "${className}.modified.class")
                        if (out.exists()) {
                            out.delete()
                            out.createNewFile()
                        }
                        ByteStreams.copy(new ByteArrayInputStream(bytes), new FileOutputStream(out))
                    }
                    println("invoker modify ${entry.getName()} in ${file.getName()}")
                }
            }
            if (input == null) {
                input = jar.getInputStream(entry)
            }
            ZipEntry outEntry = new ZipEntry(entry.getName())
            output.putNextEntry(outEntry)
            ByteStreams.copy(input, output)
            Closeables.close(input, true)
            output.closeEntry()
        }
        Closeables.close(output, true)

        if (mIsDebug) {
            file.renameTo(new File(file.getParent(), "${file.getName()}.backup.jar"))
        }
        if (file.exists()) {
            file.delete()
        }
        tmpFile.renameTo(file)
    }

    private String pathToClass(String path) {
        return path.replace('/', '.').replace('.class', '')
    }
}
