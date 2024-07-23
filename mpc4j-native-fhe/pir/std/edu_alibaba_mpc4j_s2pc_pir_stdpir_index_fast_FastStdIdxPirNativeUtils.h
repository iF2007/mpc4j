/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class edu_alibaba_mpc4j_s2pc_pir_stdpir_index_fast_FastStdIdxPirNativeUtils */

#ifndef _Included_edu_alibaba_mpc4j_s2pc_pir_stdpir_index_fast_FastStdIdxPirNativeUtils
#define _Included_edu_alibaba_mpc4j_s2pc_pir_stdpir_index_fast_FastStdIdxPirNativeUtils
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     edu_alibaba_mpc4j_s2pc_pir_stdpir_index_fast_FastStdIdxPirNativeUtils
 * Method:    generateEncryptionParams
 * Signature: (IJ[J)[B
 */
JNIEXPORT jbyteArray JNICALL Java_edu_alibaba_mpc4j_s2pc_pir_stdpir_index_fast_FastStdIdxPirNativeUtils_generateEncryptionParams
  (JNIEnv *, jclass, jint, jlong, jlongArray);

/*
 * Class:     edu_alibaba_mpc4j_s2pc_pir_stdpir_index_fast_FastStdIdxPirNativeUtils
 * Method:    keyGen
 * Signature: ([B[I)Ljava/util/List;
 */
JNIEXPORT jobject JNICALL Java_edu_alibaba_mpc4j_s2pc_pir_stdpir_index_fast_FastStdIdxPirNativeUtils_keyGen
  (JNIEnv *, jclass, jbyteArray, jintArray);

/*
 * Class:     edu_alibaba_mpc4j_s2pc_pir_stdpir_index_fast_FastStdIdxPirNativeUtils
 * Method:    nttTransform
 * Signature: ([B[[J)Ljava/util/List;
 */
JNIEXPORT jobject JNICALL Java_edu_alibaba_mpc4j_s2pc_pir_stdpir_index_fast_FastStdIdxPirNativeUtils_nttTransform
  (JNIEnv *, jclass, jbyteArray, jobjectArray);

/*
 * Class:     edu_alibaba_mpc4j_s2pc_pir_stdpir_index_fast_FastStdIdxPirNativeUtils
 * Method:    generateQuery
 * Signature: ([B[B[BII)Ljava/util/List;
 */
JNIEXPORT jobject JNICALL Java_edu_alibaba_mpc4j_s2pc_pir_stdpir_index_fast_FastStdIdxPirNativeUtils_generateQuery
  (JNIEnv *, jclass, jbyteArray, jbyteArray, jbyteArray, jint, jint);

/*
 * Class:     edu_alibaba_mpc4j_s2pc_pir_stdpir_index_fast_FastStdIdxPirNativeUtils
 * Method:    generateResponse
 * Signature: ([B[BLjava/util/List;[[BI)[B
 */
JNIEXPORT jbyteArray JNICALL Java_edu_alibaba_mpc4j_s2pc_pir_stdpir_index_fast_FastStdIdxPirNativeUtils_generateResponse
  (JNIEnv *, jclass, jbyteArray, jbyteArray, jobject, jobjectArray, jint);

/*
 * Class:     edu_alibaba_mpc4j_s2pc_pir_stdpir_index_fast_FastStdIdxPirNativeUtils
 * Method:    decodeResponse
 * Signature: ([B[B[B)[J
 */
JNIEXPORT jlongArray JNICALL Java_edu_alibaba_mpc4j_s2pc_pir_stdpir_index_fast_FastStdIdxPirNativeUtils_decodeResponse
  (JNIEnv *, jclass, jbyteArray, jbyteArray, jbyteArray);

#ifdef __cplusplus
}
#endif
#endif
