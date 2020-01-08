package engsec.cmds;

import engsec.KeyStoreUtil;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

import java.security.KeyStore;

@Command(name = "copy", description = "Copy a key and its key chain from one KeyStore to another")
public class CopyKey implements Runnable{

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

    @Option(name = "-key-pwd", description = "Key password", required = true)
    public String keyPwd;



    @Override
    public void run() {

        KeyStore srcKs = KeyStoreUtil.loadKeyStore(srcPath, srcPwd.toCharArray());
        KeyStore dstKs = KeyStoreUtil.loadKeyStore(destPath, destPwd.toCharArray());

        KeyStoreUtil.transferKey(srcKs, srcAlias, dstKs, destAlias, keyPwd.toCharArray());

        KeyStoreUtil.saveKeyStore(dstKs, destPath, destPwd.toCharArray());
    }
}
