package engsec;

import engsec.cmds.CopyCert;
import engsec.cmds.CopyKey;
import engsec.cmds.PKCS11Test;
import io.airlift.airline.Cli;
import io.airlift.airline.Help;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import static engsec.IO.prn;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, InvalidKeyException, NoSuchProviderException, SignatureException {

        Cli.CliBuilder<Runnable> builder = Cli.<Runnable>builder("ks-util")
                .withDescription("the stupid content tracker")
                .withDefaultCommand(Help.class)
                .withCommands(Help.class, CopyKey.class, CopyCert.class, PKCS11Test.class);

        Cli<Runnable> cli = builder.build();

        cli.parse(args).run();
    }

}
