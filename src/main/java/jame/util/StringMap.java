/**  
 * All rights Reserved, Designed By Jamesoft
 * @Title:  StringMap.java   
 * @Package jame.util; 
 * @author: Jame
 * @Email:  jamecloud@163.com  
 * @date:   2017-02-21 09:33:40  
 * @version V1.0 
 * @Copyright: 2017 Jamesoft All rights reserved 
 * 注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目
 * @Description:  类功能说明
 * 用以存储Key值不区分大小写的文本字符串
 * 通常用以保存属性等
 */

package jame.util;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public final class StringMap extends LinkedHashMap<String,String>{
    /**
     * 从xml报文中解析出一个Map,但必须只能是一个Map的xml。
     * 如果是多个Map的文本，所获得的Map为最后一个xml文本中的最后一个Map
     * @param xml 从xml字符串中解析值，并存储在StringMap中
     * @return 
     */
    public static StringMap fromXml(String xml){
        StringMap bg = new StringMap();
        int stPos = 0,edPos = 0;//开始位置，结束位置
        String key,value;//开始标签：结束标签
        while(true){
            stPos = xml.indexOf("<",edPos);//从前一个结束位置开始搜寻标签开始位置
            edPos = xml.indexOf(">",stPos);//从当前开始位置梭巡标签结束位置
            if(stPos<0||edPos<0)break;
            key = xml.substring(stPos+1, edPos).trim().replace("-","_");
            //获取tag 2015-3-19：对于命名中的特殊字符作转换,后期需要考虑完整的解决方案
            stPos = xml.indexOf("</",edPos);//下一个标签为当前标签的结束标签
            value = xml.substring(edPos+1,stPos);//之间的值为标签内容
            edPos = xml.indexOf(">", stPos);//搜索标签的结束位置
            if(key!=null&&value!=null)bg.set(key, value);//保存标签值
            if(stPos<0||edPos<0)break;
        }
        return bg;
    }

    public StringMap(){
//        super();
    }
    
    /**
     * 对象复制
     * @param bg
     */
    public StringMap(StringMap bg){
       this.putAll(bg);
    }
    
    /**
     * 对象复制
     * @param map
     */
    public StringMap(Map map){
       this.putAll(map);
    }
    /**
     * 
     * @param name 属性名
     * @param value 属性值
     */
    public void set(String name,String value){
        if(name==null)return;
        put(name.toUpperCase(),value);
    }
    /**
     * 获取指定属性的值
     * @param name 属性名
     * @return 属性值
     */
    public String get(String name){
        String rt = null;
        try{rt = super.get(name.toUpperCase());}catch (Exception e){}
        return rt;
    }
    /**
     * 是否包含指定属性
     * @param key 属性名
     */
    public boolean containsKey(String key){
        if(key==null)return false;
        return super.containsKey(key.toUpperCase());
    }
    /**
     * 复制对象值
     * @param bm
     */
    @Override
    public void putAll(Map bm){
       if(bm==null)return;
       String key;
       Iterator<String> bgs = bm.keySet().iterator();
       while(bgs.hasNext()){
            key = bgs.next();
            if(bm.get(key)!=null)set(key,bm.get(key).toString());//非空才保存
       }
    }
    
    public boolean equals(StringMap bg){
        return super.equals(bg);
    }
    /**
     * 复制对象
     */
    @Override
    public StringMap clone(){
        return new StringMap(this);
    }
   /**
    * 将对象转换为xml文本
    */
    public String toXml(){
        String key;
        StringBuilder sb = new StringBuilder();
        Iterator<String> keys = this.keySet().iterator();
        while(keys.hasNext()){
            key = keys.next();
            sb.append("<").append(key).append(">")//所有的key已经是大写
              .append(this.get(key).replaceAll("<", "&lt;").replaceAll(">", "&gt;"))
              .append("</").append(key).append(">");
        }
        return sb.toString();
    }
}
