/**  
 * All rights Reserved, Designed By Jamesoft
 * @Title:  ConnectionGroup.java   
 * @Package jame.sql; 
 * @author: Jame
 * @Email:  jamecloud@163.com  
 * @date:   2017-01-10 15:40:28  
 * @version V1.0 
 * @Copyright: 2017 Jamesoft All rights reserved 
 * 注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目
 * @Description:  类功能说明
 * ┏━━━━━━━━━━━━━━━━┓
 * ┃数据库连接组                   ┃
 * ┗━━━━━━━━━━━━━━━━┛
 *  在一个交易中，有可能会用到多个数据库连接，这些连接连接到不同的数据库
 *  为了便于管理，通过本类一致完成事务开始、提交、回滚以及归还操作
 *  注意:本类并不保证跨库事务的一致性，只提供了一种便捷的操作
 *  
 */

package jame.sql;

import java.sql.Connection;
import java.util.HashMap;

final public class ConnectionGroup {
    final private HashMap<String,Connection> conns = new HashMap<String,Connection>();
    
    /**
     * 将书库库连接加入到连接组
     * @param name 数据库名称
     * @param conn 数据库连接
     */
    public void addConnection(String name,Connection conn){
        conns.put(name, conn);
    }
    /**
     * 获取指定数据库的连接
     * @param name
     * @return 
     * @throws java.lang.Exception
     */ 
    public Connection getConnection(String name) throws Exception{
        name = name.toUpperCase();
        return conns.get(name.toUpperCase());
    }
    /**
     * 开启组事务
     * @throws java.lang.Exception
     */
    public void beginTrans() throws Exception{
        for(Connection conn:conns.values())conn.setAutoCommit(false);
    }
    /**
     * 提交组事务
     * @throws java.lang.Exception
     */
    public void commit() throws Exception{
        try{
            for(Connection conn:conns.values())conn.commit();
        }catch(Exception e){
            rollback();
            throw e;
        }
    }
    /**
     * 回滚组事务
     */
    public void rollback() throws Exception{
        boolean isOk =  true;
        for(Connection conn:conns.values())try{
            conn.rollback();
        }catch(Exception e){
            isOk = false;
        }
        if(!isOk)throw new Exception("数据库事务回滚失败，请检查数据一致性。");
    }
    /**
     * 关闭组连接
     * @throws java.lang.Exception
     */
    public void close() throws Exception{
        for(Connection conn:conns.values())try{
            conn.close();
        }catch(Exception e){}
        conns.clear();
    }
    
}
