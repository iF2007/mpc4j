/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class edu_alibaba_mpc4j_s2pc_pir_index_xpir_Mbfk16IndexPirNativeUtils */

#ifndef _Included_edu_alibaba_mpc4j_s2pc_pir_index_xpir_Mbfk16IndexPirNativeUtils
#define _Included_edu_alibaba_mpc4j_s2pc_pir_index_xpir_Mbfk16IndexPirNativeUtils
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     edu_alibaba_mpc4j_s2pc_pir_index_xpir_Mbfk16IndexPirNativeUtils
 * Method:    generateSealContext
 * Signature: (IJ)[B
 */
JNIEXPORT jbyteArray JNICALL Java_edu_alibaba_mpc4j_s2pc_pir_index_xpir_Mbfk16IndexPirNativeUtils_generateSealContext
  (JNIEnv *, jclass, jint, jlong);

/*
 * Class:     edu_alibaba_mpc4j_s2pc_pir_index_xpir_Mbfk16IndexPirNativeUtils
 * Method:    keyGen
 * Signature: ([B)Ljava/util/List;
 */
JNIEXPORT jobject JNICALL Java_edu_alibaba_mpc4j_s2pc_pir_index_xpir_Mbfk16IndexPirNativeUtils_keyGen
  (JNIEnv *, jclass, jbyteArray);

/*
 * Class:     edu_alibaba_mpc4j_s2pc_pir_index_xpir_Mbfk16IndexPirNativeUtils
 * Method:    nttTransform
 * Signature: ([BLjava/util/List;)Ljava/util/ArrayList;
 */
JNIEXPORT jobject JNICALL Java_edu_alibaba_mpc4j_s2pc_pir_index_xpir_Mbfk16IndexPirNativeUtils_nttTransform
  (JNIEnv *, jclass, jbyteArray, jobject);

/*
 * Class:     edu_alibaba_mpc4j_s2pc_pir_index_xpir_Mbfk16IndexPirNativeUtils
 * Method:    generateQuery
 * Signature: ([B[B[B[I[I)Ljava/util/ArrayList;
 */
JNIEXPORT jobject JNICALL Java_edu_alibaba_mpc4j_s2pc_pir_index_xpir_Mbfk16IndexPirNativeUtils_generateQuery
  (JNIEnv *, jclass, jbyteArray, jbyteArray, jbyteArray, jintArray, jintArray);

/*
 * Class:     edu_alibaba_mpc4j_s2pc_pir_index_xpir_Mbfk16IndexPirNativeUtils
 * Method:    generateReply
 * Signature: ([BLjava/util/List;Ljava/util/List;[I)Ljava/util/ArrayList;
 */
JNIEXPORT jobject JNICALL Java_edu_alibaba_mpc4j_s2pc_pir_index_xpir_Mbfk16IndexPirNativeUtils_generateReply
  (JNIEnv *, jclass, jbyteArray, jobject, jobject, jintArray);

/*
 * Class:     edu_alibaba_mpc4j_s2pc_pir_index_xpir_Mbfk16IndexPirNativeUtils
 * Method:    decryptReply
 * Signature: ([B[BLjava/util/List;I)[J
 */
JNIEXPORT jlongArray JNICALL Java_edu_alibaba_mpc4j_s2pc_pir_index_xpir_Mbfk16IndexPirNativeUtils_decryptReply
  (JNIEnv *, jclass, jbyteArray, jbyteArray, jobject, jint);

/*
 * Class:     edu_alibaba_mpc4j_s2pc_pir_index_xpir_Mbfk16IndexPirNativeUtils
 * Method:    expansionRatio
 * Signature: ([B)I
 */
JNIEXPORT jint JNICALL Java_edu_alibaba_mpc4j_s2pc_pir_index_xpir_Mbfk16IndexPirNativeUtils_expansionRatio
  (JNIEnv *, jclass, jbyteArray);

#ifdef __cplusplus
}
#endif
#endif