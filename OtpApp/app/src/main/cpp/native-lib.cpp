#include <jni.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_sverma_otpapp_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject jObj, jstring jStr) {

    const char *str1 = (*env).GetStringUTFChars(jStr,0);

    //return env->NewStringUTF(hello.c_str());
    return (*env).NewStringUTF(str1);
}
