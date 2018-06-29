/**  
 * All rights Reserved, Designed By Jamesoft
 * @Title:  SecurityManager.java   
 * @Package jame.security; 
 * @author: Jame
 * @Email:  jamecloud@163.com  
 * @date:   2017-03-03 09:49:46  
 * @version V1.0 
 * @Copyright: 2017 Jamesoft All rights reserved 
 * 注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目
 * @Description:  类功能说明
 * ┏━━━━━━━━━━━━━━━━┓
 * ┃加密算法属性分析和查询         ┃
 * ┗━━━━━━━━━━━━━━━━┛
 *  如何获取密码的长度范围？
 */

package jame.security;

import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

final public class SecurityManager {
    final private static LinkedList<String> mds = new LinkedList<String>(),//系统所有的消息摘要算法
                                             sns = new LinkedList<String>(),//系统所有的签名算法
                                             cps = new LinkedList<String>(),//系统所有的加密算法(包括对称和不对称)
                                             pds = new LinkedList<String>();//所有的服务提供者
    final private static HashMap<String,String> aps = new HashMap<String,String>();//服务提供者和其支持的算法
    final private static HashMap<String,Integer> als = new HashMap<String,Integer>(),
                                                 ils = new HashMap<String,Integer>();//密码算法和其长度
    final private static HashMap<String,LinkedList<String>> modes = new HashMap<String,LinkedList<String>>(),//算法支持的填充模式
                                                            padds = new HashMap<String,LinkedList<String>>();//算法支持的填充方式
    static{analyse();}//初始化，获取系统所有的算法及其相关属性
    /**
     * 算法名字转换为大写，忽略用户大小写带来的差异
     */
    public static void analyse(){
       Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());//添加指定的整数加密算法类
       Provider providors[] = Security.getProviders();//获取系统支持所有安全算法提供者
       Iterator<Map.Entry<Object, Object>> it;
       Map.Entry<Object, Object> ent;
       String name,key,value,athm = null;
       LinkedList<String> list;
       for(Provider prd:providors){
           name = prd.getName();
           pds.add(name);//添加服务提供者
           it = prd.entrySet().iterator();
           while(it.hasNext()){
               ent = it.next();
               key = ent.getKey().toString();
               value = ent.getValue().toString();
               //System.out.println(ent);
               if(key.startsWith("Signature.")&&!key.contains(" ")&&!key.contains("/")){//签名算法
                   athm = key.substring(key.indexOf(".")+1);
                   if(!sns.contains(athm)){
                        sns.add(athm);
                        aps.put(athm, name);//保存每种算法与服务提供者的关联关系
                   }
               }
               if(key.startsWith("MessageDigest.")&&!key.contains(" ")&&!key.contains("/")){//摘要算法
                   athm = key.substring(key.indexOf(".")+1);
                   if(!mds.contains(athm)){
                        mds.add(athm);
                        aps.put(athm, name);//保存每种算法与服务提供者的关联关系
                   }
               }
               if(key.startsWith("Cipher.")&&!key.contains(" ")&&!key.contains("/")){//加密算法
                   athm = key.substring(key.indexOf(".")+1);
                   if(!athm.startsWith("PBEWith")&&!athm.endsWith("Wrap")&&!cps.contains(athm)){//暂时禁用PE和Wrap加密模式
                        cps.add(athm);
                        aps.put(athm, name);//保存每种算法与服务提供者的关联关系
                   }
               }
               if(key.endsWith("SupportedPaddings")){//支持的填充方式
                   athm = key.substring(key.indexOf(".")+1,key.lastIndexOf(" "));
                   list = padds.get(athm);
                   if(list==null){
                       list = new LinkedList<String>();
                       list.addAll(Arrays.asList(value.split("\\|")));
                       padds.put(athm, list);
                   }
               }
               if(key.endsWith("SupportedModes")){//支持的模式
                   athm = key.substring(key.indexOf(".")+1,key.lastIndexOf(" "));
                   list = modes.get(athm);
                   if(list==null){
                       list = new LinkedList<String>();
                       list.addAll(Arrays.asList(value.split("\\|")));
                       modes.put(athm, list);
                   }
               }
           }
       }
       //获取标准密码长度（使用密码生成类为每个算法生成一个密码，获取其长度作为标准密码长度）
       LinkedList<String> errors = new LinkedList<>();
       Iterator<String> cs = cps.iterator();
       KeyGenerator kg;
       Cipher cipher;
       SecretKey k;
       byte ivs[];
       String mode,padding;
       while(cs.hasNext())try{
           athm = cs.next();
           kg = KeyGenerator.getInstance(athm, getProvider(athm));//对称加密算法
           k = kg.generateKey();
           als.put(athm, k.getEncoded().length);//保存密码长度
           if(SecurityManager.needPadding(athm)){
                mode = SecurityManager.getDefaultMode(athm);
                padding = SecurityManager.getDefualtPadding(athm);
                cipher = Cipher.getInstance(athm+"/"+mode+"/"+padding,  getProvider(athm));
           }else cipher = Cipher.getInstance(athm,  getProvider(athm));
           cipher.init(Cipher.ENCRYPT_MODE, k);
           ivs = cipher.getIV();
           if(ivs!=null)ils.put(athm, ivs.length);
       }catch(Exception e){
           //报错则表示系统不支持这种算法用以对称加密：非堆成加密算法
           errors.add(athm);
       }
       //删除错误算法
       cps.removeAll(errors);        
    }
    /**
     * 获取算法的服务提供者
     */
    public static Provider getProvider(String athm) throws NoSuchAlgorithmException{
        if(!aps.containsKey(athm))throw new NoSuchAlgorithmException("当前系统不支持指定的算法'"+athm+"'");
        return Security.getProvider(aps.get(athm));
    }
    /**
     * 获取所有的支持的加密算法
     */
    public static LinkedList<String> getCipherAlgorithms(){
        return cps;
    }
    /**
     * 获取所有的消息摘要算法
     */
    public static LinkedList<String> getDigestAlgorithms(){
        return mds;
    }
    /**
     * 获取所有的签名算法
     */
    public static LinkedList<String> getSignatureAlgorithms(){
        return sns;
    }
    /**
     * 获取加密算法支持的填充模式
     */
    public static LinkedList<String> getModes(String athm){
        return modes.get(athm);
    }
    /**
     * 获取算法支持的填充方式
     */
    public static LinkedList<String> getPaddings(String athm){
        return padds.get(athm);
    }
    /**
     * 判断是否需要填充
     */
    public static boolean needPadding(String athm){
        return padds.containsKey(athm);
    }
    /**
     * 判断加密函数是否需要向量
     */
    public static boolean needIvParameter(String athm){
        return ils.containsKey(athm);
    }
    /**
     * 获取加密函数支持的向量长度
     */
    public static int getIvParameterLength(String athm){
        if(ils.containsKey(athm))return ils.get(athm);
        else return 0;
    }
    /**
     * 获取默认的填充算法
     * 如果存在 ISO10126PADDING 则使用 ISO10126PADDING
     * 如果存在 PKCS5PADDING 则使用 PKCS5PADDING
     * 否则则使用排序第一个填充方式
     */
    public static String getDefualtPadding(String athm){
        LinkedList<String> ps = padds.get(athm);
        if(ps==null)return null;
        if(ps.contains("ISO10126PADDING"))return "ISO10126PADDING";
        if(ps.contains("PKCS5PADDING"))return "PKCS5PADDING";
        else return ps.get(0);
    }
    /**
     * 获取默认的填充模式
     * 如果算法支持CBC则优先使用CBC
     * 如果算法支持ECB则优先使用ECB
     */
    public static String getDefaultMode(String athm){
        LinkedList<String> ms = modes.get(athm);
        if(ms==null)return null;
        if(ms.contains("CBC"))return "CBC";
        if(ms.contains("ECB"))return "ECB";
        else return ms.get(0);
    }
    /**
     * 获取指定算法的密码长度
     */
    public static Integer getPasswordLength(String athm){
        if(als.containsKey(athm))return als.get(athm);
        else return 0;
    }
    
    
    public static void main(String args[]) throws NoSuchAlgorithmException{
        System.out.println("加密驱动："+aps);
        System.out.println("加密算法："+cps);
        System.out.println("摘要算法："+mds);
        System.out.println("签名算法："+sns);
        System.out.println("密码长度："+als);
        System.out.println("填充模式："+modes);
        System.out.println("填充算法："+padds);
        System.out.println("向量长度："+ils);
    }
}
