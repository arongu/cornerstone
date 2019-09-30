package com.aron.crypto;

import com.aron.jcore.crypto.AESTool;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

import static com.aron.jcore.crypto.AESTool.*;
import static org.junit.jupiter.api.Assertions.*;

class AESToolTest {
    @Test
    @DisplayName("derive256BitKey() exception tests")
    void derive256bitKey_ThrowTest() {
        assertThrows(AESTool.AESToolException.class, () -> { derive256BitKey(null,null); });
        assertThrows(AESTool.AESToolException.class, () -> { derive256BitKey("password",null); });
        assertThrows(AESTool.AESToolException.class, () -> { derive256BitKey(null,"short"); });
        assertThrows(AESTool.AESToolException.class, () -> { derive256BitKey("password","abcdef0123456789-"); });
        assertThrows(AESTool.AESToolException.class, () -> { derive256BitKey("","abcdef0123456789"); });
    }

    @Test
    @DisplayName("derive256BitKey() Java vs GO test")
    void derive256bitKey_test_against_go() throws AESTool.AESToolException, DecoderException {
        byte[] expected = Hex.decodeHex("1D9E9E7E2BD8B7840124A79F4D486ECD81BD53E2511DA83BEE3F3642A5C7A0AD".toCharArray());
        SecretKeySpec key = derive256BitKey("password", "abcdef0123456789");

        byte[] ba = key.getEncoded();
        for (int i = 0; i < ba.length; i++){
            System.out.printf("%02X ", ba[i]);
        }
        System.out.println();

        for (int i = 0; i < expected.length; i++){
            System.out.printf("%02X ", expected[i]);
        }
        System.out.println();

        assertArrayEquals(expected, ba);
    }

    @Test
    @DisplayName("encryptByteArray() -> decryptByteArray test")
    void encryptDecryptTest() throws AESTool.AESToolException {
        final SecretKeySpec key = derive256BitKey("password", "abcdef0123456789");
        final String text = "this is my super-secret text with ~!@#$%^&*()_+ all sort of characters";

        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = encryptByteArray(key, data);
        byte[] decrypted = decryptByteArray(key, encrypted);
        String str = new String(decrypted);

        System.out.println("text    (original) : " + text);
        System.out.println("text       (bytes) : " + Arrays.toString(data));
        System.out.println("encrypted  (bytes) : " + Arrays.toString(encrypted));
        System.out.println("decrypted  (bytes) : " + Arrays.toString(decrypted));
        System.out.println("decrypted (string) : " + str);

        assertArrayEquals(data, decrypted);
        assertEquals(text, str);
    }

    @Test
    @DisplayName("alma x100 encrypt() -> decrypt() test, IV should change")
    void testIvChangeWhenBulk() throws AESToolException {
        SecretKeySpec key = derive256BitKey("password", "abcdef0123456789");
        byte[] ba = key.getEncoded();
        String base64key = Base64.getEncoder().encodeToString(ba);

        String text = "alma";
        List<String> lst = new LinkedList<>();
        for ( int i = 0; i < 100; i++){
            lst.add(text);
        }

        List<String> encryptedBase64List = encrypt(base64key, lst);
        for ( String b64 : encryptedBase64List){
            System.out.println(b64);
        }

        List<String> decryptedStrings = decrypt(base64key, encryptedBase64List);
        for ( String s : decryptedStrings){
            System.out.println(s);
            assertEquals(text, s);
        }
    }

//    @Test
//    public void doit() throws AESToolException {
//        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
//        String base64key = Base64.getEncoder().encodeToString(key.getEncoded());
//
//        String encrypted = encrypt( "","");
//        System.out.println(base64key);
//        System.out.println(encrypted);
//    }

//    @Test
//    @DisplayName("256 bit key encryption comparison with Go")
//    void derive256bitKeyByteArrayComparison() throws AESTool.AESToolException {
//        SecretKeySpec key = derive256BitKey("password", "abcdef0123456789");
//
//        //d108c7e7e61ec413b2007d1f4fd7ec1ca96c99a7e8005d80062b29e177673909
//        byte[] encrypted = encryptByteArray(key, "data".getBytes());
//        for (int i = 0; i < encrypted.length; i++){
//            System.out.printf("%02X", encrypted[i]);
//        }
//    }

//    @Test
//    @DisplayName("encrypt-decrypt base64")
//    void encryptDecryptBase64Test() throws AESToolException {
//        SecretKeySpec key = derive256BitKey("password", "abcdef0123456789");
//        String text = "Hello Go, this is Java 11 :) using AES256 encryption";
//        byte[] data = text.getBytes(StandardCharsets.UTF_8);
//
//        List<byte[]> aa = new LinkedList<>();
//        aa.add(data);
//
//        List<String> l = encryptByteArraysToBase64Strings(key, aa);
//        for (String s : l){
//            System.out.println(l);
//        }
//
//        aa = decryptBase64StringsToByteArrays(key, l);
//        for ( byte[] bb : aa ) {
//            System.out.println(new String(bb));
//        }
//    }
}
