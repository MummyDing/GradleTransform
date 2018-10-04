package com.github.mummyding.transform

import com.android.SdkConstants
import com.android.annotations.NonNull
import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import com.github.mummyding.Logger

abstract class IncrementTransform extends Transform {

    static final String TAG = "IncrementTransform"

    def DEBUG = true

    @Override
    boolean isIncremental() {
        return true
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    void transform(TransformInvocation invocation) throws TransformException, InterruptedException, IOException {
        //以下实现参考自com.android.build.gradle.internal.transforms.CustomClassTransform

        final TransformOutputProvider outputProvider = invocation.outputProvider
        assert outputProvider != null

        // Output the resources, we only do this if this is not incremental,
        // as the secondary file is will trigger a full build if modified.
        if (!invocation.isIncremental()) {
            outputProvider.deleteAll()
        }

        //transform 开始回调
        Logger.i(TAG, 'onTransformStart')
        onTransformStart(invocation)

        //遍历输入文件，初始化相应参数并复制目标文件
        invocation.inputs.each { TransformInput input ->
            // 遍历目录
            input.directoryInputs.each { DirectoryInput dirInput ->
                final File outputDir = outputProvider.getContentLocation(
                        dirInput.name,
                        dirInput.contentTypes,
                        dirInput.scopes,
                        Format.DIRECTORY)
                // 遍历目录接口
                Logger.d(DEBUG, TAG, 'onEachDirectory: ', dirInput.name)
                onEachDirectory(dirInput)
                if (invocation.isIncremental()) {
                    dirInput.changedFiles.each { File inputFile, Status status ->
                        switch (status) {
                            case Status.NOTCHANGED:
                                break
                            case Status.ADDED:
                            case Status.CHANGED:
                                //增量编译时改变包名的话，Input file 会出现 Directory
                                if (!inputFile.isDirectory() && inputFile.name.endsWith(SdkConstants.DOT_CLASS)) {
                                    // 真正需要处理的文件
                                    File outFile = toOutputFile(outputDir, dirInput.file, inputFile)
                                    Logger.d(TAG, 'onRealTransformFile: dirInput: ', dirInput.name, " inputFile: ", inputFile.name, " outFile: ", outFile.name)
                                    onRealTransformFile(dirInput, inputFile, outFile)
                                }
                                break
                            case Status.REMOVED:
                                File outFile = toOutputFile(outputDir, dirInput.file, inputFile)
                                FileUtils.deleteIfExists(outFile)
                                break
                        }
                    }
                } else {
                    FileUtils.getAllFiles(dirInput.file).each { File file ->
                        if (file.name.endsWith(SdkConstants.DOT_CLASS)) {
                            File outFile = toOutputFile(outputDir, dirInput.file, file)
                            // 真正需要处理的文件
                            Logger.d(DEBUG, TAG, 'onRealTransformFile: ', dirInput.name)
                            onRealTransformFile(dirInput, file, outFile)
                        }
                    }
                }
            }

            //遍历jar
            input.jarInputs.each { JarInput jarInput ->
                final File outJarFile = outputProvider.getContentLocation(
                        jarInput.name,
                        jarInput.contentTypes,
                        jarInput.scopes,
                        Format.JAR)
                // 遍历Jar接口
                //TODO 只添加非 Removed 的文件？
                Logger.d(DEBUG, TAG, 'onEachJar: ', jarInput.name)
                onEachJar(jarInput)
                if (invocation.isIncremental()) {
                    switch (jarInput.getStatus()) {
                        case Status.NOTCHANGED:
                            break
                        case Status.ADDED:
                        case Status.CHANGED:
                            // 真正需要处理的 jar 文件
                            onRealTransformJar(jarInput, outJarFile)
                            break
                        case Status.REMOVED:
                            FileUtils.deleteIfExists(outJarFile)
                            break
                    }
                } else {
                    // 真正需要处理的 jar 文件
                    Logger.d(DEBUG, TAG, 'onRealTransformJar: ', jarInput.name)
                    onRealTransformJar(jarInput, outJarFile)
                }
            }
        }

        //transform 结束回调
        Logger.i(TAG, 'onTransformEnd')
        onTransformEnd(invocation)

    }


    /**
     * For example:<p>
     *     outputDir is {@code /out/}, inputDir is {@code /in/}, inputFile is {@code /in/com/demo/a.class}.
     *     <p>so return {@code /out/com/demo/a.class}
     *
     * @param outputDir
     * @param inputDir
     * @param inputFile
     * @return
     */
    @NonNull
    static File toOutputFile(File outputDir, File inputDir, File inputFile) {
        return new File(outputDir, FileUtils.relativePossiblyNonExistingPath(inputFile, inputDir))
    }

    /**
     * Transform.transform() 接口开始时回调
     */
    abstract void onTransformStart(TransformInvocation invocation)

    /**
     * 遍历 DirectoryInput 时回调
     * @param dirInput
     */
    abstract void onEachDirectory(DirectoryInput dirInput)

    /**
     * 在非增量模式下：对所有输入文件回调；增量模式下：只对有变化的输入文件回调
     * @param dirInput 遍历目录时的 DirectoryInput
     * @param inputFile 遍历 DirectoryInput 时的 File
     * @param outputFile 根据 inputFile 创建的 File
     */
    abstract void onRealTransformFile(DirectoryInput dirInput, File inputFile, File outputFile)

    /**
     * 遍历 JarInput 时回调
     * @param jarInput
     */
    abstract void onEachJar(JarInput jarInput)

    /**
     * 在非增量模式下：对所有输入 Jar 回调；增量模式下：只对有变化的输入 Jar 文件回调
     * @param jarInput 遍历目录时的 JarInput
     * @param outJarFile 根据 JarInput 生成的 File
     */
    abstract void onRealTransformJar(JarInput jarInput, File outJarFile)

    /**
     * Transform.transform() 接口结束时回调
     */
    abstract void onTransformEnd(TransformInvocation invocation)
}