/**  
 * All rights Reserved, Designed By Jamesoft
 * @Title:  PooledObject.java   
 * @Package jame.lang; 
 * @author: Jame
 * @Email:  jamecloud@163.com  
 * @date:   2016-10-24 14:43:51  
 * @version V1.0 
 * @Copyright: 2017 Jamesoft All rights reserved 
 * 注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目
 * @Description:  类功能说明
 * 被对象池管理的对象实现接口
 * 对象池比线程池能对开发人员提供更丰富的服务接口
 * 开发要求：用户被池化管理的对象必须先申明一个继承于该接口的Interface
 */

package jame.lang;


public interface PooledObject extends AutoCloseable{
    /**
     * 必须声明关闭方法
     * 该方法实际被对象池拦截，在一般使用时只是将对象归还给对象池而已
     * 只有对象池决定关闭该对象时才会真正的调用该方法
     * @throws java.lang.Exception
     */
    @Override
    public void close() throws Exception;
    /**
     * 对象是否可用
     * @return 对象是否可用，是否已经被关闭等
     */
    public boolean isEnable();
}
