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
 * 线程池、对象池参数配置类
 */

package jame.lang;

import java.util.logging.Logger;

public class PoolConfig {

    private int minFreeCount = 10;//默认先启动最小规模线程
    private int maxFreeCount = 30;//最大空闲对象10：空闲的对象超过10将关闭多余对象
    private int maxCount = 100;//最大对象数，当总对象数达到这个数时即便系统再繁忙也不会创建新的对象
    private int maxTaskCount = 0;//最大任务队列长度
    private Logger logger;//线程池的日志对象
    private long maxLiveTime = 0l;//最长生存时间：默认不控制
    private long maxHireTime = 0l;//最长借用时间

    /**
     * 获取池的最小空闲数量
     * @return the minFreeCount
     */
    public int getMinFreeCount() {
        return minFreeCount;
    }

    /**
     * 设置池的最小空闲数量
     * 当池中空闲对象或线程数量小于这个数时，启动创建机制
     * @param minFreeCount the minFreeCount to set
     */
    public void setMinFreeCount(int minFreeCount) {
        this.minFreeCount = minFreeCount;
    }

    /**
     * 获取池的最大空闲对象数量设置
     * @return the maxFreeCount
     */
    public int getMaxFreeCount() {
        return maxFreeCount;
    }

    /**
     * 设置池的最大空闲数量
     * 当空闲对象数量大于这个设置时，启动对象关闭机制
     * @param maxFreeCount the maxFreeCount to set
     */
    public void setMaxFreeCount(int maxFreeCount) {
        this.maxFreeCount = maxFreeCount;
    }

    /**
     * 获取池的最大对象数量设置
     * @return the maxCount
     */
    public int getMaxCount() {
        return maxCount;
    }

    /**
     * 设置池的最大对象数量设置
     * 当池中对象数量大于等于这个数时，即便空闲对象数量小于最小空闲对象数量设置，也不会创建新的对象
     * @param maxCount the maxCount to set
     */
    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    /**
     * 获取任务池的最大长度限制
     * @return the maxTaskCount
     */
    public int getMaxTaskCount() {
        return maxTaskCount;
    }

    /**
     * 获取任务池的最大长度
     * 当任务池长度超过这个限制时，将拒绝后续服务
     * @param maxTaskCount the maxTaskCount to set
     */
    public void setMaxTaskCount(int maxTaskCount) {
        this.maxTaskCount = maxTaskCount;
    }

    /**
     * 设置池所使用的日志对象
     * @return the logger
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * 获取对象的最大生存时间
     * @return the maxLiveTime
     */
    public long getMaxLiveTime() {
        return maxLiveTime;
    }

    /**
     * 设置对象的最大生存时间
     * @param maxLiveTime the maxLiveTime to set
     */
    public void setMaxLiveTime(long maxLiveTime) {
        this.maxLiveTime = maxLiveTime;
    }

    /**
     * 获取对象的最大出借时间
     * @return the maxHireTime
     */
    public long getMaxHireTime() {
        return maxHireTime;
    }

    /**
     * 设置对象的最大出借时间
     * @param maxHireTime the maxHireTime to set
     */
    public void setMaxHireTime(long maxHireTime) {
        this.maxHireTime = maxHireTime;
    }
}
