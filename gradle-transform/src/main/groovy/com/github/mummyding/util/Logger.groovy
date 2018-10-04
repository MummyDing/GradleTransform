package com.github.mummyding

class Logger {

    static boolean isDebug = false

    static void d(String TAG, String ... msg) {
        d(isDebug, TAG, msg)
    }

    static void d(boolean shouldPrint, String TAG, String ... msg) {
        if (shouldPrint) {
            def str = ""
            for (String s : msg) {
                str += s + " "
            }
            println("debug " + TAG + " " + str)
        }
    }

    static void i(String TAG, String msg) {
        println("info " + TAG + " " + msg)
    }

}