package com.demo.smarthome.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.smarthome.R;
import com.demo.smarthome.device.DeviceDataString;
import com.demo.smarthome.device.DeviceType;
import com.demo.smarthome.server.DeviceDataResult;
import com.demo.smarthome.server.setServerURL;
import com.demo.smarthome.service.Cfg;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.demo.smarthome.server.DeviceDataSet;

public class DeviceDataViewActivity extends Activity {

    TextView title = null;
    ListView listView;
    ListView listViewTem;
    Button buttonRefresh = null;
    int dataCount;
    String deviceID;
    String pageNo;
    String pageSize;

    String jsonResult;

    DeviceDataResult deviceData = new DeviceDataResult();

    ProgressDialog dialogView;

    Gson gson = new Gson();

    DeviceDataSet currentData = new DeviceDataSet();
    List<DeviceDataString> deviceListType;
    boolean haveTemperature = false;
    static final int GET_COUNT_SUCCEED     = 0;
    static final int GET_COUNT_ERROR       = 1;
    static final int DEVICE_ID_ERROR       = 2;
    static final int GET_CURRENT_SUCCED    = 3;
    static final int GET_CURRENT_FAIL      = 4;
    static final int DELETE_ERROR          = 5;
    static final int SERVER_CANT_CONNECT   = 8;
    static final int SERVE_EXCEPTION       = 9;

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);

            switch (msg.what) {

                case GET_COUNT_ERROR:


                    break;
                case GET_CURRENT_SUCCED:

                    dialogView.dismiss();
                    showDataList();
                    break;
                case GET_CURRENT_FAIL:
                    dialogView.dismiss();
                    Toast.makeText(DeviceDataViewActivity.this, "��ȡ����ʧ��", Toast.LENGTH_SHORT)
                            .show();
                    break;
                default:
                    break;

            }
        }

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE); // ע��˳��
        setContentView(R.layout.activity_device_data);

        title = (TextView) findViewById(R.id.titleHCHOView);
        title.setClickable(true);
        title.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                finish();
            }

        });

        deviceID = Cfg.deviceID;

        listView = (ListView) this.findViewById(R.id.dataListView);
        listViewTem = (ListView) this.findViewById(R.id.horizontalListView);

        buttonRefresh = (Button) findViewById(R.id.deviceDataRefresh);
        buttonRefresh.setOnClickListener(new refreshOnClickListener());

        //�ȴ���
        dialogView = new ProgressDialog(DeviceDataViewActivity.this);
        dialogView.setTitle("ɾ���豸��");
        dialogView.setMessage("���ڴӷ������ж�ȡ����,��ȴ�");
        //����ȴ�������ȴ�����ʧ
        dialogView.setCanceledOnTouchOutside(false);
        dialogView.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            }
        });
        dialogView.setButton(DialogInterface.BUTTON_POSITIVE,
                "��ȴ�...", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        dialogView.show();
        dialogView.getButton(DialogInterface.BUTTON_POSITIVE)
                .setEnabled(false);

        dialogView.setOnKeyListener(new DialogInterface.OnKeyListener() {
            //���η��ؼ�
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return true;
                }
                return false;
            }
        });

        new getCurrentData().start();

    }
    //�÷��������ID������Ҫ��ʾ��Щadpter,��ʪ����Ҫ������adpter����
    private void showDataList() {
        List<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
        List<HashMap<String, Object>> dataTem = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> itemTem = new HashMap<String, Object>();
        if(deviceID.equals(deviceID))
        {
            //Ϊ�ܹ���ʾ�����ַ�
            deviceListType = new DeviceType(this).getHchoMonitor();
            String temperature,hygrometer;
            //��ʾ�����¶Ⱥ�ʪ�ȵ�listView
            //��ʪ��ֵ��һ���ַ���,��Ҫ���,��ռ��λ,һ��6λ,���һ������6λ,˵��ǰ�����㲹λ
            if(currentData.getTemperature().length() > 3) {
                temperature = currentData.getTemperature().substring(0
                        ,currentData.getTemperature().length() - 3);
                hygrometer = currentData.getTemperature().substring(currentData.getTemperature().length() - 3);
                //Ϊ������,����λ�ÿո�����
                for(int i = 0;i< 6 - currentData.getTemperature().length();i++){
                    temperature = " " + temperature;
                }
                //Ϊ������,��ǰ���0���ɿո�
                byte[] byteHygrometer= hygrometer.getBytes();
                for(int i = 0;i< hygrometer.length();i++){
                    if(byteHygrometer[i] == '0'){
                        byteHygrometer[i] = ' ';
                    }else{
                        break;
                    }
                }
                hygrometer = new String(byteHygrometer);

                itemTem.put("temperature", temperature);
                itemTem.put("hygrometer", hygrometer);
            }else if(currentData.getTemperature().length() <= 3){
                itemTem.put("temperature", "  0");
                hygrometer = currentData.getTemperature();
                for(int i = 0;i< 3 - currentData.getTemperature().length();i++){
                    hygrometer = " " + hygrometer;
                }
                itemTem.put("hygrometer", currentData.getTemperature());
            }

            dataTem.add(itemTem);
            haveTemperature = true;
        }

        if(deviceListType == null) {
            return;
        }
        String tempValue;
        for (DeviceDataString dataTypeTemp : deviceListType) {
            HashMap<String, Object> item = new HashMap<String, Object>();

            if((dataTypeTemp.getType()).equals("hcho")){
                tempValue = currentData.getHcho();
            }else if((dataTypeTemp.getType()).equals("tvoc")){
                tempValue = currentData.getTvoc();
            }else if((dataTypeTemp.getType()).equals("pm2_5")){
                tempValue = currentData.getPm2_5();
            }else if((dataTypeTemp.getType()).equals("pm10")) {
                tempValue = currentData.getPm10();
            }
            else{
                return;
            }
            //Ϊ������,ȥҪ��ǰ��ֵǰ�ӿո�
            if(tempValue.length() == 1){
                tempValue = "    " + tempValue;
            }else if(tempValue.length() == 2){
                tempValue = "  " + tempValue;
            }


            item.put("name", dataTypeTemp.getName());
            item.put("value", tempValue);
            item.put("unit", dataTypeTemp.getUnit());
            data.add(item);
        }
        // ����SimpleAdapter�����������ݰ󶨵�item��ʾ�ؼ���
        SimpleAdapter adapter = new MySimpleAdapter(this, data,
                R.layout.activity_device_adapter, new String[] { "name", "value","unit"},
                new int[] { R.id.adapterTypeName, R.id.adapterValue,R.id.adapterUnit});

        // ʵ���б����ʾ
        listView.setAdapter(adapter);
        //ɾ���ָ���
        listView.setDivider(null);
        listView.setOnItemClickListener(new ItemClickListener());
        //��ʾ�����¶Ⱥ�ʪ�ȵ�listView
        if(haveTemperature){
            SimpleAdapter adapterTem = new MySimpleAdapter(this, dataTem,
                    R.layout.activity_device_adapter_tem, new String[] { "temperature", "hygrometer"},
                    new int[] { R.id.adapterValueTem, R.id.adapterValueDam});
            listViewTem.setAdapter(adapterTem);
            listViewTem.setDivider(null);
            listViewTem.setOnItemClickListener(new ItemTemClickListener());
        }
    }

    class MySimpleAdapter extends SimpleAdapter {

        public MySimpleAdapter(Context context,
                               List<? extends Map<String, ?>> data, int resource,
                               String[] from, int[] to) {
            super(context, data, resource, from, to);

        }
    }
    // ��ȡ���listView�¼�
    private final class ItemClickListener implements AdapterView.OnItemClickListener {

        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            ListView TempListView = (ListView) parent;
            HashMap<String, Object> data = (HashMap<String, Object>) TempListView
                    .getItemAtPosition(position);

            String dataName = (String) data.get("name");
            Bundle bundleData = new Bundle();

            if (dataName == null) {
                Toast.makeText(getApplicationContext(), "��ȡ��ʷ����ʧ��", Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            if(dataName.equals(getString(R.string.device_hcho_name))){
                bundleData.putString("dataName", "hcho");
            }
            else if(dataName.equals(getString(R.string.device_pm2_5_name))){
                bundleData.putString("dataName", "pm2_5");
            }else if(dataName.equals(getString(R.string.device_pm10_name))){
                bundleData.putString("dataName", "pm10");
            }else if(dataName.equals(getString(R.string.device_tvoc_name))){
                bundleData.putString("dataName", "tvoc");
            }
            else{
                Toast.makeText(getApplicationContext(), "���ʹ���", Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            Intent tempIntent = new Intent();
            tempIntent.setClass(DeviceDataViewActivity.this, historyDataActivity.class);
            tempIntent.putExtras(bundleData);
            startActivity(tempIntent);

        }
    }

    // �����ʪ���¼�
    private final class ItemTemClickListener implements AdapterView.OnItemClickListener {

        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            //������ʪ�Ȳ�֧����ʷ���ݲ�ѯ
            if(true){
                return;
            }
            ListView TempListView = (ListView) parent;
            HashMap<String, Object> data = (HashMap<String, Object>) TempListView
                    .getItemAtPosition(position);

            Bundle dataDevId = new Bundle();

            // ��ת�����ý���
            dataDevId.putString("dataName", "temperature");

            Intent tempIntent = new Intent();
            tempIntent.setClass(DeviceDataViewActivity.this, historyDataActivity.class);
            tempIntent.putExtras(dataDevId);
            startActivity(tempIntent);

        }
    }

        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_hcho_monitor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class getCurrentData extends Thread {

        @Override
        public void run() {
            Message message = new Message();
            message.what = GET_CURRENT_SUCCED;

            if(deviceID.isEmpty()) {
                message.what = DEVICE_ID_ERROR;
                handler.sendMessage(message);
                return;
            }

            String[] paramsName = {"deviceID"};
            String[] paramsValue = {deviceID};

            setServerURL regiterUser= new setServerURL();
            //��Ҫ�жϷ������Ƿ���
            if((jsonResult = regiterUser.sendParamToServer("getCurrentDeviceData", paramsName
                    , paramsValue)).isEmpty()){
                message.what = SERVER_CANT_CONNECT;
                handler.sendMessage(message);
                return;
            }
            try {
                deviceData = gson.fromJson(jsonResult, DeviceDataResult.class);
            }
            catch (Exception e){
                e.printStackTrace();
            }

            switch (Integer.parseInt(deviceData.getCode()))
            {
                case Cfg.CODE_SUCCESS:
                    if(deviceData.getRows().size() != 1)
                    {
                        message.what = GET_CURRENT_FAIL;
                        break;
                    }
                    currentData = deviceData.getRows().get(0);
                    message.what = GET_CURRENT_SUCCED;
                    break;
                default:
                    message.what = GET_CURRENT_FAIL;
                    break;
            }
            handler.sendMessage(message);
        }

    }

    /**
     * ˢ�� ��ť�����¼�
     *
     * @author Administrator
     *
     */
    class refreshOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            finish();
            Intent intent = new Intent(DeviceDataViewActivity.this, DeviceDataViewActivity.class);
            startActivity(intent);
        }
    }

}
