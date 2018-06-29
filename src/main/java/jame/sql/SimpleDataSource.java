/**  
 * All rights Reserved, Designed By Jamesoft
 * @Title:  Database.java   
 * @Package jame.sql; 
 * @author: Jame
 * @Email:  jamecloud@163.com  
 * @date:   2017-09-12 15:07:02  
 * @version V1.0 
 * @Copyright: 2017 Jamesoft All rights reserved 
 * 注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目
 * @Description:  类功能说明
 * 数据库连接池使用本类创建到指定数据库的连接
 * 注意：create方法创建的连接不是连接池管理的连接，请勿使用该方法创建连接
 */
 
package jame.sql;
 
import jame.lang.ObjectPool;
import jame.lang.PoolConfig;
import jame.lang.PooledObjectFactory;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;

final public class SimpleDataSource extends PooledObjectFactory implements javax.sql.DataSource{ 
    //配置数据库连接属性
    private final String url;
    private final Driver driver;//驱动程序
    private final Properties up = new Properties();//数据库连接参数
    /**
     * 创建连接到指定数据库的连接池
     * 注意：本方法没有传入数据库用户名和密码
     * @param name 数据源的名称
     * @param url 数据源的连接字符串
     * @param driver 数据库的驱动类
     * @throws java.lang.Exception
     */
    public SimpleDataSource(String name,String url,String driver) throws Exception{
        this(name,url,null,null,driver,null);
    }
    /**
     * 创建连接到指定数据库的连接池
     * 注意：本方法没有传入数据库用户名和密码
     * @param name 数据源的名称
     * @param url 数据源的连接字符串
     * @param driver 数据库的驱动类
     * @param poolConfig 连接池配置参数
     * @throws java.lang.Exception
     */
    public SimpleDataSource(String name,String url,String driver,PoolConfig poolConfig) throws Exception{
        this(name,url,null,null,driver,poolConfig);
    }
    /**
     * 创建连接到指定数据库的连接池
     * 注意：create方法创建的连接不是连接池管理的连接，请勿使用该方法创建连接
     * @param name 数据源的名称
     * @param url 数据源的连接字符串
     * @param userName 数据库用户名
     * @param password 数据库密码
     * @param driver 数据库的驱动类
     * @throws java.lang.Exception
     */
    public SimpleDataSource(String name,String url,String userName,String password,String driver) throws Exception{
        this(name,url,userName,password,driver,null);
    }
    /**
     * 创建连接到指定数据库的连接池
     * 注意：create方法创建的连接不是连接池管理的连接，请勿使用该方法创建连接
     * @param name 数据源的名称
     * @param url 数据源的连接字符串
     * @param userName 数据库用户名
     * @param password 数据库密码
     * @param driver 数据库的驱动类
     * @throws java.lang.Exception
     */
    public SimpleDataSource(String name,String url,String userName,String password,String driver,PoolConfig poolConfig) throws Exception{
        if(url==null||url.isEmpty())throw new Exception("数据库连接字符串不能为空");
        if(driver==null||driver.isEmpty())throw new Exception("数据库驱动类不能为空");
        this.setName(name);
        this.url = url;
        this.driver = (Driver)Class.forName(driver).newInstance();
        this.setUserName(userName);
        this.setPassword(password);
        super.setPoolConfig(poolConfig);
    }
    /**
     * 设置数据库的连接用户
     * @param userName the userName to set
     */
    public void setUserName(String userName) {
        if(userName!=null&&!userName.isEmpty())up.setProperty("user", userName);
        else up.remove("user");
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        if(password!=null&&!password.isEmpty())up.setProperty("password", password);
        else up.remove("password");
    }
   
    /**
     * 获取数据库的一个真实连接
     * 注意：该连接尚未托管到连接池管理
     * @throws java.sql.SQLException
     */
    @Override
    public java.sql.Connection getConnection() throws SQLException{
        ObjectPool pool = ObjectPool.getInstance(this.getName(),this);//他们是同一个class,所以不能再pool中直接获取
        try {
            return (Connection)pool.getObject(0);
        } catch (Exception ex) {
            throw new SQLException(ex);
        }
        
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        this.setUserName(username);
        this.setPassword(password);
        return this.getConnection();
    }
    
    /**
     * 创建连接到指定数据库的连接
     * 注意：本方法是直接创建到数据库的新连接，未经过连接池代理
     * @return 
     * @throws java.lang.Exception
     */
    @Override
    public Object create() throws Exception{
        return driver.connect(url,up);
    }

    @Override
    public void close(Object obj) {
        try{
            ((Connection)obj).close();
        }catch(Exception e){}
    }

    @Override
    public boolean isEnable(Object obj) {
        boolean rt = false;
        try{
            rt = ((Connection)obj).isValid(3);//若连接3秒无响应，则任务异常
        }catch(Exception e){}
        return rt;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;//不支持输出
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
    
    public static void main(String args[]) throws Exception{
       SimpleDataSource jds = new SimpleDataSource("cfg","jdbc:oracle:thin:@localhost:1521:XE","cfg","1lI62133","oracle.jdbc.OracleDriver");
       java.sql.Connection conn = jds.getConnection();
       Class clss[] = conn.getClass().getInterfaces();
       for(Class cls:clss)System.out.println(cls.getCanonicalName());
       Statement smt = conn.createStatement();
       java.sql.ResultSet rs = smt.executeQuery("select * from tbl");
        ResultSetMetaData rsmd = rs.getMetaData();
        for(int i = 1; i <= rsmd.getColumnCount(); i++){
            System.out.println(rsmd.getColumnName(i));
            System.out.println(rsmd.getColumnLabel(i));
        }
       System.exit(0);
   }
    
}
