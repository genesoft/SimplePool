/**  
 * All rights Reserved, Designed By Jamesoft
 * @Title:  DateUtil.java   
 * @Package jame.util; 
 * @author: Jame
 * @Email:  jamecloud@163.com  
 * @date:   2017-03-20 21:19:17  
 * @version V1.0 
 * @Copyright: 2017 Jamesoft All rights reserved 
 * 注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目
 * @Description:  类功能说明
 * 时间处理工具类
 */

package jame.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

final public class DateUtil {
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    private Calendar cal= Calendar.getInstance();

    public DateUtil(){}

    public DateUtil(Date date){
        cal.setTime(date);
    }

    public DateUtil(String date) throws ParseException{
        cal.setTime(parse(date));
    }
    
    public void setTime(String date) throws ParseException{
        cal.setTime(parse(date));
    }
    
    public void setTime(Date dt){
        cal.setTime(dt);
    }

    public void setFormat(String fmt){
        sdf.applyPattern(fmt);
    }

    public void addYear(int diff){
        cal.add(Calendar.YEAR, diff);
    }

    public void addMonth(int diff){
        cal.add(Calendar.MONTH, diff);
    }

    public void addDay(int diff){
        cal.add(Calendar.DATE, diff);
    }

    public void addHour(int diff){
        cal.add(Calendar.HOUR, diff);
    }

    public void addMinute(int diff){
        cal.add(Calendar.MINUTE, diff);
    }

    public void addSecond(int diff){
        cal.add(Calendar.SECOND, diff);
    }

    public void add(int field,int diff){
        cal.add(field, diff);
    }

    public Date getDate(){
        return cal.getTime();
    }

    public String getDate(String pat){
        if(pat==null)pat="yyyyMMdd";
        return new SimpleDateFormat(pat).format(getDate());
    }
    
    public String getTime(String pat){
        if(pat==null)pat="HHmmss";
        return new SimpleDateFormat(pat).format(getDate());
    }
    
    public String getDateTime(String pat){
        if(pat==null)pat="yyyyMMddHHmmss";
        return new SimpleDateFormat(pat).format(getDate());
    }

    public int compare(Date date){
        if(date==null)date = new Date();
        return getDate().compareTo(date);
    }

    public int compare(String date) throws ParseException{
        return getDate().compareTo(parse(date));
    }
    

    public Date parse(String date) throws ParseException{
        return parse(date,null);
    }
    

    public Date parse(String date,String pat) throws ParseException{
        Date rt = null;
        SimpleDateFormat df = sdf;
        if(pat!=null)df = new SimpleDateFormat(pat);
        if(date!=null)rt= df.parse(date);
        return rt;
    }
    /**
    * 获取间隔时间的描述
     */
    public String getString(long ms){
        if(ms<=0)return "0毫秒";
        long diff = 24*60*60*1000;
        long count = ms / diff;
        StringBuilder rt = new StringBuilder();
        if(count>0){
            rt.append(count).append("天");
            ms -= count * diff;
        }
        diff = 60*60*1000;
        count = ms / diff;
        if(count>0){
            rt.append(count).append("小时");
            ms -= count * diff; 
        }
        diff = 60*1000;
        count = ms / diff;
        if(count>0){
            rt.append(count).append("分钟");
            ms -= count * diff; 
        }
        diff = 1000;
        count = ms / diff;
        if(count>0){
            rt.append(count).append("秒");
            ms -= count * diff; 
        }
        if(ms>0)rt.append(ms).append("毫秒");
        return rt.toString();
    }

    @Override
    public String toString(){
        return sdf.format(cal.getTime());
    }
    
    public String toString(Date dt){
        if(dt==null)dt=new Date();
        return sdf.format(dt);
    }

    public static void main(String args[]) throws ParseException{
        DateUtil du = new DateUtil();
        du.setTime("20150227");
        System.out.println(du.getDate("E"));
    }

}
