package parkir.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Penyimpanan password dengan PBKDF2-HMAC-SHA256 + salt acak per pengguna.
 * Murni JDK, tanpa pustaka tambahan.
 */
public final class Hash {

    private static final int ITERASI = 120_000;
    private static final int PANJANG_KUNCI = 256;
    private static final SecureRandom ACAK = new SecureRandom();

    private Hash() {
    }

    public static String buatSalt() {
        byte[] salt = new byte[16];
        ACAK.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hash(String password, String saltBase64) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(),
                    Base64.getDecoder().decode(saltBase64), ITERASI, PANJANG_KUNCI);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return Base64.getEncoder().encodeToString(factory.generateSecret(spec).getEncoded());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("PBKDF2WithHmacSHA256 tidak tersedia", e);
        }
    }

    public static boolean cocok(String password, String saltBase64, String hashBase64) {
        if (password == null || saltBase64 == null || hashBase64 == null) {
            return false;
        }
        byte[] hasil = Base64.getDecoder().decode(hash(password, saltBase64));
        byte[] tersimpan = Base64.getDecoder().decode(hashBase64);
        return MessageDigest.isEqual(hasil, tersimpan);
    }
}
