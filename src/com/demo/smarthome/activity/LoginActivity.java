package com.demo.smarthome.activity;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;

import com.demo.smarthome.dao.ConfigDao;
import com.demo.smarthome.server.LoginServer;
import com.demo.smarthome.server.ServerReturnResult;
import com.demo.smarthome.service.ConfigService;
import com.demo.smarthome.service.HttpConnectService;
import com.demo.smarthome.service.Cfg;
import com.demo.smarthome.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.smarthome.device.Dev;
import com.demo.smarthome.server.setServerURL;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;


/**
 * ��¼��
 * 
 * @author Administrator
 * 
 */
public class LoginActivity extends Activity {
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		// if(Cfg.register){
		// try {
		// Thread.sleep(1000);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// txtName.setText(Cfg.regUserName);
		// txtPassword.setText(Cfg.regUserPass);
		// new LoginThread().start();
		// try {
		// Thread.sleep(1000);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// new LoginThread().start();
		// }
	}

	TextView title = null;

	EditText txtName = null;
	EditText txtPassword = null;

	CheckBox isAtuoLogin = null;

	String name = "";
	String password = "";
	boolean isLogin = false;
	private static final String TAG = "LoginActivity";
	ConfigService dbService;
	static final int LOGIN_SUCCEED = 0;
	static final int PASSWORD_ERROR = 1;
	static final int GET_DEV_SUCCEED = 2;
	static final int GET_DEV_ERROR = 3;
	static final int SERVER_ERROR = 7;

	ServerReturnResult loginResult = new ServerReturnResult();

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			// dataToui();
			switch (msg.what) {
			case LOGIN_SUCCEED:
//				Toast.makeText(LoginActivity.this
//						, loginResult.getMsg() +loginResult.getCode(), Toast.LENGTH_SHORT)
//						.show();
				isLogin = true;
				dbService.SaveSysCfgByKey(Cfg.KEY_USER_NAME, txtName.getText()
						.toString());
				dbService.SaveSysCfgByKey(Cfg.KEY_PASS_WORD, txtPassword
						.getText().toString());
				dbService.SaveSysCfgByKey(Cfg.KEY_AUTO_LOGIN
						, String.valueOf(isAtuoLogin.isChecked()));

				Intent intent = new Intent();
				intent.setClass(LoginActivity.this, MainActivity.class);
				startActivity(intent);// ���½���

				// ��ȡ�豸�б�
				// new GetDevListThread().start();
				break;
			case PASSWORD_ERROR:

				Toast.makeText(LoginActivity.this, "�������", Toast.LENGTH_SHORT)
						.show();
				// finish();
				break;

			case GET_DEV_SUCCEED:
				Toast.makeText(LoginActivity.this, "��ȡ�豸�б�ɹ���",
						Toast.LENGTH_SHORT).show();
				// ��ת�����ý���
				// Intent intent = new Intent();
				// intent.setClass(LoginActivity.this, MainActivity.class);
				// startActivity(intent);// ���½���
				// finish();
				break;
			case GET_DEV_ERROR:
				Toast.makeText(LoginActivity.this, "��ȡ�豸�б�ʧ�ܣ�",
						Toast.LENGTH_SHORT).show();
				break;
			case SERVER_ERROR:
				break;
			default:
				break;

			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // ע��˳��
		requestWindowFeature(Window.FEATURE_NO_TITLE); // ע��˳��
		setContentView(R.layout.activity_login);
		// getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.title);
		title = (TextView) findViewById(R.id.titlelogin);
		title.setClickable(true);
		title.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}

		});

		txtName = (EditText) findViewById(R.id.loginTxtName);
		txtPassword = (EditText) findViewById(R.id.loginTxtPassword);
		isAtuoLogin = (CheckBox) findViewById(R.id.rememberUser);


		Button btnOk = (Button) findViewById(R.id.loginBtnOk);
		btnOk.setOnClickListener(new BtnOkOnClickListener());
		Button btnReg = (Button) findViewById(R.id.loginBtnReg);
		btnReg.setOnClickListener(new BtnRegOnClickListener());
		//Button btnSetup = (Button) findViewById(R.id.loginBtnSetup);
		//btnSetup.setOnClickListener(new BtnSetupOnClickListener());
//		Button btnSmartLink = (Button) findViewById(R.id.loginBtnSmartLink);
//		btnSmartLink.setOnClickListener(new BtnSmartLinkOnClickListener());

		dbService = new ConfigDao(LoginActivity.this.getBaseContext());

		//�����ݿ���ȡ���û�������

		txtName.setText(dbService.getCfgByKey(Cfg.KEY_USER_NAME));
		txtPassword.setText(dbService.getCfgByKey(Cfg.KEY_PASS_WORD));

		if(dbService.getCfgByKey(Cfg.KEY_AUTO_LOGIN).equals("true")) {
			isAtuoLogin.setChecked(true);
		}
		else {
			isAtuoLogin.setChecked(false);
		}

		name = txtName.getText().toString();
		password = txtPassword.getText().toString();
		if (name.trim().isEmpty()) {
			// Toast.makeText(getApplicationContext(), "�������û���", 0).show();
			txtName.setFocusable(true);
			return;
		}
		if (password.trim().isEmpty()) {
			// Toast.makeText(getApplicationContext(), "����������", 0).show();
			txtPassword.setFocusable(true);
			return;
		}

		// new LoginThread().start();

		// txtName.setText("asdf");
		// txtPassword.setText("asdf");
		// if(!txtName.getText().toString().isEmpty()){
		// if(!txtPassword.getText().toString().isEmpty()){
		// // new LoginThread().start();
		// // try {
		// // Thread.sleep(1000);
		// // } catch (InterruptedException e) {
		// // // TODO Auto-generated catch block
		// // e.printStackTrace();
		// // }
		// //// if(isLogin){
		// //// return;
		// //// }
		// new LoginThread().start();
		// }
		//
		// }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	/**
	 * ��¼��ť������
	 * 
	 * @author Administrator
	 * 
	 */
	class BtnOkOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			name = txtName.getText().toString();
			password = txtPassword.getText().toString();
			if (name.trim().isEmpty()) {
				Toast.makeText(getApplicationContext(), "�������û���", Toast.LENGTH_SHORT).show();
				txtName.setFocusable(true);
				return;
			}
			if (password.trim().isEmpty()) {
				Toast.makeText(getApplicationContext(), "����������", Toast.LENGTH_SHORT).show();
				txtPassword.setFocusable(true);
				return;
			}
			Cfg.userName = name;
			Cfg.userPassword =password;
			new LoginThread().start();

		}

	}

	/**
	 * ע�ᰴť������
	 * 
	 * @author Administrator
	 * 
	 */
	class BtnRegOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {

			Intent intent = new Intent();
			intent.setClass(LoginActivity.this, RegisterActivity.class);
//			Bundle bundle = new Bundle();
//			bundle.putInt("type", 2);
//			intent.putExtras(bundle);
			startActivity(intent);
		}
	}

	/**
	 * ���ð�ť������
	 * 
	 * @author Administrator
	 * 
	 */
	class BtnSetupOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClass(LoginActivity.this, SetupDevActivity.class);
			startActivity(intent);

		}

	}

//	class BtnSmartLinkOnClickListener implements OnClickListener {
//
//		@Override
//		public void onClick(View v) {
//			Intent intent = new Intent();
//			intent.setClass(
//					LoginActivity.this,
//					com.espressif.iot.esptouch.demo_activity.EsptouchDemoActivity.class);
//			startActivity(intent);
//
//		}
//
//	}

	/**
	 * ��¼
	 * 
	 * @author Administrator
	 * 
	 */
	class LoginThread extends Thread {

		@Override
		public void run() {
			Message message = new Message();
			message.what = Cfg.REG_SUCCESS;

			if((loginResult = LoginServer.LoginServerMethod())==null) {
				message.what = SERVER_ERROR;
				handler.sendMessage(message);
				return;
			}

			switch (Integer.parseInt(loginResult.getCode()))
			{
				case Cfg.CODE_SUCCESS:
					message.what = LOGIN_SUCCEED;
					break;
				case Cfg.CODE_PWD_ERROR:
					message.what = PASSWORD_ERROR;
					break;
				case Cfg.CODE_USER_EXISTED:
					break;
				//�����������쳣
				case Cfg.CODE_EXCEPTION:
					break;
				default:
					message.what = Cfg.REG_ERROR;
					break;
			}

			handler.sendMessage(message);
		}
	}

}
