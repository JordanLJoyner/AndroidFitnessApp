//
// Created by Jordan on 6/8/2017.
//
#include <jni.h>

extern "C"
JNIEXPORT void JNICALL Java_com_jordan_jordanfitnessapp_MainActivity_doJNIToastExample(JNIEnv * env, jobject obj)
{
    // Construct a String
    jstring jstr = env->NewStringUTF("This string comes from JNI");
    // First get the class that contains the method you need to call
    jclass clazz = env->FindClass("com/jordan/jordanfitnessapp/MainActivity");
    // Get the method that you want to call
    jmethodID printToast = env->GetMethodID(clazz, "invokeToast", "(Ljava/lang/String;)V");
    // Call the method on the object
    env->CallVoidMethod(obj, printToast, jstr);

}