package com.demo.smarthome.activity;

import com.demo.smarthome.R;
import com.demo.smarthome.activity.ScanActivity.StartUDPThread;
import com.demo.smarthome.activity.ScanActivity.UDPThread;
import com.demo.smarthome.device.Dev;
import com.demo.smarthome.iprotocol.IProtocol;
import com.demo.smarthome.protocol.MSGCMD;
import com.demo.smarthome.protocol.MSGCMDTYPE;
import com.demo.smarthome.protocol.Msg;
import com.demo.smarthome.protocol.PlProtocol;
import com.demo.smarthome.service.Cfg;
import com.demo.smarthome.service.SocketService;
import com.demo.smarthome.service.SocketService.SocketBinder;
import com.demo.smarthome.tools.IpTools;
import com.demo.smarthome.tools.StrTools;
import com.demo.smarthome.view.SlipButton;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.URL;
import java.net.HttpURLConnection;
/**
 * �豸��ʾ��
 * 
 * @author Administrator
 * 
 */
public class HCHOActivity extends Activity {

	String httpArg = "cityname=�ϲ�";
	infoReslut Info = new infoReslut();

	protected class infoReslut{
		public  String errNum = "0";
		public  String errMsg = "fail";
		public  String city = "";//����������
		public  String date = "";//��ǰ����
		public  String time = "";//�¶ȷ���ʱ��ʱ��
		public  String weather = "";//�������
		public  String l_tmp = "";//�����������
		public  String h_tmp = "";//�����������
		public  String WD = "";//����
		public  String WS = "";//����
		//protected static String sunrise = "";//�ճ�ʱ��
		//protected static String sunset = "";//����ʱ��
	}

	protected boolean toInfo(String jsonResult){
		final String tempResult = jsonResult.replaceAll("\"","");//ȥ������˫����
		String temp[];
		temp = tempResult.split("errNum:");
		Info.errNum = temp[1].substring(0, 1);
		temp = tempResult.split("errMsg:");
		Info.errMsg = temp[1].substring(0, temp[1].indexOf(","));
		if(Info.errMsg.equals("success")) {
			temp = tempResult.split("city:");
			Info.city = temp[1].substring(0, temp[1].indexOf(","));
			temp = tempResult.split("date:");
			Info.date = temp[1].substring(0, temp[1].indexOf(","));
			temp = tempResult.split("time:");
			Info.time = temp[1].substring(0, temp[1].indexOf(","));
			temp = tempResult.split("weather:");
			Info.weather = temp[1].substring(0, temp[1].indexOf(","));
			//Info.weather  = new String(Info.weather.getBytes("UTF-8"), "utf-8");
			temp = tempResult.split("l_tmp:");
			Info.l_tmp = temp[1].substring(0, temp[1].indexOf(","));
			temp = tempResult.split("h_tmp:");
			Info.h_tmp = temp[1].substring(0, temp[1].indexOf(","));
			temp = tempResult.split("WD:");
			Info.WD = temp[1].substring(0, temp[1].indexOf(","));
			temp = tempResult.split("WS:");
			Info.WS = temp[1].substring(0, temp[1].indexOf(","));
			return true;
		}
		return false;
	}
	class getEnviromentFromInternetThread extends Thread {
		@Override
		public void run() {
			final String jsonResult = requestAPI(Cfg.WEATHER_INFORMATION, httpArg);
			if (toInfo(jsonResult)) {
				TextView infoRsult = (TextView) findViewById(R.id.enviInfoTextView);
				Log.i("hcho", "��������" + Info.weather + "��������£�"
						+ Info.h_tmp + "��������£�" + Info.l_tmp);
				infoRsult.setText("��������" + Info.weather + "��������£�"
						+ Info.h_tmp + "��������£�" + Info.l_tmp);
				//infoRsult.setTextSize(10);
			}
		}
	}
	/**
	 * @param urlAll
	 *            :����ӿ�
	 * @param httpArg
	 *            :����
	 * @return ���ؽ��
	 */
	public static String requestAPI(String httpUrl, String httpArg) {
		String result = null;
		StringBuffer sbf = new StringBuffer();
		httpUrl = httpUrl + "?" + httpArg;

		try {
			URL url = new URL(httpUrl);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(10000);
			// ����apikey��HTTP header
			connection.setRequestProperty("apikey",  "63cb0c1770c622f9287d62868c079989");
			connection.connect();
			InputStream is = connection.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String strRead = null;
			while ((strRead = reader.readLine()) != null) {
				sbf.append(strRead);
				sbf.append("\r\n");
			}

			reader.close();
			result = sbf.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // ע��˳��
		setContentView(R.layout.activity_hcho_dev_view);
		TextView title = (TextView) findViewById(R.id.titleHCHOView);
		title.setClickable(true);
		title.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}

		});
	Button getEnviroment = (Button) findViewById(R.id.getEnvironment);
	getEnviroment.setOnClickListener(new getEnviromentListener());
	}

	class getEnviromentListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			TextView environmentInfo = (TextView)findViewById(R.id.enviInfoTextView);
			environmentInfo.setText("���Եȣ���Ϣ���ڻ�ȡ��");
			new getEnviromentFromInternetThread().start();
		}
	}
}