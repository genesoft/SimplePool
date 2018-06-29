/**  
 * All rights Reserved, Designed By Jamesoft
 * @Title:  Codec.java   
 * @Package jame.tools; 
 * @author: Jame
 * @Email:  jamecloud@163.com  
 * @date:   2016-10-28 14:28:47  
 * @version V1.0 
 * @Copyright: 2017 Jamesoft All rights reserved 
 * 注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目
 * @Description:  类功能说明
 * 编码/解码接口
 */
package jame.util;

import jame.lang.PooledObject;

public interface Codec extends PooledObject{
    /**
     * 将字节编码
     */
    public String encode(byte bts[]);
    /**
     * 将编码解码
     */
    public byte[]  decode(String str);
    
}
