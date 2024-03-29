package com.demo.smarthome.activity;

import com.demo.smarthome.R;
import com.demo.smarthome.dao.ConfigDao;
import com.demo.smarthome.server.LoginServer;
import com.demo.smarthome.server.ServerReturnResult;
import com.demo.smarthome.server.setServerURL;
import com.demo.smarthome.service.Cfg;
import com.demo.smarthome.service.ConfigService;
import com.demo.smarthome.service.HttpConnectService;
import com.demo.smarthome.staticString.StringRes;
import com.demo.smarthome.tools.StrTools;
import com.demo.smarthome.tools.MD5Tools;
import com.demo.smarthome.updata.UpdataInfo;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.os.Message;
import android.util.Xml;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.util.Log;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * ��ӭ������
 * 
 * @author sl
 * 
 */
public class WelcomeActivity extends Activity {

	static final String TAG = "WelcomeActivity";
	private boolean isAutoLogin;
	static final int LOGIN_SUCCEED = 0;
	static final int LOGIN_ERROR = 1;

	static final int VERSION_HIGHEST = 2;
	static final int VERSION_UPDATA  = 3;

	static final int LONGIN_WAIT_TIME = 3000;

	static final int UPDATA_SUCCEED  = 4;
	static final int UPDATA_ERROR    = 5;

	static final int DIALOG_SHOW    = 8;

	UpdataInfo info = null;
	ConfigService dbService;

	ServerReturnResult loginResult = new ServerReturnResult();
	long startTimestamp;

	ProgressDialog installPD = null;

	Handler handler = new Handler(){
		public void handleMessage(Message msg){
			switch (msg.what){
				case LOGIN_SUCCEED:
					Intent intent = new Intent();
					Bundle bundle = new Bundle();
					bundle.putString("activity", "welcome");
					intent.putExtras(bundle);
					intent.setClass(WelcomeActivity.this, MainActivity.class);
					startActivity(intent);
					finish();
				break;
				case LOGIN_ERROR:
					post(r);
					break;
				case VERSION_HIGHEST:
					dbService = new ConfigDao(WelcomeActivity.this.getBaseContext());
					isAutoLogin = dbService.getCfgByKey(Cfg.KEY_AUTO_LOGIN).equals("true")? true : false;

					if (isAutoLogin) {
						new AutoLoginThread().start();
					} else {
						//��Ҫ���ٵȴ�3��
						long wait_time = System.currentTimeMillis() - startTimestamp;
						wait_time = (wait_time > LONGIN_WAIT_TIME)?0:LONGIN_WAIT_TIME - wait_time;
						handler.postDelayed(r, wait_time);
					}
					break;
				case VERSION_UPDATA:
					updataVersionMothod();
					break;
				case UPDATA_ERROR:
					Toast.makeText(getApplicationContext(), "�����ļ�ʧ��", Toast.LENGTH_SHORT).show();
					handler.postDelayed(r, 1000);
					break;
				case DIALOG_SHOW:
					ProgressDialog.show(WelcomeActivity.this
							,"��װ������","���ڰ�װ����,��ȴ�...",false,true);
					break;
				default:
					handler.postDelayed(r, 0);
					break;
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // ע��˳��
		setContentView(R.layout.activity_welcome);
		//��ӭ�������3��
		startTimestamp = System.currentTimeMillis();
		new CheckVersionThread().start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.welcome, menu);
		return true;
	}

	//����Ҫ���º�û���Զ���¼���,�ڻ�ӭ�������������¼����
	Runnable r = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Intent intent = new Intent();
			intent.setClass(WelcomeActivity.this, LoginActivity.class);

			startActivity(intent);
			finish();
		}
	};

	//���֮ǰѡ�����Զ���¼
	class AutoLoginThread extends Thread {

		@Override
		public void run() {
			Message message = new Message();
			message.what = LOGIN_ERROR;

			Cfg.userName = dbService.getCfgByKey(Cfg.KEY_USER_NAME);
			Cfg.userPassword = dbService.getCfgByKey(Cfg.KEY_PASS_WORD);

			if(Cfg.userName  == null || Cfg.userPassword == null) {
				return;
			}

			loginResult = LoginServer.LoginServerMethod();
			switch (Integer.parseInt(loginResult.getCode()))
			{
				case Cfg.CODE_SUCCESS:
					message.what = LOGIN_SUCCEED;
					break;
				default:
					message.what = LOGIN_ERROR;
					break;
			}
			//��Ҫ�ȴ���ӭ���漸��
			while(System.currentTimeMillis() - startTimestamp < LONGIN_WAIT_TIME){

			}
			handler.sendMessage(message);
		}
	}

	//���汾�Ƿ�����,�����������Ѹ���
	class CheckVersionThread extends Thread {
		@Override
		public void run() {
				Message msg = new Message();
				msg.what = VERSION_HIGHEST;

			try {
				//ȡ�ñ��ذ汾��
				getVersionName();
				//����Դ�ļ���ȡ������ ��ַ
				String path = "http://" + StringRes.updateXmlUrl;

				//��װ��url�Ķ���
				URL url = new URL(path);
				HttpURLConnection conn =  (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(5000);
				InputStream is =conn.getInputStream();
				info =  getUpdataInfo(is);
				if(!info.getVersion().equals(Cfg.versionNumber)) {
					msg.what = VERSION_UPDATA;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			handler.sendMessage(msg);
		}
	}
	/*
     * ��ȡ��ǰ����İ汾��
     */
	private String getVersionName()  throws Exception{
		//��ȡpackagemanager��ʵ��
		PackageManager packageManager = getPackageManager();
		//getPackageName()���㵱ǰ��İ�����0�����ǻ�ȡ�汾��Ϣ
		PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
		Cfg.versionNumber = packInfo.versionName;
		return packInfo.versionName;
	}

	/*
	 * ��pull�������������������ص�xml�ļ� (xml��װ�˰汾��)
	*/
	public static UpdataInfo getUpdataInfo(InputStream is) throws Exception{
		XmlPullParser  parser = Xml.newPullParser();
		parser.setInput(is, "utf-8");//���ý���������Դ
		int type = parser.getEventType();
		UpdataInfo info = new UpdataInfo();//ʵ��
		while(type != XmlPullParser.END_DOCUMENT ){
			switch (type) {
				case XmlPullParser.START_TAG:
					if("version".equals(parser.getName())){
						info.setVersion(parser.nextText()); //��ȡ�汾��
					}else if ("url".equals(parser.getName())){
						info.setUrl(parser.nextText()); //��ȡҪ������APK�ļ�
					}else if ("description".equals(parser.getName())){
						info.setDescription(parser.nextText()); //��ȡ���ļ�����Ϣ
					}
					break;
			}
			type = parser.next();
		}
		return info;
	}
	protected void updataVersionMothod(){
		AlertDialog.Builder builer = new AlertDialog.Builder(this) ;
		builer.setTitle("�汾����");
		builer.setMessage(info.getDescription());
		//����ȷ����ťʱ�ӷ����������� �µ�apk Ȼ��װ
		builer.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Log.i(TAG,"����apk,����");
				downLoadApk();
			}
		});
		//����ȡ����ťʱ���е�¼
		builer.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Message msg = new Message();
				msg.what = VERSION_HIGHEST;
				handler.sendMessage(msg);
			}
		});
		AlertDialog dialog = builer.create();
		dialog.setCancelable(false);
		dialog.show();
	}
	/*
     * �ӷ�����������APK
     */
	protected void downLoadApk() {

		final ProgressDialog pd;    //�������Ի���
		pd = new  ProgressDialog(this);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setMessage("�������ظ���");
		pd.show();
		new Thread(){
			@Override
			public void run() {
				Message msg = new Message();
				msg.what = UPDATA_ERROR;
				try {
					String ApkUrl = "http://" + StringRes.serverIP +":"+ StringRes.serverPort
							+info.getUrl();
					File file = getFileFromServer(ApkUrl, pd);
					pd.dismiss();
					//�����ڷ�UI�߳�����ʾdialog
					Message message = new Message();
					message.what = DIALOG_SHOW;
					handler.sendMessage(message);

					sleep(3000);
					installApk(file);

				} catch (Exception e) {
					e.printStackTrace();
					handler.sendMessage(msg);
				}

			}}.start();
	}

	//��װapk
	protected void installApk(File file) {

		Intent intent = new Intent();
		//ִ�ж���
		intent.setAction(Intent.ACTION_VIEW);
		//ִ�е���������
		intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
		startActivity(intent);
	}

	public static File getFileFromServer(String path, ProgressDialog pd) throws Exception{
		//�����ȵĻ���ʾ��ǰ��sdcard�������ֻ��ϲ����ǿ��õ�
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			URL url = new URL(path);
			HttpURLConnection conn =  (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			//��ȡ���ļ��Ĵ�С
			pd.setMax(conn.getContentLength());
			InputStream is = conn.getInputStream();
			File file = new File(Environment.getExternalStorageDirectory(), "smartBegood.apk");
			FileOutputStream fos = new FileOutputStream(file);
			BufferedInputStream bis = new BufferedInputStream(is);
			byte[] buffer = new byte[1024];
			int len ;
			int total=0;
			while((len =bis.read(buffer))!=-1){
				fos.write(buffer, 0, len);
				total+= len;
				//��ȡ��ǰ������
				pd.setProgress(total);
			}
			fos.close();
			bis.close();
			is.close();
			return file;
		}
		else{
			return null;
		}
	}
}
