/**  
 * All rights Reserved, Designed By Jamesoft
 * @Title:  Pool.java   
 * @Package jame.lang; 
 * @author: Jame
 * @Email:  jamecloud@163.com  
 * @date:   2017-08-25 14:42:08  
 * @version V1.0 
 * @Copyright: 2017 Jamesoft All rights reserved 
 * 注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目
 * @Description:  类功能说明
 * 对象池、线程池、被动池的超类
 * 创建对象时报错需要记录日志，可以用setLogger方法传入日志对象，
 * 若不传入，系统将使用java默认的日志对象用以记录日志
 * 任务池相关参数和方法向上封装在超类的原因是：这些参数可以充分反应出系统的压力情况，
 * 便于开发代码监控所有池对象的工作状态
 */

package jame.lang;

import java.util.LinkedList;
import java.util.logging.Logger;

abstract public class Pool {
    /**
     * creater对象池功能
     * 用以完成所有的对象池中的对象创建任务。
     */
    private static LinkedList<Pool> Pools = new LinkedList<Pool>();//用以保存所有的对象池对象，主要用以对象池状态查询和管理
    final static protected ThreadPool CreaterPool = ThreadPool.getInstance(CreaterThread.class);
    //轮循时间
    private static long RotateTime = 30 * 60 * 1000;//管理进程轮循时间，用以清除无效对象和无限工作线程
    //池基本参数
    protected String name;//对象池的名称
    protected Logger logger;//日志对象
    protected int minFreeCount = 10;//默认先启动最小规模线程
    protected int maxFreeCount = 30;//最大空闲对象10：空闲的对象超过10将关闭多余对象
    protected int maxCount = 100;//最大对象数，当总对象数达到这个数时即便系统再繁忙也不会创建新的对象
    //线程池特有参数
    protected int maxTaskCount = 0;//最大任务队列长度
    //对象池特有参数
    private long maxLiveTime = 0l;//对象的最长生存时间:默认不控制
    private long maxHireTime = 0l;//对象的最长使用时间:默认不控制
    //池工作状态
    protected boolean isStopped = false;//对象池是否已关闭

    /**
     * 获取所有的对象池
     * 本方法只用以将所有对象池索引返回，用以查询对象池状态，不能作为具体的对象池使用入口
     * @return 返回系统中所有的池
     */
    final public static LinkedList<Pool> getAllPools(){
        return Pools;
    }
    /**
     * 获取池巡检时间间隔
     * @return the RotateTime
     */
    public static long getRotateTime() {
        return RotateTime;
    }

    /**
     * 设置池巡检时间
     * 定期启动巡检线程，关闭不可用对象和线程
     * @param aRotateTime the RotateTime to set
     */
    public static void setRotateTime(long aRotateTime) {
        RotateTime = aRotateTime;
    }
    /**
     * 构造方法，以默认的参数创建池对象
     */
    public Pool(){
        if(Pools==null)Pools = new LinkedList<Pool>();
        //所有创建的对象都保留在这里，哪怕暂时关闭，对象池作为系统的核心工作对象
        //不会作为临时对象创建后又销毁，除非系统停止服务
        Pools.add(this);
    }
    /**
     * 设置对象池参数
     * @param poolConfig
     */
    public void setConfig(PoolConfig poolConfig){
        if(poolConfig==null)return;
        this.logger = poolConfig.getLogger()!=null?poolConfig.getLogger():Logger.getLogger(this.getName());
        this.minFreeCount = poolConfig.getMinFreeCount();
        this.maxFreeCount = poolConfig.getMaxFreeCount();
        this.maxCount = poolConfig.getMaxCount();
        this.maxTaskCount = poolConfig.getMaxTaskCount();
        this.maxLiveTime = poolConfig.getMaxLiveTime();
        this.maxHireTime = poolConfig.getMaxHireTime();
    }

    /**
     * 设置池名
     * @param name 设置池的名称
     */
    public void setName(String name){
        this.name = name;
    }
    /**
     * 返回对象池的名字
     * 
     * @return 获取池的名称
     */
    public String getName(){
        return this.name;
    }
    /**
     * 设置池的日志对象
     * @param logger 日志对象
     */
    public void setLogger(Logger logger){
        this.logger = logger;
    }
    /**
     * 设置最小空闲数量，一旦空闲对象小于该设置，将引发对象创建机制
     * @param freeCount 设置池的最小空闲对象数量
     */
    public void setMinFreeCount(int freeCount){
        this.minFreeCount = freeCount;
    }
    /**
     * 返回最小空闲对象数
     * 
     * @return 获取池的最小空闲对象数量
     */
    public int getMinFreeCount(){
        return this.minFreeCount;
    }
    /**
     * 设置对大空闲对象数，一旦空闲对象超过这个数量，将引发对象关闭机制
     * @param freeCount 设置池的最大空闲对象数量
     */
    public void setMaxFreeCount(int freeCount){
        this.maxFreeCount = freeCount;
    }
    /**
     * 返回最大空闲对象数
     * @return 
     */
    public int getMaxFreeCount(){
        return this.maxFreeCount;
    }
    /**
     * 设置最大线程数，设置池的最大对象数量，一旦总想象数量超过这个数，
     * 即便空闲对象数量小于最小空闲对象设置，也不会引发创建机制
     * @param maxCount 设置池的最大对象数量
     */
    public void setMaxCount(int maxCount){
        this.maxCount = maxCount;
    }
    /**
     * 返回池的最大对象数量设置值
     * @return 返回池的最大对象数量设置值
     */
    public int getMaxCount(){
        return this.maxCount;
    }
    /**
     * 设置线程池最大任务队列长度
     * 对象池该参数无效
     * @param length 最大任务队列长度
     */
    public void setMaxTaskCount(int length){
        this.maxTaskCount = length;
    }
    /**
     * 返回最大任务队列长度设置
     * 对象池该参数无效，返回0
     * @return 
     */
    public int getMaxTaskCount(){
        return this.maxTaskCount;
    }
    /**
     * 返回池中任务队列中的任务数量
     * 对象池该参数无效，返回0
     * @return 返回任务队列中等候执行的任务数量
     */
    abstract public int getTaskCount();
    /**
     * 检查任务池是否已满
     */
    /**
     * 判断任务队列是否已经满了
     * @return 判断任务队列是否已经满了
     */
    public boolean isTaskFull(){
        if(this.maxTaskCount<=0)return false;
        else return getTaskCount()>=this.maxTaskCount;
    }

    /**
     * 对象池特有参数
     * 设置对象的最长生存时间，超过这个时间后再调用这个对象的任何方法均会报错。
     * @return the maxLiveTime
     */
    public long getMaxLiveTime() {
        return maxLiveTime;
    }

    /**
     * 对象池特有参数
     * 设置对象的最长生存时间，超过这个时间后再调用这个对象的任何方法均会报错。
     * 
     * @param maxLiveTime the maxLiveTime to set
     */
    public void setMaxLiveTime(long maxLiveTime) {
        this.maxLiveTime = maxLiveTime;
    }

    /**
     * 对象池特有参数
     * 设置对象的最长借用时间，超过这个时间后再调用这个对象的任何方法均会报错。
     * @return the maxHireTime
     */
    public long getMaxHireTime() {
        return maxHireTime;
    }

    /**
     * 对象池特有参数
     * 设置对象的最长借用时间，超过这个时间后再调用这个对象的任何方法均会报错。
     * @param maxHireTime the maxHireTime to set
     */
    public void setMaxHireTime(long maxHireTime) {
        this.maxHireTime = maxHireTime;
    }
    /**
     * 获取对象池中正忙的对象数量
     * @return 返回池中正在工作的对象数量
     */
    abstract public int getBusyCount();
    /**
     * 返回池中当前正处于空闲状态的对象数量
     * @return 返回池中当前正处于空闲状态的对象数量
     */
    abstract public int getFreeCount();
    /**
     * 返回池当前的工作压力情况：
     * 
     * return = busyCount / maxCount * 100%
     * @return 返回池当前的工作压力情况
     */
    public double getPressure(){
        return getBusyCount()*100d/maxCount;
    }
    /**
     * 池是否已经达到最大规模
     * @return 池是否已经达到最大规模
     */
    public boolean isFull(){
        return getBusyCount()+getFreeCount()>=getMaxCount();
    }
    /**
     * 执行新建对象任务
     * 逻辑：如果空闲对象数量小于最小空闲对象数量，且池未满，则创建新的对象
     * 一直到空闲对象数量大于等于最小空闲对象数量，或池已经达到最大规模
     */
    abstract public void  create();
    /**
     * 是否可以关闭某个被池化的对象
     * @param obj 如果空闲对象数量大于最大空闲对象数量，则关闭当前对象
     * @return 
     */
    abstract public boolean  close(Object obj);      
    /**
     * 池的巡检方法
     * 检查所有对象和线程的可用状态
     * 若不可用，则关闭对象并从空闲池中删除
     */
    abstract public void check();
    /**
     * 关闭对象池
     */
    final public void stop(){
        isStopped = true;
    }
    /**
     * 重新恢复服务
     */
    final public void start(){
        isStopped = false;
    }
    /**
     * 判断对象池的服务状态
     * @return 是否已关闭
     */
    final public boolean isStopped(){
        return isStopped;
    }
    /**
    * 本类用以定时调度每个池对象的维护方法，检查每个池对象的可用状态，及时清除无效对象
    */
   class GuardThread extends Thread{
       @Override
       public void run(){
           while(true){
               synchronized(this){
                   try{this.wait(RotateTime);}catch(Exception e){}
               }
               //执行池状态巡检方法
               for(Pool pool : Pools ){
                   pool.check();//调度池的巡检方法，对池对象进行检查
               }
           }
       }
   }
}
