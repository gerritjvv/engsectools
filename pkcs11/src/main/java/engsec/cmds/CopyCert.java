package engsec.cmds;

import engsec.KeyStoreUtil;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

import java.security.KeyStore;

@Command(name = "copy-cert", description = "Copy a certificate from one KeyStore to another")
public class CopyCert implements Runnable{

    @Option(name = "-src", description = "Source KeyStore, can be null if type is PKCS11")
    public String srcPath;

    @Option(name = "-src-pwd", description = "Source KeyStore password", required = true)
    public String srcPwd;


    @Option(name = "-dest", description = "Destination KeyStore, can be null if type is PKCS11")
    public String destPath;

    @Option(name = "-dest-pwd", description = "Destination KeyStore password", required = true)
    public String destPwd;



    @Option(name = "-src-alias", description = "Source alias of the key to copy", required = true)
    public String srcAlias;

    @Option(name = "-dest-alias", description = "Destination alias to save the key as", required = true)
    public String destAlias;



    @Override
    public void run() {

        KeyStore srcKs = KeyStoreUtil.loadKeyStore(srcPath, srcPwd.toCharArray());
        KeyStore dstKs = KeyStoreUtil.loadKeyStore(destPath, destPwd.toCharArray());

        KeyStoreUtil.transferCert(srcKs, srcAlias, dstKs, destAlias);

        KeyStoreUtil.saveKeyStore(dstKs, destPath, destPwd.toCharArray());
    }
}
