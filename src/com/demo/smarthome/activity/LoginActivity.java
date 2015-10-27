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
	static final int LOGIN_ERROR = 1;
	static final int GET_DEV_SUCCEED = 2;
	static final int GET_DEV_ERROR = 3;

	ServerReturnResult loginResult = new ServerReturnResult();

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			// dataToui();
			switch (msg.what) {
			case LOGIN_SUCCEED:
				Toast.makeText(LoginActivity.this
						, loginResult.getMsg() +loginResult.getCode(), Toast.LENGTH_SHORT)
						.show();
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
				//finish();
				// ��ȡ�豸�б�
				// new GetDevListThread().start();
				break;
			case LOGIN_ERROR:
				if (Cfg.register) {
					Cfg.register = false;
					new LoginThread().start();
					return;
				}
				Toast.makeText(LoginActivity.this, "��¼ʧ�ܣ�", Toast.LENGTH_SHORT)
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
		Button btnSmartLink = (Button) findViewById(R.id.loginBtnSmartLink);
		btnSmartLink.setOnClickListener(new BtnSmartLinkOnClickListener());


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

//			Intent intent = new Intent();
//			intent.setClass(LoginActivity.this, RegisterActivity.class);// CaptureActivity
//			Bundle bundle = new Bundle();
//			bundle.putInt("type", 2);
//			intent.putExtras(bundle);
//			startActivity(intent);
			//finish();
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

	class BtnSmartLinkOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClass(
					LoginActivity.this,
					com.espressif.iot.esptouch.demo_activity.EsptouchDemoActivity.class);
			startActivity(intent);

		}

	}

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

			loginResult = LoginServer.LoginServerMethod();

			switch (Integer.parseInt(loginResult.getCode()))
			{
				case Cfg.CODE_SUCCESS:
					message.what = Cfg.REG_SUCCESS;
					break;
				case Cfg.CODE_PWD_ERROR:
					message.what = Cfg.REG_PWD_ERROR;
					break;
				case Cfg.CODE_USER_EXISTED:
					message.what = Cfg.REG_USER_EXISTED;
					break;
				//�����������쳣
				case Cfg.CODE_EXCEPTION:
					message.what = Cfg.REG_EXCEPTION;
					break;
				default:
					message.what = Cfg.REG_ERROR;
					break;
			}

			handler.sendMessage(message);
		}
	}

	/**
	 * ����豸�б��߳�
	 * 
	 * @author Administrator
	 * 
	 */
	class GetDevListThread extends Thread {

		@Override
		public void run() {
			Message message = new Message();
			message.what = GET_DEV_ERROR;

			Log.v("GetDevListThread", "GetDevListThread start..");

			List<Dev> listDev = HttpConnectService.getDeviceList(Cfg.userName,
					new String(Cfg.torken));

			Cfg.listDev = listDev;
			for (Dev dev : listDev) {
				Log.v("GetDevListThread", "dev:" + dev);

			}
			if (listDev.size() > 0) {
				message.what = GET_DEV_SUCCEED;
			}
			handler.sendMessage(message);
		}
	}

	class UDPThread extends Thread {

		public String echo(String msg) {
			return " adn echo:" + msg;
		}

		public void run() {
			int port = 2468;
			// DatagramChannel channel = null;
			final int MAX_SIZE = 1024;

			byte[] buf = new byte[1024];
			DatagramPacket dp = new DatagramPacket(buf, 1024);
			// try {
			// channel = DatagramChannel.open();
			// } catch (IOException e1) {
			// // TODO Auto-generated catch block
			// e1.printStackTrace();
			// }
			InetAddress local = null;
			DatagramSocket socket = null;
			try {
				local = InetAddress.getByName("192.168.1.88"); // ��������
																// Cfg.DEV_UDP_IPADDR
				// local = InetAddress.getLocalHost(); // ��������
				System.out.println("local:" + local);
				socket = new DatagramSocket(null);
			} catch (SocketException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			SocketAddress localAddr = new InetSocketAddress(port);
			try {
				socket.setReuseAddress(true);
				socket.bind(new InetSocketAddress(port));
				// socket.bind(localAddr);
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("UDP����������");

			ByteBuffer receiveBuffer = ByteBuffer.allocate(MAX_SIZE);

			String msg = "android. ";
			DatagramPacket dPacket = new DatagramPacket(msg.getBytes(),
					msg.length(), local, Cfg.DEV_UDP_PORT);
			try {
				socket.send(dPacket);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			while (true) {
				try {
					// ��������Ϊ�㲥
					socket.setBroadcast(true);
					// socket.send(dPacket);

					socket.receive(dp);
					String strInfo = new String(dp.getData(), 0, dp.getLength());
					System.out.println(strInfo);

					// receiveBuffer.clear();
					// InetSocketAddress client = (InetSocketAddress) channel
					// .receive(receiveBuffer);
					// // ������������һ��EchoClient�����ݱ�
					// receiveBuffer.flip();
					// String msg =
					// Charset.forName("utf8").decode(receiveBuffer)
					// .toString();
					// System.out.println(client.getAddress() + ":" +
					// client.getPort()
					// + ">" + msg);
					// channel.send(ByteBuffer.wrap(echo(msg).getBytes()),
					// client);
					// // ��EchoClient�ظ�һ�����ݱ�
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	class UDPReceiveThread extends Thread {
		public String echo(String msg) {
			return "and echo:" + msg;
		}

		public void run() {
			Log.v("UDPReceiveThread", "run start..");
			// ���յ��ֽڴ�С���ͻ��˷��͵����ݲ��ܳ���MAX_UDP_DATAGRAM_LEN
			byte[] lMsg = new byte[1024];
			// ʵ����һ��DatagramPacket��
			DatagramPacket dp = new DatagramPacket(lMsg, lMsg.length);
			// �½�һ��DatagramSocket��
			DatagramSocket ds = null;
			try {
				// UDP�����������Ķ˿�
				// ��������Ϊ�㲥
				// ds.setBroadcast(true);
				InetAddress local = null;
				try {
					local = InetAddress.getByName(Cfg.DEV_UDP_IPADDR); // ��������
					ds = new DatagramSocket(); // ע��˴�Ҫ���������ļ�������Ȩ��,�������Ȩ�޲�����쳣
					// local = InetAddress.getLocalHost(); // ��������
					System.out.println("local:" + local);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				String msg = "android hello.";
				int msg_len = msg == null ? 0 : msg.getBytes().length;
				DatagramPacket dPacket = new DatagramPacket(msg.getBytes(),
						msg_len, local, Cfg.DEV_UDP_PORT);
				ds.setBroadcast(true);
				ds.send(dPacket);

				Log.v("UDPReceiveThread", "run 1..");
				ds = new DatagramSocket(2468);
				sleep(2000);
				// ׼����������
				Log.v("UDPReceiveThread", "run 2..");
				ds.receive(dp);
				Log.v("UDPReceiveThread", "run 3..");
				String strInfo = new String(dp.getData(), 0, dp.getLength());
				System.out.println(strInfo);
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				// ���ds����Ϊ�գ���ر�ds����
				if (ds != null) {
					ds.close();
				}
			}

			Log.v("UDPReceiveThread", "run end..");
		}
	}

}
