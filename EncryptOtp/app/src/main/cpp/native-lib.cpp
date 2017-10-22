#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_rkgoswami_encryptotp_MainActivity_stringFromJNI(
        JNIEnv *env,
        jstring jStr) {

    /*jclass cls = env->FindClass("com/example/rkgoswami/encryptotp");
    jmethodID methodId = env->GetMethodID(cls, "<string>", "(I)V");
    jobject obj = env->NewObject(cls, methodId, param);
    std::string hello = env-> GetStringChars(jStr,0);*/
    std::string hello = "Hello World";
    return env->NewStringUTF(hello.c_str());
}
