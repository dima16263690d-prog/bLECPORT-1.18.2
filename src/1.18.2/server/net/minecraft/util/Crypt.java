/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import net.minecraft.util.CryptException;

public class Crypt {
    private static final String SYMMETRIC_ALGORITHM = "AES";
    private static final int SYMMETRIC_BITS = 128;
    private static final String ASYMMETRIC_ALGORITHM = "RSA";
    private static final int ASYMMETRIC_BITS = 1024;
    private static final String BYTE_ENCODING = "ISO_8859_1";
    private static final String HASH_ALGORITHM = "SHA-1";

    public static SecretKey generateSecretKey() throws CryptException {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(SYMMETRIC_ALGORITHM);
            keyGenerator.init(128);
            return keyGenerator.generateKey();
        }
        catch (Exception exception) {
            throw new CryptException(exception);
        }
    }

    public static KeyPair generateKeyPair() throws CryptException {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ASYMMETRIC_ALGORITHM);
            keyPairGenerator.initialize(1024);
            return keyPairGenerator.generateKeyPair();
        }
        catch (Exception exception) {
            throw new CryptException(exception);
        }
    }

    public static byte[] digestData(String string, PublicKey publicKey, SecretKey secretKey) throws CryptException {
        try {
            return Crypt.digestData(string.getBytes(BYTE_ENCODING), secretKey.getEncoded(), publicKey.getEncoded());
        }
        catch (Exception exception) {
            throw new CryptException(exception);
        }
    }

    private static byte[] digestData(byte[] ... byArray) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);
        for (byte[] byArray2 : byArray) {
            messageDigest.update(byArray2);
        }
        return messageDigest.digest();
    }

    public static PublicKey byteToPublicKey(byte[] byArray) throws CryptException {
        try {
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(byArray);
            KeyFactory keyFactory = KeyFactory.getInstance(ASYMMETRIC_ALGORITHM);
            return keyFactory.generatePublic(x509EncodedKeySpec);
        }
        catch (Exception exception) {
            throw new CryptException(exception);
        }
    }

    public static SecretKey decryptByteToSecretKey(PrivateKey privateKey, byte[] byArray) throws CryptException {
        byte[] byArray2 = Crypt.decryptUsingKey(privateKey, byArray);
        try {
            return new SecretKeySpec(byArray2, SYMMETRIC_ALGORITHM);
        }
        catch (Exception exception) {
            throw new CryptException(exception);
        }
    }

    public static byte[] encryptUsingKey(Key key, byte[] byArray) throws CryptException {
        return Crypt.cipherData(1, key, byArray);
    }

    public static byte[] decryptUsingKey(Key key, byte[] byArray) throws CryptException {
        return Crypt.cipherData(2, key, byArray);
    }

    private static byte[] cipherData(int n, Key key, byte[] byArray) throws CryptException {
        try {
            return Crypt.setupCipher(n, key.getAlgorithm(), key).doFinal(byArray);
        }
        catch (Exception exception) {
            throw new CryptException(exception);
        }
    }

    private static Cipher setupCipher(int n, String string, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance(string);
        cipher.init(n, key);
        return cipher;
    }

    public static Cipher getCipher(int n, Key key) throws CryptException {
        try {
            Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");
            cipher.init(n, key, new IvParameterSpec(key.getEncoded()));
            return cipher;
        }
        catch (Exception exception) {
            throw new CryptException(exception);
        }
    }
}

