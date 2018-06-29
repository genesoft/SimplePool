/**  
 * All rights Reserved, Designed By Jamesoft
 * @Title:  ObjectPool.java   
 * @Package jame.lang; 
 * @author: Jame
 * @Email:  jamecloud@163.com  
 * @date:   2017-08-14 16:22:28  
 * @version V1.0 
 * @Copyright: 2017 Jamesoft All rights reserved 
 * 注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目
 * @Description:  类功能说明
 * 对象池
 * 被管理的对象必须遵守PooledObject接口
 * 被管理的对象必须有工厂方法来创建对象
 * 当需要获取对象时：无可用对象则创建新的对象
 * 问题：
 * 因为在平时系统并为轮循每个空闲对象的工作状态，
 * 将可能在空闲很长时间后，对象池中存放的对象全部是无效的
 * 这样，就需要一定的时间重新来清理无效对象和创建新对象，这次访问的时间效率会比较差？
 * 是否需要更改设计？
 */

package jame.lang;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

final public class ObjectPool extends Pool{
    //统一管理所有的对象池
    static final private HashMap<String,ObjectPool> pools = new HashMap<String,ObjectPool>();
    //类变量
    final private Class<? extends PooledObject> pooledClass;//被池化管理的对象类
    final private PooledObjectFactory factory;//对象池任务执行主体
    private Class faces[];//所有需要被代理的接口
    
    final private ConcurrentLinkedQueue<PooledObjectProxy> busy_objects =  new ConcurrentLinkedQueue<PooledObjectProxy>();//正在工作的对象
    final private ConcurrentLinkedQueue<PooledObjectProxy> free_objects = new ConcurrentLinkedQueue<PooledObjectProxy>();//正在等候工作的对象
    final private Object createLock = new Object();//对象创建锁：保证任何时刻只有一个线程创建对象

    /**
     * 获取指定名称的已经被创建的对象池
     * @param name 
     * @return 
     */
    public static ObjectPool getInstance(String name){
        return pools.get("ObjectPool_" + name);
    }
    /**
     * 以PooledClass为参数获取对应的对象池对象
     * @param cls 以对象类为原型创建对象池
     * @return 
     */
    public static ObjectPool getInstance(Class<? extends PooledObject>  cls){
        return getInstance(cls.getCanonicalName(),cls,null);
    }
    /**
     * 创建并返回指定名称的对象池
     * @param name 对象池名称
     * @param cls 对象的类
     * @return 创建的对象池实例
     */
    public static ObjectPool getInstance(String name,Class<? extends PooledObject> cls){
        return getInstance(name,cls,null);
    }
    /**
     * 根据指定对象池配置参数，创建指定名称的对象池
     * @param name 对象池名称
     * @param cls 对象类
     * @param poolConfig 对象池的配置参数
     * @return 对象池实例
     */
    public static ObjectPool getInstance(String name,Class<? extends PooledObject> cls,PoolConfig poolConfig){
        ObjectPool rt = pools.get("ObjectPool_" + name);
        if(rt==null)rt = new ObjectPool(name,cls,poolConfig);
        return rt;
    }
    /**
     * 以工厂类为参数获取对应的对象池对象
     * @param factory 对象工厂类
     * @return 
     */
    public static ObjectPool getInstance(PooledObjectFactory factory){
        return getInstance(factory.getClass().getCanonicalName(),factory);
    }
    /**
     * 以工厂类为参数获取对应的对象池对象
     * @param name 指定对象池的名称
     * @param factory 对象工厂类
     * @return 
     */
    public static ObjectPool getInstance(String name,PooledObjectFactory factory){
        return getInstance(name,factory,null);
    }
    /**
     * 以工厂类为参数获取对应的对象池对象
     * @param name 对象池名称
     * @param factory 对象工厂类
     * @param poolConfig 对象池配置参数
     * @return 
     */
    public static ObjectPool getInstance(String name,PooledObjectFactory factory,PoolConfig poolConfig){
        ObjectPool rt = pools.get("ObjectPool_" + name);
        if(rt==null&&factory!=null)rt = new ObjectPool(name,factory,poolConfig);
        return rt;
    }
    /**
     * 以默认参数配置方式创建对象池
     * @param cls 以该对象作为原型创建对象池
     */
    private ObjectPool(Class<? extends PooledObject> cls){
        this("ObjectPool_" + cls.getCanonicalName(),cls,null);
    }
    /**
     * 以cls为原型，创建指定名称的对象池
     */
    private ObjectPool(String name,Class<? extends PooledObject> cls){
        this(name,cls,null);
    }
    /**
     * 以cls为原型，创建指定名称的对象池
     */
    private ObjectPool(String name,Class<? extends PooledObject> cls,PoolConfig poolConfig){
        this.pooledClass = cls;
        this.factory = null;
        this.name ="ObjectPool_" + name;
        this.logger = Logger.getLogger(name);//创建默认日志对象
        pools.put(this.name, this);
        super.setConfig(poolConfig);
        this.create();//要求创建后立即可用
    }
    /**
     * 以factory方式创建对象池
     * @param factory 以该工厂来创建对象
     */
    private ObjectPool(PooledObjectFactory factory){
        this("ObjectPool_" + factory.getClass().getCanonicalName(),factory,null);
    }
    /**
     * 以factory方式创建指定名称的对象池
     * @param factory 以该工厂来创建对象
     */
    private ObjectPool(String name,PooledObjectFactory factory){
        this(name,factory,null);
    }
    /**
     * 以factory方式创建指定名称的对象池
     * @param factory 以该工厂来创建对象
     */
    private ObjectPool(String name,PooledObjectFactory factory,PoolConfig poolConfig){
        this.pooledClass = null;
        this.factory = factory;
        this.name = "ObjectPool_" + name;
        this.logger = Logger.getLogger(name);//创建默认日志对象
        pools.put(name, this);
        super.setConfig(poolConfig);
        this.create();//要求创建后立即可用
    }
    /**
     * 获取池中当前的繁忙对象数量
     * @return 工作对象数量
     */
    @Override
    public int getBusyCount(){
        return busy_objects.size();
    }
    /**
     * 查看当前的空闲对象
     * 获取池中当前的空闲对象数量
     * @return 空闲对象数量
     */
    @Override
    public int getFreeCount(){
        return free_objects.size();
    }
    /**
     * 获取任务队列中正等候执行的任务数量
     * 对象池不使用该参数，默认返回0
     * @return 
     */
    @Override
    public int getTaskCount(){
        return 0;
    }
    
    /**
     * 创建更多的对象用以满足用户使用需求
     */
    @Override
    final public void create(){
        if(isFull()||isStopped||this.getFreeCount()>=this.maxFreeCount)return;//如果对象已经达到最大规模
//        log.info(this.getName()+":当前空闲对象数量："+this.getFreeCount()+" 最小空闲对象数量："+this.minFreeCount);
        synchronized(createLock){//一次只允许一个对象执行本程序块
            int count = 0;
            PooledObjectProxy obj;
            while(free_objects.size()<minFreeCount&&//空闲对象数量小于最小空闲对象数
                    maxCount>(free_objects.size()+busy_objects.size()))//总对象数小于系统最大总对象数
            try{
                obj = createObject();
                if(obj!=null)free_objects.add(obj);
                if(count++ >= 200 )break;//创建函数大于等于多少时则直接跳出
            }catch(Exception e){
                break;//有错误发生时，终止本次创建
            }
        }
    }
    /**
     * 创建被代理的对象
     */
    private PooledObjectProxy createObject() throws Exception{
        if(isFull())return null;//如果对象已经达到最大规模则直接返回
        if(this.getFreeCount()>=this.maxFreeCount)return null;
        Object obj = null;
        if(factory!=null){
            try{
                obj = factory.create();
            }catch(Exception e){
                logger.log(Level.SEVERE,"对象池'"+getName()+"'在创建对象时发生错误。",e);
                throw e;
            }
        }else{
            try{
                obj = pooledClass.newInstance();
            }catch(Exception e){
                logger.log(Level.SEVERE,"对象池'"+getName()+"在创建对象时发生错误。",e);
                throw e;
            }
        }
        if(obj!=null){
            if(faces==null)faces = getAllInterfaces(obj);
            return new PooledObjectProxy(this,obj,this.factory,this.faces);
        }
        else  return null;
    }
    /**
     * 获取对象的所有接口，用以创建对象的代理对象
     */
    private Class[] getAllInterfaces(Object obj){
        LinkedList<Class> fcs = new LinkedList();
        Class clss[] = obj.getClass().getInterfaces();
        for(Class cls:clss)fcs.add(cls);
        Class main = obj.getClass();//如果本类不是基于接口开发的
        while(true){//则获取本类所有超类的接口信息，所有接口全部代理
            main = main.getSuperclass();
            if(main.getCanonicalName().equals("java.lang.Object"))break;
            clss = main.getInterfaces();
            for(Class cls:clss)fcs.add(cls);
        }
        return fcs.toArray(new Class[]{});
    }
    /**
     * 判断是否需要关闭对象，若需要则关闭
     * 关闭原则：
     * （1）对象不可用
     * （2）池已关闭
     * （3）空闲对象数量大于最大空闲对象数量
     * @param obj 需要关闭的对象
     * @return 对象是否成功关闭
     */
    @Override
    final public boolean close(Object obj){
        PooledObjectProxy pop =  (PooledObjectProxy)obj;
        //先从空闲池删除对象
        boolean rt = !pop.isEnable()|| this.isStopped() || 
                      this.getFreeCount()>=this.getMaxFreeCount();
        if(rt){
            rt = free_objects.remove(pop);
            if(!rt)return false;
            //关闭对象
            pop.close();
            return true;
        }else return false;
    }
    /**
     * 租借对象
     * @param seconds 最长使用时间，单位：秒。超过这个时间后再调用这个对象的任何方法均会报错
     * @return 返回租借的对象
     * @throws java.lang.Exception
     */
    final public Object getObject(long seconds) throws Exception{//租借多少秒
//        this.printStatus();//打印对象池工作状态
        if(isStopped)throw new Exception("ObjectPool is closed");
        if(this.getFreeCount()<minFreeCount&&!isFull()&&!isStopped)//启动条件：空闲对象小于系统最小空闲对象数量，并且池未达到最大规模
            try{CreaterPool.addTask(this);}catch (Exception e){}
        //1、获取空闲对象
        PooledObjectProxy rt = free_objects.poll();
        //2、如果没有则创建对象(若对象池在规定时间类没有有用的对象则报错)
        if(rt==null&&!isFull())rt = createObject();//对象池没满则创建成功，若已满，则依然创建失败
        //3、返回对象
        if(rt!=null&&rt.isEnable()){
            rt.setMaxHireTime(seconds);//设置对象的最长租借时间：如果超时未归还再次调用该对象的方式时将报错
            rt.setHired(true);//设置对象租借状态
            busy_objects.add(rt);
            return rt.getObject();
        }else throw new Exception("ObjectPool is busy!");
    }
    /**
     * 归还对象
     * 本方法被PooledObject
     * @param object 归还对象
     */
    public void returnOject(PooledObjectProxy object){
        object.setHired(false);//设置租借转台
        busy_objects.remove(object);
        if(!this.close(object)&&!free_objects.contains(object))
            free_objects.add(object);//防止对象被多次归还
    }
     
    /**
     * 对象池工作状态巡检方法
     * 删除空闲对象列表中的不可用对象
     * 注意：若对象池繁忙，则不运行该方法
     */
    @Override
    public void check(){
        if(this.isFull())return;//如果对象池正繁忙，返回
        if(this.getPressure()>=20)return;//如果对象池工作压力大于20%返回
        for(PooledObjectProxy pop:this.free_objects){
            if(!pop.isEnable())this.free_objects.remove(pop);//如果对象不可用，则删除对象
            try{CreaterPool.addTask(this);}catch (Exception e){}//通知新建线程执行新建任务，防止删除过程中导致没有一个对象可用
        }
    }
    
    /**
     * 关闭对象池
     * @throws java.lang.Throwable
     */
    @Override
    public void finalize() throws Throwable{
        super.finalize();
        this.stop();
        //立即关闭空闲对象
        for(PooledObjectProxy obj:free_objects)obj.close();
        //等候在正运行中的对象退出
        while(this.getBusyCount()>0){
            logger.info("对象池'"+this.getName()+"'正在关闭中，请稍后！");
            try{this.wait(1000);}catch(Exception e){}
        }
    }
   
}

