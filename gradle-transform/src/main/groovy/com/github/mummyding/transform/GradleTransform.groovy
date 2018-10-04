package com.github.mummyding.transform

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.TransformInvocation

class GradleTransform extends IncrementTransform {

    public static final String NAME = "gradle-transform"

    GradleTransform() {
        DEBUG = true
    }

    @Override
    String getName() {
        return NAME
    }

    @Override
    void onTransformStart(TransformInvocation invocation) {

    }

    @Override
    void onEachDirectory(DirectoryInput dirInput) {

    }

    @Override
    void onRealTransformFile(DirectoryInput dirInput, File inputFile, File outputFile) {
    }

    @Override
    void onEachJar(JarInput jarInput) {
    }

    @Override
    void onRealTransformJar(JarInput jarInput, File outJarFile) {

    }

    @Override
    void onTransformEnd(TransformInvocation invocation) {
    }
}