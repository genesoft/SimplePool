/**  
 * All rights Reserved, Designed By Jamesoft
 * @Title:  Encryptor.java   
 * @Package jame.security; 
 * @author: Jame
 * @Email:  jamecloud@163.com  
 * @date:   2017-06-09 16:19:38  
 * @version V1.0 
 * @Copyright: 2017 Jamesoft All rights reserved 
 * 注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目
 * @Description:  类功能说明
 * 加密解密工具包
 * 支持对称和非堆成加密算法
 * 区别在于：构造方法传入的算法不同和加密解密时传入的Key类型不同
 * 当前非对称加密暂只支持RSA算法，加密使用公钥，解密使用私钥
 */

package jame.security;

import jame.util.Base16;
import jame.util.Base64;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

final public class Encryptor implements Encrypt{
    private String algorithm = "";
    private String mode = "";
    private String padding = "";
    private final int pwdLength,ivLength;
    private final Cipher cipher;
    private final Base64 base64 = new Base64();
    private final Base16 base16 = new Base16();
    /**
     * 创建指定算法的加密器
     */
    private Encryptor(String athm) throws Exception{
        if(athm.contains("/")){
            String tmp[]=athm.split("\\/");
            this.algorithm = tmp[0];
            this.mode = tmp[1];
            this.padding = tmp[2];
         }else if(SecurityManager.needPadding(athm)){
            this.algorithm = athm;
            this.mode = SecurityManager.getDefaultMode(athm);
            this.padding = SecurityManager.getDefualtPadding(athm);
        }else this.algorithm = athm;
        pwdLength = SecurityManager.getPasswordLength(this.algorithm);//获取编码长度
        ivLength = SecurityManager.getIvParameterLength(this.algorithm);//获取密码向量的长度
        //System.out.println(this.algorithm+"/"+this.mode+"/"+this.padding);
        if(this.padding!=null)cipher = Cipher.getInstance(this.algorithm+"/"+this.mode+"/"+this.padding,SecurityManager.getProvider(this.algorithm));//指定算法的服务提供者ivLength
        else cipher = Cipher.getInstance(this.algorithm,SecurityManager.getProvider(this.algorithm));
    }
    /**
     * 创建指定算法和驱动的加密器
     */
    private Encryptor(String athm,Provider prd) throws Exception{
        if(athm.contains("/")){
            String tmp[]=athm.split("\\/");
            this.algorithm = tmp[0];
            this.mode = tmp[1];
            this.padding = tmp[2];
         }else if(SecurityManager.needPadding(athm)){
            this.algorithm = athm;
            this.mode = SecurityManager.getDefaultMode(athm);
            this.padding = SecurityManager.getDefualtPadding(athm);
        }else this.algorithm = athm;
        pwdLength = SecurityManager.getPasswordLength(this.algorithm);//获取编码长度
        ivLength = SecurityManager.getIvParameterLength(this.algorithm);//获取密码向量的长度
        //System.out.println(this.algorithm+"/"+this.mode+"/"+this.padding);
        if(this.padding!=null)cipher = Cipher.getInstance(this.algorithm+"/"+this.mode+"/"+this.padding,prd);//指定算法的服务提供者ivLength
        else cipher = Cipher.getInstance(this.algorithm,prd);
    }
    /**
     * 创建指定算法的加密器
     * @param Algorithm 加密算法
     * @return 
     * @throws java.lang.Exception
     */
    public static Encryptor getInstance(String Algorithm) throws Exception{ 
        return new Encryptor(Algorithm);
    }
    /**
     * 依据驱动创建指定算法的加密器
     * @param Algorithm
     * @param prd
     * @return 
     * @throws java.lang.Exception
     */
    public  static Encryptor getInstance(String Algorithm,Provider prd) throws Exception{ 
        return new Encryptor(Algorithm,prd);
    }
    /**
     * 根据用户输入创建符合算法要求的密码
     */
    @Override
    public SecretKey createSecretKey(String key) throws Exception{
        if(pwdLength==0)return null;
        while(key.getBytes("utf-8").length<pwdLength)key += "0";//不足部分用"0"补齐
        if(key.getBytes("utf-8").length>pwdLength)key = key.substring(0, pwdLength);//多余部分切断
        return new SecretKeySpec(key.getBytes("utf-8"),this.algorithm);
    }
    /**
     * 创建密钥对
     */
    @Override
    public KeyPair createKeyPair() throws Exception{
        KeyPairGenerator kpg =KeyPairGenerator.getInstance("RSA",new org.bouncycastle.jce.provider.BouncyCastleProvider());
        kpg.initialize(1024);//使用1024字节长度密码
        return kpg.generateKeyPair();
    }
    /**
     * 根据输入的base64编码恢复私钥
     * @param code 经base64编码的私钥code数据
     */
    @Override
    public PrivateKey getPrivateKey(String code) throws Exception{
        KeyFactory kf = KeyFactory.getInstance("RSA",new org.bouncycastle.jce.provider.BouncyCastleProvider());
        KeySpec ks = new PKCS8EncodedKeySpec(base64.decode(code));
        return kf.generatePrivate(ks);
    }
    /**
     * 根据输入的base64编码恢复公钥
     * @param code 经base64编码的公钥code数据
     */
    @Override
    public PublicKey getPublicKey(String code) throws Exception{
        KeyFactory kf = KeyFactory.getInstance("RSA",new org.bouncycastle.jce.provider.BouncyCastleProvider());
        KeySpec ks = new X509EncodedKeySpec(base64.decode(code));
        return kf.generatePublic(ks);
    } 
    /**
     * 获取公钥的模数，用以传输公钥
     * @param key  公钥
     */
    @Override
    public String getModulus(PublicKey key){
        if(key==null)return "";
        RSAPublicKey rk = (RSAPublicKey)key;
        String rt = rk.getModulus().toString(16);
        rt = base64.encode(base16.decode(rt));
        return rt;
    }
    /**
     * 获取密钥的base64编码，用以存储或传输
     */
    @Override
    public String encodeKey(Key key){
        return base64.encode(key.getEncoded());
    }
    /**
     * 用指定密码加密信息
     * 加密以后的内容做base64编码
     * @param desc 需要加密的文本
     */
    @Override
    synchronized public String encrypt(Key key,String desc) throws Exception{
        cipher.init(Cipher.ENCRYPT_MODE,key,getIvParameter(key.getEncoded()));
        return base64.encode(cipher.doFinal(desc.getBytes("utf-8")));//加密以后的结果用base64编码
    }
    /**
     * 生成指定密码的加密向量
     */
    private IvParameterSpec getIvParameter(byte bts[]){
       if(ivLength==0)return null;
       byte bs[] = new byte[ivLength]; 
       System.arraycopy(bts, 0, bs, 0, ivLength);
       return new IvParameterSpec(bs);
    }
    /**
     * 用指定密码加密文件
     * @param file 需要加密的文件
     */
    @Override
    synchronized public File encrypt(Key key,File file) throws Exception{
        cipher.init(Cipher.ENCRYPT_MODE,key,getIvParameter(key.getEncoded()));
        File tmp = File.createTempFile(file.getName(),"enc");
        ByteBuffer bf1 = ByteBuffer.allocate(8192),bf2 = ByteBuffer.allocate(8192);
        FileChannel fic = new FileInputStream(file).getChannel();
        FileChannel foc = new FileOutputStream(tmp).getChannel();
        int read;
        while (true){
            bf1.clear();
            read = fic.read(bf1);
            if (read < 0)break;
            cipher.doFinal(bf1, bf2);
            foc.write(bf2);
        }
        foc.close();
        fic.close();
        return tmp;
    }
    /**
     * 使用字段密码解密信息
     * @param desc 解密指定文本
     */
    @Override
    synchronized public String decrypt(Key key,String desc) throws Exception {
        cipher.init(Cipher.DECRYPT_MODE,key,getIvParameter(key.getEncoded()));
        return new String(cipher.doFinal(base64.decode(desc)),"utf-8");//加密以后的结果用base64编码
    }
    /**
     * 用指定密码加密文件
     */
    @Override
    synchronized public File decrypt(Key key,File file) throws Exception{
        cipher.init(Cipher.DECRYPT_MODE,key,getIvParameter(key.getEncoded()));
        File tmp = File.createTempFile(file.getName(),"enc");
        ByteBuffer bf1 = ByteBuffer.allocate(8192),bf2 = ByteBuffer.allocate(8192);
        FileChannel fic = new FileInputStream(file).getChannel();
        FileChannel foc = new FileOutputStream(tmp).getChannel();
        int read;
        while (true){
            bf1.clear();
            read = fic.read(bf1);
            if (read < 0)break;
            cipher.doFinal(bf1, bf2);
            foc.write(bf2);
        }
        foc.close();
        fic.close();
        return tmp;
    }

    @Override
    public void close() throws Exception {
        //断开与第三方连接，本类不需要
    }
    /**
     * 判断与第三方的连接是否断开
     */
    @Override
    public boolean isEnable() {
        return false;
    }

    static public void main(String args[]) throws Exception{
        Encryptor en;
        String pwd = "WSWhuhKu7qgjhSsU";
        String msg = "123123123123123123123";
        Key key;
        System.out.println("明文："+msg);
//        System.out.println(SecuritManager.getCipherAlgorithms());
        for(String athm:SecurityManager.getCipherAlgorithms()){
//        String athm ="ARCFOUR";//
            try{
                System.out.println("加密方法："+athm);
                en = Encryptor.getInstance(athm);
                key = en.createSecretKey(pwd);
                msg =en.encrypt(key,msg);
                System.out.println("密文："+msg);
                System.out.println("明文："+en.decrypt(key,msg ));
            }catch(Exception e){
                System.out.println("报错算法："+athm);
            }
       }
    }

}
