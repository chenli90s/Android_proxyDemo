package proxy.demo.com.proxydemo;

import android.content.Context;
import android.net.ProxyInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Chenli
 * @version $Rev$
 * @time 2016/11/4 19:30
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */

public class WifiConnect {

    private ProxyInfo mInfo;

    // 设置公共成员常量值
    public static void setEnumField(Object obj, String value, String name)
            throws SecurityException, NoSuchFieldException,IllegalArgumentException, IllegalAccessException {

        Field f = obj.getClass().getField(name);
        f.set(obj, Enum.valueOf((Class<Enum>) f.getType(), value));
    }


    // getField只能获取类的public 字段.
    public static Object getFieldObject(Object obj, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getField(name);
        Object out = f.get(obj); return out;
    }
    public static Object getDeclaredFieldObject(Object obj, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        Object out = f.get(obj); return out;
    }
    public static void setDeclardFildObject(Object obj, String name, Object object){
        Field f = null;
        try {
            f = obj.getClass().getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        f.setAccessible(true);
        try {
            f.set(obj,object);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    // 获取当前的Wifi连接
    public static WifiConfiguration getCurrentWifiConfiguration(WifiManager wifiManager) {
        if (!wifiManager.isWifiEnabled())
            return null;
        List<WifiConfiguration> configurationList = wifiManager.getConfiguredNetworks();
        WifiConfiguration configuration = null;
        int cur = wifiManager.getConnectionInfo().getNetworkId();
       // Log.d("当前wifi连接信息",wifiManager.getConnectionInfo().toString());
        for (int i = 0; i < configurationList.size(); ++i) {
            WifiConfiguration wifiConfiguration = configurationList.get(i);
            if (wifiConfiguration.networkId == cur)
                configuration = wifiConfiguration;
        }
        return configuration;
    }

    // API 17 可以用
    // 其它可以用的版本需要再测试和处理
    // @exclList 那些不走代理， 没有传null,多个数据以逗号隔开
    public static void setWifiProxySettingsFor17And(Context context, String host, int port, String exclList) {
        WifiManager wifiManager =(WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config;

        config = getCurrentWifiConfiguration(wifiManager);
        if(config == null)return;

        try {
            //get the link properties from the wifi configuration
            Object linkProperties = getFieldObject(config, "linkProperties");
            if(null == linkProperties) return;

            //获取类 LinkProperties的setHttpProxy方法
            Class<?> proxyPropertiesClass = Class.forName("android.net.ProxyProperties");
            Class<?>[] setHttpProxyParams = new Class[1];
            setHttpProxyParams[0] = proxyPropertiesClass;
            Class<?> lpClass = Class.forName("android.net.LinkProperties");

            Method setHttpProxy = lpClass.getDeclaredMethod("setHttpProxy",setHttpProxyParams);
            setHttpProxy.setAccessible(true);


            // 获取类 ProxyProperties的构造函数
            Constructor<?> proxyPropertiesCtor = proxyPropertiesClass.getConstructor(String.class,int.class, String.class);
            // 实例化类ProxyProperties
            Object proxySettings =proxyPropertiesCtor.newInstance(host, port, exclList);


            //pass the new object to setHttpProxy

            Object[] params = new Object[1];
            params[0] = proxySettings;
            setHttpProxy.invoke(linkProperties, params);
            setEnumField(config, "STATIC", "proxySettings");

            //save the settings
            wifiManager.updateNetwork(config);
            wifiManager.disconnect();
            wifiManager.reconnect();
        } catch(Exception e) { }
    }
    // 取消代理设置
    public static void unsetWifiProxySettingsFor17And(Context context) {

        WifiManager wifiManager =(WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config;
        config = getCurrentWifiConfiguration(wifiManager);
        if(null == config) return;

        try {
            //get the link properties from the wifi configuration
            Object linkProperties = getFieldObject(config, "linkProperties");
            if(null == linkProperties)return;
            //get the setHttpProxy method for LinkProperties

            Class<?> proxyPropertiesClass = Class.forName("android.net.ProxyProperties");
            Class<?>[] setHttpProxyParams = new Class[1];
            setHttpProxyParams[0] = proxyPropertiesClass;

            Class<?> lpClass = Class.forName("android.net.LinkProperties");
            Method setHttpProxy = lpClass.getDeclaredMethod("setHttpProxy",setHttpProxyParams);
            setHttpProxy.setAccessible(true);

            //pass null as the proxy
            Object[] params = new Object[1];

            params[0] = null;
            setHttpProxy.invoke(linkProperties, params);
            setEnumField(config, "NONE", "proxySettings");

            //save the config
            wifiManager.updateNetwork(config);

            wifiManager.disconnect();
            wifiManager.reconnect();
        } catch(Exception e) {
        }
    }

    /**
     * API 21设置代理
     * android.net.IpConfiguration.ProxySettings
     * {@hide}
     * */
    public static final void setHttpProxySystemProperty(String host, String port, String exclList,
                                                        Context context) {

        WifiConfiguration config;
        WifiManager wifiManager =(WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        config = getCurrentWifiConfiguration(wifiManager);
        if (exclList != null) exclList = exclList.replace(",", "|");
        if (true) Log.d("代理信息：", "setHttpProxySystemProperty :"+host+":"+port+" - "+exclList);
        if (host != null) {
            String syshost = System.setProperty("http.proxyHost", host);
            String syshost1 = System.setProperty("https.proxyHost", host);
        } else {
            System.clearProperty("http.proxyHost");
            System.clearProperty("https.proxyHost");
        }
        if (port != null) {
            System.setProperty("http.proxyPort", port);
            System.setProperty("https.proxyPort", port);
        } else {
            System.clearProperty("http.proxyPort");
            System.clearProperty("https.proxyPort");
        }
        if (exclList != null) {
            System.setProperty("http.nonProxyHosts", exclList);
            System.setProperty("https.nonProxyHosts", exclList);
        } else {
            System.clearProperty("http.nonProxyHosts");
            System.clearProperty("https.nonProxyHosts");
        }
       /* if (!Uri.EMPTY.equals(pacFileUrl)) {
            ProxySelector.setDefault(new PacProxySelector());
        } else {
            ProxySelector.setDefault(sDefaultProxySelector);
        }*/


        wifiManager.updateNetwork(config);

        wifiManager.disconnect();
        wifiManager.reconnect();
    }


    /**
     * 设置代理信息 exclList是添加不用代理的网址用的
     * */
    public void setHttpPorxySetting(Context context, String host, int port, List<String> exclList)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
            IllegalAccessException, NoSuchFieldException {
        WifiManager wifiManager =(WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config = getCurrentWifiConfiguration(wifiManager);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            mInfo = ProxyInfo.buildDirectProxy(host,port);
        }
        if (config != null){
           Class clazz = Class.forName("android.net.wifi.WifiConfiguration");
            Class parmars = Class.forName("android.net.ProxyInfo");
            Method method = clazz.getMethod("setHttpProxy",parmars);
            method.invoke(config,mInfo);
            Object mIpConfiguration = getDeclaredFieldObject(config,"mIpConfiguration");

            setEnumField(mIpConfiguration, "STATIC", "proxySettings");
            setDeclardFildObject(config,"mIpConfiguration",mIpConfiguration);
            //save the settings
            wifiManager.updateNetwork(config);
            wifiManager.disconnect();
            wifiManager.reconnect();
        }

    }
    /**
     * 取消代理设置
     * */
    public void unSetHttpProxy(Context context)
            throws ClassNotFoundException, InvocationTargetException, IllegalAccessException,
            NoSuchFieldException, NoSuchMethodException {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration configuration = getCurrentWifiConfiguration(wifiManager);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            mInfo = ProxyInfo.buildDirectProxy(null,0);
        }
        if (configuration != null){
            Class clazz = Class.forName("android.net.wifi.WifiConfiguration");
            Class parmars = Class.forName("android.net.ProxyInfo");
            Method method = clazz.getMethod("setHttpProxy",parmars);
            method.invoke(configuration,mInfo);

            Object mIpConfiguration = getDeclaredFieldObject(configuration,"mIpConfiguration");
            setEnumField(mIpConfiguration, "NONE", "proxySettings");
            setDeclardFildObject(configuration,"mIpConfiguration",mIpConfiguration);

            //save the settings
            wifiManager.updateNetwork(configuration);
            wifiManager.disconnect();
            wifiManager.reconnect();
        }
    }
}
