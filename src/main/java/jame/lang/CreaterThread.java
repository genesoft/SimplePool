/**  
 * All rights Reserved, Designed By Jamesoft
 * @Title:  CreaterThread.java   
 * @Package jame.lang; 
 * @author: Jame
 * @Email:  jamecloud@163.com  
 * @date:   2016-10-24 15:46:29  
 * @version V1.0 
 * @Copyright: 2017 Jamesoft All rights reserved 
 * 注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目
 * @Description:  类功能说明
 * 本工具为对象池调用 主要用以创建对象
 * 以该类构建线程池：主要目的用以维护线程池的状态
 * 主要的实现办法：调用Pool的create方法以创建足够的对象来满足用户的需求
 * 而关闭方法在PooleadObject包装中实现，对象每次归还时自动判断是否可以关闭
 * 如果可以关闭对象池则自动关闭对象
 */

package jame.lang;

final class CreaterThread implements PooledThread{
    /**
     * 线程池维护线程，用以在需要的时候调用线程池或对象池的创建方法，保证对象池有足够的对象用以提供服务
     */
    @Override
    public Object execute(Object param){
        ((Pool)param).create();
        return null;
    }

    @Override
    public void close() throws Exception {
        
    }

    @Override
    public boolean isEnable() {
        return true;
    }

}
