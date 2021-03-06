package engsec;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.function.Supplier;

public class KeyStoreUtil {


    public static void saveKeyStore(KeyStore ks, String path, char[] pwd) {
        throwingRunnableWrapper(() -> {
            if (ks.getType().equals("PKCS11")) {
                ks.store(null, pwd);
            } else {
                try (OutputStream out = Files.newOutputStream(Paths.get(path))) {
                    ks.store(out, pwd);
                }
            }
        }).run();
    }

    /**
     * Load a keysstore inferring the type from the file name extension
     *
     * @param path if null we use PKCS11 as type
     * @param pwd
     * @return KeyStore (loaded)
     */
    public static KeyStore loadKeyStore(String path, char[] pwd) {
        return throwingSupplierWrapper(() -> {

            KeyStore keyStore = KeyStore.getInstance(path == null ? "PKCS11" : inferTypeFromFile(path));

            if (path == null) {
                keyStore.load(null, pwd);
            } else {
                try (InputStream in = Files.newInputStream(Paths.get(path))) {
                    keyStore.load(in, pwd);
                }
            }

            return keyStore;
        }).get();
    }

    public static void transferCert(KeyStore src, String srcAlias, KeyStore dest, String destAlias) {
        throwingRunnableWrapper(() -> {

            Certificate crt = null;

            if (!src.containsAlias(srcAlias) || (crt = src.getCertificate(srcAlias)) == null)
                throw new RuntimeException("Alias " + srcAlias + " does not exist in src");

            dest.setCertificateEntry(destAlias, crt);

        }).run();
    }

    public static void transferKey(KeyStore src, String srcAlias, KeyStore dest, String destAlias, char[] pwd) {
        throwingRunnableWrapper(() -> {

            Key key = null;

            if ((key = src.getKey(srcAlias, pwd)) == null)
                throw new RuntimeException("Alias " + srcAlias + " does not exist in src");


            dest.setKeyEntry(destAlias, key, pwd, src.getCertificateChain(srcAlias));
        }).run();
    }

    public static final String inferTypeFromFile(String file) {
        if (file == null)
            return "PKCS11";
        else if (file.endsWith("jks") || file.endsWith("keystore"))
            return "JKS";
        else if (file.endsWith("p12") || file.endsWith("pfx"))
            return "PKCS12";
        else
            throw new RuntimeException("Cannot infer keystore type from file name, please used either .p12, .jks, or .keystore");
    }

    private static <T> Supplier<T> throwingSupplierWrapper(
            ThrowingSupplier<T, Exception> supplier) {

        return () -> {
            try {
                return supplier.get();
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    private static Runnable throwingRunnableWrapper(
            ThrowingRunnable<Exception> runnable) {

        return () -> {
            try {
                runnable.run();
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T, E extends Throwable> {
        T get() throws E;
    }

    @FunctionalInterface
    public interface ThrowingRunnable<E extends Throwable> {
        void run() throws E;
    }
}
