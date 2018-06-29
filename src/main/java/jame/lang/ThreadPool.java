/**  
 * All rights Reserved, Designed By Jamesoft
 * @Title:  ThreadPool.java   
 * @Package jame.lang; 
 * @author: Jame
 * @Email:  jamecloud@163.com  
 * @date:   2017-08-14 16:17:07  
 * @version V1.0 
 * @Copyright: 2017 Jamesoft All rights reserved 
 * 注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目
 * @Description:  类功能说明
 * 线程池
 * 所有需要池化管理的线程必须按PooledThread接口规范开发
 * 若是生产不能修改的线程需要池化管理，可以按PooledThread接口开发代理线程
 * PS：若任务对象需要添加过期等信息，由用户自定义开发，本线程池不负责任务对象的任何处理
 */

package jame.lang;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

final public class ThreadPool extends Pool{
    private static HashMap<String,ThreadPool> pools = new HashMap<String,ThreadPool>();//保存所有在内存中运行的线程池
    //类变量
    final private Class<? extends PooledThread> pooledClass;//线程池任务执行主体
    final private ConcurrentLinkedQueue<PooledThreadProxy> busy_threads = new ConcurrentLinkedQueue();//正在工作的线程
    final private ConcurrentLinkedQueue<PooledThreadProxy> free_threads = new ConcurrentLinkedQueue();//正在等候工作的线程
    final private ConcurrentLinkedQueue tasks = new ConcurrentLinkedQueue();//任务池
    final private   Object lock = new Object(),//任务信号锁
                    createLock = new Object();//线程创建锁
    private int t_code = 1;//线程编号
    /**
     * 返回已经创建好的指定命名的线程池
     * @param name 线程池的名称
     * @return 线程池
     */
    public static ThreadPool getInstance(String name){
        if(pools==null)pools=new HashMap<String,ThreadPool>();
        return pools.get("ThreadPool_"+name);
    }
    /**
     * 创建一个以cls为工作线程的线程池
     * @param cls 线程类
     * @return 
     */
    public static ThreadPool getInstance(Class<? extends PooledThread> cls){
        return getInstance(cls.getCanonicalName(),cls);
    }
    /**
     * 创建一个以cls为工作线程的指定名称线程池
     * @param name 线程池的名称
     * @param cls 线程类
     * @return 
     */
    public static ThreadPool getInstance(String name,Class<? extends PooledThread> cls){
        return getInstance(name,cls,null);
    }
    
    /**
     * 创建一个以cls为工作线程的指定名称线程池
     * @param name 线程池的名称
     * @param cls 线程类
     * @param poolConfig 线程池配置参数
     * @return 
     */
    public static ThreadPool getInstance(String name,Class<? extends PooledThread> cls,PoolConfig poolConfig){
        ThreadPool rt = getInstance(name);
        if(rt==null&&cls!=null)rt = new ThreadPool(name,cls,poolConfig);
        return rt;
    }
    /**
     * 以被池化管理的类为参数创建线程池
     */
    private ThreadPool(Class<? extends PooledThread> cls){
        this(cls.getCanonicalName(),cls,null);
    }
   
    /**
     * 以被池化管理的类为参数创建线程池
     */
    private ThreadPool(String name,Class<? extends PooledThread> cls){
        this(name,cls,null);
    }
    
    /**
     * 以cls为原型创建指定名称的线程池
     */
    private ThreadPool(String name,Class<? extends PooledThread> cls,PoolConfig poolConfig){
        this.pooledClass = cls;
        this.name="ThreadPool_"+name;
        this.logger = Logger.getLogger(name);
        pools.put(this.name,this);
        super.setConfig(poolConfig);
        this.create();//要求创建后立即可用
    }
    /**
     * 查看当前线程池的状态
     * @return 正在工作的线程数量
     */
    @Override
    public int getBusyCount(){
        return busy_threads.size();
    }
    /**
     * 查看当前的空闲线程
     * @return 正处于空闲的线程数量
     */
    @Override
    public int getFreeCount(){
        return free_threads.size();
    }
    
    /**
     * 看线程池的工作状态是否需要加入新的线程、如果需要则直接创建
     * 本方法在添加任务时或线程池创建时执行
     */
    @Override
    final public void create(){
        if(isFull()||isStopped()||this.getFreeCount()>=this.getMaxFreeCount())return;//如果已经达到最大规模则返回
        synchronized(createLock){//一次只允许一个线程执行本程序块
            PooledThreadProxy thread;
            while(free_threads.size()<minFreeCount&&//空闲对象数量小于最小空闲对象数
                    (maxCount>(free_threads.size()+busy_threads.size())))try{//总对象数小于系统最大总对象数
                thread = createThread();
                if(thread!=null){
                    free_threads.add(thread);//直接加入空闲线程
                    thread.start();//创建新的线程立即尝试工作，若无任务则转入睡眠
                }//新加入的线程直接启动：若无任务则自动睡眠
            }catch(Exception e){
                break;//若创建工作线程报错，则退出本次创建，后续根据任务调度持续创建
            }
        }
    }

 
    /**
     * 如果创建对象失败：需要记录状态
     */
    private PooledThreadProxy createThread() throws Exception{
        if(isFull()||isStopped()||this.getFreeCount()>=this.getMaxFreeCount())return null;//如果对象已经达到最大规模则直接返回
        PooledThread obj;
        try{
            obj = (PooledThread)pooledClass.newInstance();
            return new PooledThreadProxy(this,obj);
        }catch(Exception e){
            Logger.getLogger(ObjectPool.class.getCanonicalName())
                    .log(Level.SEVERE,"线程池'"+getName()+"在创建对象时发生错误。",e);
            throw e;
        }
    }


    /**
     * 是否允许关闭某线程
     * 关闭条件：
     * （1）空闲线程数量大于最大空闲线程数量
     * （2）线程对象已不可用
     * （3）停止服务
     * @return 
     */
    @Override
    final public boolean close(Object obj){
        PooledThreadProxy ptp = (PooledThreadProxy)obj;
        boolean rt = free_threads.contains(ptp)
                &&(free_threads.size()>maxFreeCount||!ptp.isEnable()||isStopped());//需要关闭的情况：空闲线程多余系统规定；线程池关闭
        if(rt){
            free_threads.remove(ptp);
            ptp.close();
        }
        return rt;

    }
   
    /**
     * 向任务池添加一个对象，并激活一个线程取执行任务，
     * 并检查线程池是否需要创建更多的线程用以执行任务
     * @param task
     * @throws java.lang.Exception
     */
    final public void addTask(Object task) throws Exception{
        if(isStopped())throw(new Exception(this.getName()+"服务维护中，请稍后再试。"));
        if(isTaskFull())throw(new Exception(this.getName()+"服务繁忙，请稍后再试。"));
        if(tasks.contains(task))return;//若已经添加过了本任务，则返回
        tasks.offer(task);//添加任务（1）
        synchronized(lock){lock.notify();}//激活一个线程使其参与工作
        if(!task.equals(CreaterPool)&&!isFull())//如果任务已经等于creater本身，那么（1）处已经添加任务，这里不需要再次添加，否则陷入死循环
            try{CreaterPool.addTask(this);}catch(Exception e){}//creater启动、检查是否需要创建新的线程以及时的相应后续请求
    }
    /**
     * 从任务池获取一个对象
     */
    private Object getTask(){
        return tasks.poll();
    }
    /**
     * 获取任务队列中正等候执行的任务数量
     * @return 返回当前等候执行的任务数量
     */
    @Override
    public int getTaskCount(){
        return tasks.size();
    }
    
    /**
     * 按本线程池的设计逻辑，只要线程池一直在使用中，那么线程池的对象将一直是可用的
     * 因此，只要线程池的工作压力大于0，线程池状态就应该应该是良好
     */
    @Override
    public void check() {
        if(this.isFull())return;//如果对象池正繁忙，返回
        if(this.getPressure()>=20)return;//如果对象池工作压力大于20%返回
        for(PooledThreadProxy pop:this.free_threads){
            if(!pop.isEnable())this.free_threads.remove(pop);//如果对象不可用，则删除对象
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
        while(this.getTaskCount()!=0){
            System.out.println("线程池'"+this.getName()+"'正在关闭中，请稍后！");
        }
    }
    /**
     * 包装执行线程
     */
    final class PooledThreadProxy extends Thread{
        final private ThreadPool pool;//被线程所关联的线程池
        final private PooledThread act;//执行任务的主体方法
        /**
         * 构造方法
         */
        public PooledThreadProxy(ThreadPool pool,PooledThread act){
            this.pool = pool;
            this.act = act;
            this.setName(act.getClass().getCanonicalName()+"_"+(t_code++));
        }
        /**
         * 关闭线程
         */
        public void close(){
            try{act.close();}catch(Exception e){}
        }
        /**
         * 检查线程是否可用
         */
        public boolean isEnable(){
            return act.isEnable();
        }
        /**
         * 执行任务
         */
        @Override
        final public void run(){
            Object task;//用以接受本线程获取的任务
            synchronized(this){try{lock.wait();}catch(Exception e){}}
            while(true){
                if(!this.isEnable()){//如果本线程不可用，则强制关闭该线程
                    free_threads.remove(this);
                    this.close();
                    synchronized(lock){lock.notify();}//通知一个新的线程启动工作
                    break;
                }
                if((task=getTask())!=null){
                    free_threads.remove(this);
                    busy_threads.add(this);
                    try{
                        act.execute(task);
                    }catch(Exception e){//执行任务：需要act自己处理相关错误，不能报错
                        Logger.getLogger(act.getClass().getSimpleName())
                            .log(Level.SEVERE,getName()+"线程池执行任务时发生错误。",e);
                    }
                    busy_threads.remove(this);
                    if(!isStopped()&&this.isEnable())free_threads.add(this);
                    synchronized(this){try{act.wait(1);}catch(Exception e){}}//让出cpu执行权，让其他线程得以执行
                    //执行完毕后并不进入lock.wait状态，而是再次获取任务，再没有任务时才进入等候状态
                    //这样可以让处于运行状态的线程一直运行，避免线程切换造成的性能消耗
                }else{//没有任务了才决定是否关闭对象
                    if(pool.close(this))break;//如果线程关闭close(this)也会返回true
                    else free_threads.add(this);
                    synchronized(this){try{lock.wait();}catch(Exception e){}}//如果没有任务则开始睡眠
                }
            }
        }
    }

}
