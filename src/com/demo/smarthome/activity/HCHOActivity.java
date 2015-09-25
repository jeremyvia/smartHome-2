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
		import java.net.SocketTimeoutException;
		import java.net.URL;
		import java.net.HttpURLConnection;
		import java.util.concurrent.TimeoutException;

/**
 * ���ڼ�ȩ�豸ʹ��app
 *
 * @author sl
 *
 * create at 2015/09/23
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

	/**
	 * ���������뵽���ַ�������ת����infoReslut����
	 */
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
			Info.city  = decodeUnicode(Info.city);//��Ҫ��unicodeת��ut8
			temp = tempResult.split("date:");
			Info.date = temp[1].substring(0, temp[1].indexOf(","));
			temp = tempResult.split("time:");
			Info.time = temp[1].substring(0, temp[1].indexOf(","));
			temp = tempResult.split("weather:");
			Info.weather = temp[1].substring(0, temp[1].indexOf(","));
			Info.weather  = decodeUnicode(Info.weather);
			temp = tempResult.split("l_tmp:");
			Info.l_tmp = temp[1].substring(0, temp[1].indexOf(","));
			temp = tempResult.split("h_tmp:");
			Info.h_tmp = temp[1].substring(0, temp[1].indexOf(","));
			temp = tempResult.split("WD:");
			Info.WD = temp[1].substring(0, temp[1].indexOf(","));
			Info.WD  = decodeUnicode(Info.WD);//��Ҫ��unicodeת��ut8
			temp = tempResult.split("WS:");
			Info.WS = temp[1].substring(0, temp[1].indexOf(","));
			Info.WS  = decodeUnicode(Info.WS);//��Ҫ��unicodeת��ut8
			return true;
		}
		return false;
	}

	class getEnvironmentFromInternetThread extends Thread {
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
	 * �������Ӱٶ�����������Ϣ
	 *
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
			// ����apikey��HTTP header,����apikey�ǰ󶨰ٶ��˺ŵ�
			connection.setRequestProperty("apikey",Cfg.APIKEY);
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
		}
		catch (SocketTimeoutException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
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
			new getEnvironmentFromInternetThread().start();
		}
	}
	/**
	 * �������ڽ�unicodeת����utf-8
	 * @param String theString
	 *            :unicode
	 * @return String
	 *			:UTF-8
	 *
	 */
	private static String decodeUnicode(String theString) {
		char aChar;
		int len = theString.length();
		StringBuffer outBuffer = new StringBuffer(len);
		for (int x = 0; x < len;) {
			aChar = theString.charAt(x++);
			if (aChar == '\\') {
				aChar = theString.charAt(x++);
				if (aChar == 'u') {
					// Read the xxxx
					int value = 0;
					for (int i = 0; i < 4; i++) {
						aChar = theString.charAt(x++);
						switch (aChar) {
							case '0':
							case '1':
							case '2':
							case '3':
							case '4':
							case '5':
							case '6':
							case '7':
							case '8':
							case '9':
								value = (value << 4) + aChar - '0';
								break;
							case 'a':
							case 'b':
							case 'c':
							case 'd':
							case 'e':
							case 'f':
								value = (value << 4) + 10 + aChar - 'a';
								break;
							case 'A':
							case 'B':
							case 'C':
							case 'D':
							case 'E':
							case 'F':
								value = (value << 4) + 10 + aChar - 'A';
								break;
							default:
								throw new IllegalArgumentException(
										"Malformed   \\uxxxx   encoding.");
						}

					}
					outBuffer.append((char) value);
				} else {
					if (aChar == 't')
						aChar = '\t';
					else if (aChar == 'r')
						aChar = '\r';
					else if (aChar == 'n')
						aChar = '\n';
					else if (aChar == 'f')
						aChar = '\f';
					outBuffer.append(aChar);
				}
			} else
				outBuffer.append(aChar);
		}
		return outBuffer.toString();
	}
}