/*******************************************************************************
 * Copyright (c) 2013 Hani Naguib.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Hani Naguib - initial API and implementation
 ******************************************************************************/
package com.gvmax.common.util;

import java.security.Key;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

/**
 * Utility used to encrypt and decrypt values.
 * If the provided password isBlank than encryption is disabled.
 */
public class Enc {
    private static final Logger logger = Logger.getLogger(Enc.class);
    /** Is encruption enabled */
    private boolean enabled;
    /** Key to use */
    private Key key;
    /** Cipher to use */
    private Cipher c;

    private static byte[] salt = { (byte) 0x19, (byte) 0x73, (byte) 0x41, (byte) 0x8c, (byte) 0x7e, (byte) 0xd8, (byte) 0xee, (byte) 0x89 };
    private static byte[] iv = { (byte) 0x19, (byte) 0x73, (byte) 0x41, (byte) 0x8c, (byte) 0x7e, (byte) 0xd8, (byte) 0xee, (byte) 0x89, (byte) 0x19, (byte) 0x73, (byte) 0x41, (byte) 0x8c, (byte) 0x7e, (byte) 0xd8, (byte) 0xee, (byte) 0x89 };

    // --------------
    // CONSTRUCTORS
    // --------------

    public Enc(String password) {
        this(password, 256);
    }

    public Enc(String password, int keyLength) {
        if (password == null || password.trim().equals("")) {
            enabled = false;
        } else {
            enabled = true;
            try {
                SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 1024, keyLength);
                key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
                c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    // ------------
    // ENC/DEC
    // ------------

    public String encrypt(String valueToEnc) {
        if (!enabled) {
            return valueToEnc;
        }
        if (valueToEnc == null) {
            return null;
        }
        try {
            c.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
            byte[] encValue = c.doFinal(valueToEnc.getBytes("UTF-8"));
            return Base64.encodeBase64String(encValue);
        } catch (Exception e) {
            logger.warn("Unable to encrypt: "+valueToEnc,e);
            return null;
        }
    }

    public String decrypt(String encryptedValue) {
        if (!enabled) {
            return encryptedValue;
        }
        byte[] val = decryptByte(encryptedValue);
        if (val == null) {
            return null;
        }
        return StringUtil.toString(val);
    }

    public byte[] decryptByte(String encryptedValue) {
        if (encryptedValue == null) {
            return null;
        }
        if (!enabled) {
            return StringUtil.getBytes(encryptedValue);
        }
        try {
            c.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
            byte[] decordedValue = Base64.decodeBase64(encryptedValue);
            return c.doFinal(decordedValue);
        } catch (Exception e) {
            logger.warn("Unable to decrypt: "+encryptedValue,e);
            return null;
        }
    }

}
