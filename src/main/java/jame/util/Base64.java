/**  
 * All rights Reserved, Designed By Jamesoft
 * @Title:  Base64.java   
 * @Package jame.tools; 
 * @author: Jame
 * @Email:  jamecloud@163.com  
 * @date:   2016-10-28 14:28:47  
 * @version V1.0 
 * @Copyright: 2017 Jamesoft All rights reserved 
 * 注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目
 * @Description:  类功能说明
 * ┏━━━━━━━━━━━━━━━━┓
 * ┃自定义的Base64开发工具         ┃
 * ┗━━━━━━━━━━━━━━━━┛
 */

package jame.util;

import java.io.UnsupportedEncodingException;


public class Base64 implements Codec{
    String codes = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    /**
     * 编码输入的指定字节
     * @param bts
     * @return 
     */
    @Override
    public String encode(byte bts[]){
        byte src[] = bts;
        int paddings = 3 - bts.length % 3;//长度必须是3的整数倍，否则补充0
        if(paddings!=3){
            src = new byte[bts.length + paddings];
            System.arraycopy(bts, 0, src, 0, bts.length);
        }
        StringBuilder sb = new StringBuilder();
        int bt1,bt2,bt3,b1,b2,b3,b4;
        for(int i = 0;i<src.length;){
            bt1=src[i++]&0xff;//处理负数
            bt2=src[i++]&0xff;//处理负数
            bt3=src[i++]&0xff;//处理负数
            b1 = bt1 >>> 2; //取bt1的前6位
            b2 = ((bt1&0x03)<<4) | (bt2>>>4);//取bt1后2位 + bt2 的前4位  11 = 0x03
            b3 = ((bt2&0x0f)<<2) | (bt3>>>6);//取bt2的后4位 和 bt3 的前2位  1111=0x0f
            b4 = bt3&0x3f;//取bt3的后六位  11 1111 = 0x3f
            sb.append(codes.charAt(b1)).append(codes.charAt(b2)).append(codes.charAt(b3)).append(codes.charAt(b4));
        }
        if(paddings!=3)sb.delete(sb.length()-paddings, sb.length());//删除补位造成的错误码
        if(paddings!=3)while(paddings>0){//添加补位数
            sb.append("=");
            paddings--;
        }
        return sb.toString();
    }
    /**
     * 将4个base64编码转换为3个byte
     */
    @Override
    public byte[] decode(String cds){
//        if(cds.length()%4!=0)throw new Exception("编码长度不对。");
        int paddings = 0;
        if(cds.endsWith("="))paddings=1;
        if(cds.endsWith("=="))paddings=2;
        int i1,i2,i3,i4,bt1,bt2,bt3;
        byte bts[] = new byte[cds.length()*3/4];
        for(int i=0;i<cds.length()/4;i++){
            i1 = codes.indexOf(cds.charAt(i*4));//都是正数
            i2 = codes.indexOf(cds.charAt(i*4+1));//都是正数
            i3 = codes.indexOf(cds.charAt(i*4+2));//都是正数
            i4 = codes.indexOf(cds.charAt(i*4+3));//都是正数
            i1 = i1>0?i1:0;//处理'='
            i2 = i2>0?i2:0;//处理'='
            i3 = i3>0?i3:0;//处理'='
            i4 = i4>0?i4:0;//处理'='
            bt1 = (i1<<2) | (i2>>>4);//第一个的的全部，第2个的前两位
            bt2 = ((i2&0x0f)<<4) | (i3>>>2);//第一个的后四位 第三个的前四位
            bt3 = ((i3&0x03)<<6) | i4;//点三个的后两位 和
            bts[i*3]=(byte)bt1;
            bts[i*3+1]=(byte)bt2;
            bts[i*3+2]=(byte)bt3;
        }
        byte rt[] = new byte[bts.length-paddings];//处理'='
        System.arraycopy(bts, 0, rt, 0, rt.length);
        return rt;
    }
    

    @Override
    public void close() throws Exception {
        
    }
    

    @Override
    public boolean isEnable() {
        return true;
    }

    
    public static void main(String args[]) throws UnsupportedEncodingException, Exception{
        Base64 j64 = new Base64();
        System.out.println(j64.encode("abcd123123".getBytes("utf-8")));
        System.out.println(new String(j64.decode("5L2g5aW95ZWK5rWL6K+VMTIzMTIz"),"utf-8"));
    }

    
}
