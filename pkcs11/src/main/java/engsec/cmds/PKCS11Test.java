package engsec.cmds;

import io.airlift.airline.Command;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Date;

import static engsec.IO.prn;

@Command(name = "test", description = "Test the pkcs11 config works")
public class PKCS11Test implements Runnable {

    public void run() {
        try {

            prn(Security.getAlgorithms("KeyStore"));

            prn(Security.getProviders());

            //See: https://docs.oracle.com/javase/7/docs/technotes/guides/security/p11guide.html#KeyToolJarSigner
            KeyStore ks = KeyStore.getInstance("PKCS11");
            ks.load(null, "1234".toCharArray());

            // Generate the key
            SecureRandom sr = new SecureRandom();
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", ks.getProvider());
            keyGen.initialize(1024, sr);
            KeyPair keyPair = keyGen.generateKeyPair();
            PrivateKey pk = keyPair.getPrivate();

            // Java API requires a certificate chain
            X509Certificate[] chain = generateV3Certificate(keyPair);

            ks.setKeyEntry("MyTest", pk, "1234".toCharArray(), chain);

            ks.store(null);


            prn("HasMoreAlaised:", ks.aliases().hasMoreElements());
            prn("Aliases: ");
            prn(ks.aliases());
            prn("Provider", ks.getProvider());
            prn("Type", ks.getType());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static X509Certificate[] generateV3Certificate(KeyPair pair) throws InvalidKeyException, NoSuchProviderException, SignatureException, IOException {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();

        certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
        certGen.setIssuerDN(new X500Principal("CN=Test Certificate"));
        certGen.setNotBefore(new Date(System.currentTimeMillis() - 10000));
        certGen.setNotAfter(new Date(System.currentTimeMillis() + 10000));
        certGen.setSubjectDN(new X500Principal("CN=Test Certificate"));
        certGen.setPublicKey(pair.getPublic());
        certGen.setSignatureAlgorithm("SHA256WithRSA");

        certGen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(false));
        certGen.addExtension(X509Extensions.KeyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));
        certGen.addExtension(X509Extensions.ExtendedKeyUsage, true, new ExtendedKeyUsage(KeyPurposeId.id_kp_serverAuth));

        certGen.addExtension(X509Extensions.SubjectAlternativeName, false, new GeneralNames(new GeneralName(GeneralName.rfc822Name, "test@test.test")));

        X509Certificate[] chain = new X509Certificate[1];
        chain[0] = certGen.generateX509Certificate(pair.getPrivate(), "SunPKCS11-SoftHSM");

        return chain;
    }
}
