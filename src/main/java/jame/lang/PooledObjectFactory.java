/**  
 * All rights Reserved, Designed By Jamesoft
 * @Title:  PooledObjectFactory.java   
 * @Package jame.lang; 
 * @author: Jame
 * @Email:  jamecloud@163.com  
 * @date:   2016-11-02 10:09:14  
 * @version V1.0 
 * @Copyright: 2017 Jamesoft All rights reserved 
 * 注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目
 * @Description:  类功能说明
 * 对象池的创建接口
 */

package jame.lang;

abstract public class PooledObjectFactory {
    private String name;
    private PoolConfig poolConfig = new PoolConfig();
    /**
     * 设置对象池的名称
     * @param name
     */
    public void setName(String name){
        if(name!=null)this.name = name;
        else this.name = this.getClass().getCanonicalName();
    } 
    /**
     * 返回对象池的名称
     * @return 
     */
    public String getName(){
        return this.name;
    }
    /**
     * 创建池化对象
     * @return 创建对象
     * @throws java.lang.Exception 
     */
    abstract public Object create() throws Exception;//返回对象
    /**
     * 关闭对象
     * @param obj
     */
    abstract public void close(Object obj);
    /**
     * 判断对象是否可用
     * @param obj 判断对象是否可用
     * @return 是否可用
     */
    abstract public boolean isEnable(Object obj);
    /**
     * 获取对象池配置参数
     * 若为空，将使用默认的对象池配置参数
     * @return 
     */
    public PoolConfig getPoolConfig(){
        return this.poolConfig;
    }
    /**
     * 获取对象池配置参数
     * 若为空，将使用默认的对象池配置参数 
     * @param poolConfig
     */
    public void setPoolConfig(PoolConfig poolConfig){
        if(poolConfig!=null)this.poolConfig = poolConfig;
    }
}
