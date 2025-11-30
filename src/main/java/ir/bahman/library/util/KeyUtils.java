package ir.bahman.library.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyUtils {
    public KeyUtils() {}

    public static PrivateKey loadPrivateKey(String path) throws Exception {
        String pem = readResourceAsString(path);
        String key = pem
                .replaceAll("-----BEGIN (.*)PRIVATE KEY-----", "")
                .replaceAll("-----END (.*)PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    public static PublicKey loadPublicKey(String path) throws Exception {
        String pem = readResourceAsString(path);
        String key = pem
                .replaceAll("-----BEGIN (.*)PUBLIC KEY-----", "")
                .replaceAll("-----END (.*)PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private static String readResourceAsString(String resourcePath) throws IOException {
        try (InputStream is = KeyUtils.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Key not found: " + resourcePath);
            }
            return new String(is.readAllBytes());
        }
    }
}
