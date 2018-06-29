/**  
 * All rights Reserved, Designed By www.jamesoft.com
 * @Title:  PrinterThread   
 * @Package jame.test 
 * @author: Jame软件工作室
 * @Email:  jamecloud@163.com  
 * @date:   2018-6-27 17:22:57   
 * @version V1.0 
 * @Copyright: 2017 www.jamesoft.com Inc. All rights reserved. 
 * 注意：本内容仅限于Jame软件工作室内部传阅，禁止外泄以及用于其他的商业目
 * @Description:  类功能说明
 * 线程池测试类，用以在显示指定字符串
 */

package jame.test;

import jame.lang.PooledThread;
import jame.lang.ThreadPool;

public class PrinterThread implements PooledThread{

    @Override
    public Object execute(Object obj) throws Exception {
        System.out.println(obj);
        return null;
    }

    @Override
    public void close() throws Exception {
        
    }

    @Override
    public boolean isEnable() {
        return true;
    }
    
    public static void main(String args[]) throws Exception{
        ThreadPool pool = ThreadPool.getInstance(PrinterThread.class);
        pool.addTask("你好");
        pool.addTask(pool);
    }

}
