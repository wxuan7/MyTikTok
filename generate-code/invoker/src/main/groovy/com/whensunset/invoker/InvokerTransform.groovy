package com.whensunset.invoker

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.google.gson.Gson
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

public class InvokerTransform extends Transform {
    static final Gson GSON = new Gson()

    Project mProject
    private Scanner mScanner
    private Set<File> mJars = new HashSet<>()
    private Set<File> mClassFiles = new HashSet<>()

    @Override
    void transform(Context context, Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider, boolean isIncremental)
            throws IOException, TransformException, InterruptedException {
        long start = System.currentTimeMillis()
        // Transform的inputs有两种类型，一种是目录，一种是jar包，要分开遍历
        inputs.each { TransformInput input ->
            // 必须处理，否则android app module里的类无法打到最终的apk中
            input.directoryInputs.each { DirectoryInput directoryInput ->
                // 获取output目录
                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes,
                        Format.DIRECTORY)

                // 将input的目录复制到output指定目录
                FileUtils.copyDirectory(directoryInput.file, dest)
                mClassFiles.add(dest)
            }
            // 仅处理Jar
            // 对类型为jar文件的input进行遍历
            input.jarInputs.each { JarInput jarInput ->
                // 重命名输出文件（同目录copyFile会冲突）
                def jarName = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                //生成输出路径
                def dest = outputProvider.getContentLocation(jarName + md5Name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
                //将输入内容复制到输出
                FileUtils.copyFile(jarInput.file, dest)
                mJars.add(dest)
            }
        }
        println("invoker ${System.currentTimeMillis() - start}ms used for copy files")
        start = System.currentTimeMillis()
        mScanner = new Scanner(mProject.rootProject.ext.invokerConfig['fileName'])
        int round = 0
        boolean finish = false
        while (!finish) {
            mJars.each { File jar ->
                mScanner.scan(jar, round)
            }
            mClassFiles.each { File file ->
                mScanner.addToPath(file)
            }
            finish = mScanner.hasFinished()
            println("invoker success " + finish + " left " + mScanner.mUnmatchedClasses.toString())
            round++
        }
        println("invoker ${System.currentTimeMillis() - start}ms used for scan")
        start = System.currentTimeMillis()
        new Modifier(context.getPath().contains('Debug'), mScanner.mInfos).modify()
        println("invoker ${System.currentTimeMillis() - start}ms used for modify")
    }

    public InvokerTransform(Project project) {
        mProject = project
    }

    @Override
    String getName() {
        return "invoker"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_JARS
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

}