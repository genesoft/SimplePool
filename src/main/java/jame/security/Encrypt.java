/**  
 * All rights Reserved, Designed By Jamesoft
 * @Title:  Encrypt.java   
 * @Package jame.security; 
 * @author: Jame
 * @Email:  jamecloud@163.com  
 * @date:   2017-02-16 09:29:35  
 * @version V1.0 
 * @Copyright: 2017 Jamesoft All rights reserved 
 * 注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目
 * @Description:  类功能说明
 * ┏━━━━━━━━━━━━━━━━┓
 * ┃加密和解密接口                 ┃
 * ┗━━━━━━━━━━━━━━━━┛
 */

package jame.security;

import jame.lang.PooledObject;
import java.io.File;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.SecretKey;

public interface Encrypt extends PooledObject{
    /**
     * 根据用户输入的字符串构造符合算法要求的密码
     * @param pwd 以用户输入为依据构建密码对象
     * @return 构建的密码对象
     * @throws java.lang.Exception
     */
    SecretKey createSecretKey(String pwd) throws Exception;
    /**
     * 生成KeyPair
     * @return 返回系统成成的密钥对
     * @throws java.lang.Exception
     */
    KeyPair createKeyPair() throws Exception;;
    /**
     * 将Base编码还原为privateKey
     * @param code 私钥经base64编码以后的文本
     * @return 私钥
     * @throws java.lang.Exception
     */
    PrivateKey getPrivateKey(String code) throws Exception;;
    /**
     * 将Base编码还原为publicKey
     * @param code base64编码后的公钥字符串
     * @return 
     * @throws java.lang.Exception
     */
    PublicKey getPublicKey(String code) throws Exception;;
    /**
     * 
     * @param key 将密钥code编码做base64编码
     * @return 
     */
    String encodeKey(Key key);//将密码对象编码为字符串
    /**
     * 获取公钥的模数
     * 
     * @param key 用以向其他用户传递密钥信息爱
     * @return 
     */
    String getModulus(PublicKey key);
    /**
     * 加密指定文本
     * @param key 用以加密的密钥
     * @param str 需要加密的文本
     * @return 
     * @throws java.lang.Exception
     */
    String encrypt(Key key,String str) throws Exception;
    /**
     * 加密指定文件
     * @param key 用以加密的密钥
     * @param file 需要加密的文件
     * @return 
     * @throws java.lang.Exception
     */
    File encrypt(Key key,File file) throws Exception;
    /**
     * 解密指定文本
     * @param key 用以加密的密钥
     * @param str 需要解密的字符串
     * @return 加密以后的密文
     * @throws java.lang.Exception
     */
    String decrypt(Key key,String str) throws Exception;
    /**
     * 解密指定文件
     * @param key 用以解密的密钥
     * @param file 需要解密的文件
     * @return 解密以后的文件
     * @throws java.lang.Exception
     */
    File decrypt(Key key,File file) throws Exception;
}
