/**  
 * All rights Reserved, Designed By Jamesoft
 * @Title:  PooledThread.java   
 * @Package jame.lang; 
 * @author: Jame
 * @Email:  jamecloud@163.com  
 * @date:   2016-10-31 16:55:37  
 * @version V1.0 
 * @Copyright: 2017 Jamesoft All rights reserved 
 * 注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目
 * @Description:  类功能说明
 * 基于线程池模式的服务必须实现该接口
 * 特别申明：
 * 1、任务：特指消息、报文、信号等信息，不包含业务逻辑处理的信息对象
 * 2、线程：根据消息、报文等进行业务逻辑处理的代码
 * 3、若生产类无法按本接口改造，建议使用桥接模式实现本接口开发
 */

package jame.lang;

public interface PooledThread extends PooledObject{
    /**
     * 执行任务
     * @param obj 任务对象
     * @return 
     * @throws java.lang.Exception
     */
    public Object execute(Object obj) throws Exception ;
}
