package com.whensunset.invoker

import com.google.common.io.ByteStreams
import javassist.ClassPath
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
            ClassPath classPath = mPool.appendClassPath(jar.getAbsolutePath())
            Map<String, byte[]> newClasses = new HashMap<>()
            map.each { String invoker, Set<Invocation> invocations ->
                newClasses.put(invoker, modifyClass(invoker, invocations))
            }

            println("invoker:close classPath")
            mPool.removeClassPath(classPath)
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

    private void writeFile(File jarFile, Map<String, byte[]> newClasses) {
        println("invoker" + jarFile.absolutePath)
        def tmpFile = new File(jarFile.getParent(), jarFile.getName() + ".tmp")
        if (tmpFile.exists()) {
            tmpFile.delete()
        }
        JarOutputStream output = new JarOutputStream(new FileOutputStream(tmpFile))
        def jar = new JarFile(jarFile)
        jar.entries().each { entry ->
            InputStream input = null
            if (entry.getName().endsWith('.class')) {
                def className = pathToClass(entry.getName())
                byte[] bytes = newClasses.get(className)
                if (bytes != null) {
                    input = new ByteArrayInputStream(bytes)
                    if (mIsDebug) {
                        File out = new File(jarFile.getParent(), "${className}.modified.class")
                        if (out.exists()) {
                            out.delete()
                            out.createNewFile()
                        }
                        ByteStreams.copy(new ByteArrayInputStream(bytes), new FileOutputStream(out))
                    }
                    println("invoker modify ${entry.getName()} in ${jarFile.getName()}")
                }
            }
            if (input == null) {
                input = jar.getInputStream(entry)
            }
            ZipEntry outEntry = new ZipEntry(entry.getName())
            output.putNextEntry(outEntry)
            ByteStreams.copy(input, output)

            if (input != null) {
                try {
                    println("invoker:close input")
                    input.close()
                    output.closeEntry()
                } catch (IOException var3) {
                    println("invoker:" + var3.getCause())
                    println("invoker:" + var3.getMessage())
                    println("invoker:" + var3.getLocalizedMessage())
                }
            }
        }
        if (jar != null) {
            try {
                println("invoker:close jar output")
                jar.close()
                output.close()
            } catch (IOException var3) {
                println("invoker:" + var3.getCause())
                println("invoker:" + var3.getMessage())
                println("invoker:" + var3.getLocalizedMessage())
            }
        }

        if (mIsDebug) {
            boolean renameSucceed = jarFile.renameTo(new File(jarFile.getParent(), "${jarFile.getName()}.backup.jar"))
            println("invoker rename " + jarFile.getName() + ":" + renameSucceed)
        }
        if (jarFile.exists()) {
            println("invoker delete " + jarFile.getName() + ":" + jarFile.delete())
        }
        tmpFile.renameTo(jarFile)
    }

    private String pathToClass(String path) {
        return path.replace('/', '.').replace('.class', '')
    }
}
