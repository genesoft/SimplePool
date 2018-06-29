/**  
 * All rights Reserved, Designed By Jamesoft
 * @Title:  EncryptFactory.java   
 * @Package jame.security; 
 * @author: Jame
 * @Email:  jamecloud@163.com  
 * @date:   2016-11-02 10:24:51  
 * @version V1.0 
 * @Copyright: 2017 Jamesoft All rights reserved 
 * 注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目
 * @Description:  类功能说明
 */

package jame.security;

import jame.lang.PooledObjectFactory;

public class EncryptFactory extends PooledObjectFactory {
    final private String athm;
    /**
     * 静态工厂方法
     * 创建指定算法的加密方法
     * @param athm
     * @return 
     */
    public static EncryptFactory getInstance(String athm){
        return new EncryptFactory(athm);
    }
    /**
     * 返回工厂名
     * @return 
     */
    @Override
    public String getName(){
        return EncryptFactory.class.getCanonicalName()+"_"+athm;
    }
    /**
     * 工厂方法
     */
    private EncryptFactory(String athm){
        this.athm = athm;
    }
    
    @Override
    public Encrypt create() throws Exception {
        return Encryptor.getInstance(athm);
    }

    @Override
    public void close(Object obj) {
        
    }

    @Override
    public boolean isEnable(Object obj) {
        return true;
    }
    
}
