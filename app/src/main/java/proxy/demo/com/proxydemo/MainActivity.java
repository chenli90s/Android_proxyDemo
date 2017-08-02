package proxy.demo.com.proxydemo;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.lang.reflect.InvocationTargetException;

public class MainActivity extends AppCompatActivity {


    private static WifiConnect wifiProxy;
    private EditText mIp;
    private EditText mPort;
    private CheckBox mRem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mIp = (EditText) findViewById(R.id.ip);
        mPort = (EditText) findViewById(R.id.port);
        mRem = (CheckBox) findViewById(R.id.rem);
        Button proxying = (Button) findViewById(R.id.button);
        Button disproxy = (Button) findViewById(R.id.button2);
        wifiProxy = new WifiConnect();
//        SharedPreferences.Editor editor = getSharedPreferences("lock", MODE_WORLD_WRITEABLE).edit();
        SharedPreferences read = getSharedPreferences("lock", MODE_PRIVATE);
        String ip = read.getString("ip", "");
        int port = read.getInt("port", 0);
        if (!(ip.equals("") & port == 0)){
            mIp.setText(ip);
            mPort.setText(port+"");
        }
        proxying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setWifiProxy();
                boolean click = mRem.isChecked();
                if (click){
                    SharedPreferences.Editor editor = getSharedPreferences("lock", MODE_PRIVATE).edit();
                    editor.putString("ip",mIp.getText().toString());
                    editor.putInt("port",Integer.parseInt(mPort.getText().toString()));
                    editor.commit();
                }
            }
        });
        disproxy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unSetWifiProxy();
            }
        });
    }

    private void setPorxy() {
        String ip = mIp.getText().toString();
        int port = Integer.parseInt(mPort.getText().toString());
        try {
            wifiProxy.setHttpPorxySetting(getApplicationContext(),ip,port,null);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private void setWifiProxy() {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            setPorxy();
        }else {
            String ip = mIp.getText().toString();
            int port = Integer.parseInt(mPort.getText().toString());
            wifiProxy.setWifiProxySettingsFor17And(this,ip,port,null);
        }
    }

    private void unSetWifiProxy() {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            clearPorxy();
        }else {
            wifiProxy.unsetWifiProxySettingsFor17And(this);
        }
    }

    private void clearPorxy(){
        //boolean result = setRoot();
        //Log.d("root是否成功",result+"");
        //WifiProxy.setHttpProxySystemProperty("","",null,getApplicationContext());
        try {
            wifiProxy.unSetHttpProxy(getApplicationContext());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unSetWifiProxy();
    }
}
