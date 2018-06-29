/**  
 * All rights Reserved, Designed By Jamesoft
 * @Title:  Base16.java   
 * @Package jame.tools; 
 * @author: Jame
 * @Email:  jamecloud@163.com  
 * @date:   2017-03-03 11:39:43  
 * @version V1.0 
 * @Copyright: 2017 Jamesoft All rights reserved 
 * 注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目
 * @Description:  类功能说明
 * 16进制编码解码工具
 * 由于每个Byte要编码成2位16进制字符，所以16进制编码后长度翻倍
 */

package jame.util;

public class Base16 implements Codec{
    /**
     * 每个Byte 8 bit 转换为 hex应该为2位16进制数
     * @param bytes 需要编码的2进制数组
     * @return 16进制编码字符串
     */
    @Override
    public String encode(byte bytes[]){
        StringBuilder rt = new StringBuilder();
        String tmp;
        for(byte bt:bytes){
            tmp = Integer.toHexString(bt&0xff);
            if(tmp.length()<2)tmp = "0"+tmp;
            rt.append(tmp);
        }
        return rt.toString();
    }
    /**
     * 
     * @param str 16进制编码字符串
     * @return 解码以后的2进制字符串
     */
    @Override
    public byte[] decode(String str){
        byte[] rt = new byte[str.length()/2];
        byte bt;
        for(int i=0;i<rt.length;i++){
            bt = (byte)(Integer.parseInt(str.substring(i*2, i*2+2), 16));
            rt[i] = bt;
        }
        return rt;
    }

    @Override
    public void close() throws Exception {
        
    }

    @Override
    public boolean isEnable() {
        return true;
    }
    
    
    public static void main(String args[]) throws Exception{
        Base16 base16 = new Base16();
        String str = base16.encode("你好啊".getBytes());
        System.out.println(str);
        System.out.println(new String(base16.decode(str)));
    }
}
