//
// Created by Jordan on 6/6/2017.
//

#include <jni.h>
#include <string>


extern "C"
JNIEXPORT jstring JNICALL
Java_android_jordan_com_userinfomodule_UserInfoManager_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}