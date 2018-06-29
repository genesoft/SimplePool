/**  
 * All rights Reserved, Designed By Jamesoft
 * @Title:  PooledObjectProxy.java   
 * @Package jame.lang; 
 * @author: Jame
 * @Email:  jamecloud@163.com  
 * @date:   2016-10-24 16:20:15  
 * @version V1.0 
 * @Copyright: 2017 Jamesoft All rights reserved 
 * 注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目
 * @Description:  类功能说明
 * 池化对象代理类
*/

package jame.lang;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

final public class PooledObjectProxy implements InvocationHandler{
    private final Pool pool;
    private Object proxy;
    private final Object object;//原型被代理的对象
    private final PooledObjectFactory factory;//对象的工厂类
    private final Class faces[];//所有需要代理的接口（若无接口，将没有任何方法被代理）
    private long birthTime,//创建时间
                 hireTime,//出借时间
                 maxHireTime = 0,//最长出借时间
                 maxLiveTime = 0;//最长生存时间
    private boolean isHired = false;//是否被出借
    /**
     * 原始构造方法
     */
    public PooledObjectProxy(Pool pool,Object obj,PooledObjectFactory factory,Class faces[]){
        this.pool = pool;
        this.object = obj;//保存原型
        this.factory = factory;
        this.faces = faces;
        birthTime = System.currentTimeMillis();
    }
    /**
     * 设置对象的最大生存时间
     * @param seconds 对象的最长生存时间
     */
    public void setMaxLiveTime(int seconds){
        this.maxLiveTime = seconds*1000;
    }
    /**
     * 返回对象的最长生命周期
     * @return 返回对象的最长生命设置,单位:秒
     */
    public long getMaxLiveTime(){
        return this.maxLiveTime / 1000;
    }
    /**
     * 返回对象的生存时间
     * @return 返回对象的生存时间,单位:秒
     */
    public long getLiveTime(){
        return (System.currentTimeMillis() - birthTime)/1000;
    }
    /**
     * 设置对象状态为出借
     * @param isHired 设置对象出状态
     */
    public void setHired(boolean isHired){
        this.isHired = isHired;
        if(isHired)hireTime = System.currentTimeMillis();
    }
    /**
     * 返回对象的最长出借时间
     * @param seconds 最长出借时间，单位：秒
     */
    public void setMaxHireTime(long seconds){
        this.maxHireTime = seconds*1000;
    }
    /**
     * 返回对象的最长出借时间
     * @return 对象的最长出借时间
     */
    public long getMaxHireTime(){
        return this.maxHireTime;
    }
    /**
     * 返回对象已经被出借的时间
     * @return 对象已经被出借的时间，单位：秒
     */
    public long getHireTime(){
        return (System.currentTimeMillis() - hireTime);
    }
    /**
     * 检查对象是否可用
     * 如果设置有最长生存时间，则生存时间不超过最长生存时间
     * 如果设置有最长出借时间，如果对象已经被出借，则对象被出借的时间不超过最长出借时间
     * @return 对象是否可用
     */
    public boolean isEnable(){
        boolean rt;
        if(this.factory!=null)rt = this.factory.isEnable(object);
        else rt = ((PooledObject)object).isEnable();
        if(!rt)return rt;
        rt = (maxLiveTime == 0)||(maxLiveTime != 0 && (System.currentTimeMillis() - birthTime > maxLiveTime));
        if(!rt)return rt;
        if(isHired)rt = ((maxHireTime == 0)||(maxHireTime != 0 && (System.currentTimeMillis() - hireTime <= maxHireTime)));
        return rt;
    }
    /**
     * 关闭原型对象
     */
    public void close(){
        if(this.factory!=null)this.factory.close(object);
        else try{((PooledObject)object).close();}catch(Exception e){}
    }
    /**
     * 返回被代理的对象，供用户使用
     * 代理存在的主要作用：
     * 1、调用close时返回对象，而不是真正的关闭对象
     * 2、调用对象方法时，判断对象是否可用，对于超期的操作全部报错
     * @return 返回代理对象
     */
    public Object getObject(){
        if(proxy==null){
            proxy = Proxy.newProxyInstance(
                    object.getClass().getClassLoader(),//类加载器
                    faces,this);//指定代理器
        }
        return proxy;
    }
    /**
     * 调用被代理对象的方法
     * 这里代理对象的核心方法，由该方法实现对象的归池
     * 这里可以添加一些列其他操作，譬如日志等，可以完整的记录对象的所有操作，但会影响性能，是否扩展由用户自己决定
     * 归还对象:不管对象是否基于AutoCloseable接口开发，池均通过close或back方法判定归还对象
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(!isEnable())throw new Exception("Object is unuseable!");//如果超期则报错
        //归还对象:不管对象是否基于AutoCloseable接口开发，池均通过close或back方法判定归还对象
        if(method.getName().equalsIgnoreCase("close")||
                method.getName().equalsIgnoreCase("back")){
            if(pool instanceof ObjectPool)((ObjectPool)pool).returnOject(this);
            return null;
        }
        //执行目标任务
        Object rt = null;
        try{
            rt = method.invoke(object, args);
        }catch(InvocationTargetException e){
            throw e.getTargetException();//如果报错 则抛出目标错误
        }
        return rt;
    }
}
