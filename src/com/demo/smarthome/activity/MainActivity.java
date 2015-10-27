package com.demo.smarthome.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.demo.smarthome.R;
import com.demo.smarthome.dao.DevDao;
import com.demo.smarthome.device.Dev;
import com.demo.smarthome.iprotocol.IProtocol;
import com.demo.smarthome.protocol.MSGCMD;
import com.demo.smarthome.protocol.MSGCMDTYPE;
import com.demo.smarthome.protocol.Msg;
import com.demo.smarthome.protocol.PlProtocol;
import com.demo.smarthome.server.LoginServer;
import com.demo.smarthome.server.ServerReturnResult;
import com.demo.smarthome.server.setServerURL;
import com.demo.smarthome.service.Cfg;
import com.demo.smarthome.service.HttpConnectService;
import com.demo.smarthome.service.SocketService;
import com.demo.smarthome.service.SocketService.SocketBinder;
import com.demo.smarthome.tools.StrTools;
import com.demo.smarthome.zxing.demo.CaptureActivity;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import android.app.AlertDialog;
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
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.content.DialogInterface;

/**
 * ��������
 * 
 * @author Administrator
 * 
 */
public class MainActivity extends Activity {

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		new GetDevThread().start();
//		new GetDevListThread().start();
	}

	Button btnRefresh = null;
	Button btnAddDev = null;
	ListView listView;
	private final String TAG = "MainActivity";
	IProtocol protocol = new PlProtocol();
	Msg msg = new Msg();
	static final int GET_DEV_SUCCEED = 0;
	static final int GET_DEV_ERROR = 1;
	static final int BUTTON_DELETE = 2;
	static final int BUTTON_CONTROL = 3;
	static final int DELETE_SUCCEED = 4;
	static final int DELETE_ERROR = 5;
	String jsonResult;
	ServerReturnResult getResult = new ServerReturnResult();

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			// dataToui();
			switch (msg.what) {

			case GET_DEV_SUCCEED:
				getDevList();
				break;
			case GET_DEV_ERROR:
				// Toast.makeText(MainActivity.this, "��ȡ�豸�б�ɹ���",
				// Toast.LENGTH_SHORT).show();
//				Cfg.listDev.clear();
//				changeDevList();
				break;

			case BUTTON_DELETE:
				Toast.makeText(getApplicationContext(), "�ɹ�ɾ���豸", Toast.LENGTH_SHORT)
						.show();
				finish();
				Intent intent = new Intent(MainActivity.this, MainActivity.class);
				startActivity(intent);
				break;
			case BUTTON_CONTROL:
				// Toast.makeText(MainActivity.this,
				// "BUTTON_CONTROL:"+msg.arg1,Toast.LENGTH_SHORT).show();
				HashMap<String, Object> data = (HashMap<String, Object>) listView
						.getItemAtPosition(msg.arg1);
				String devId = (String) data.get("id");

				Log.i(TAG, "ItemClickListener devId��" + devId);
				Toast.makeText(getApplicationContext(), "ѡ���豸" + devId, Toast.LENGTH_SHORT)
						.show();
				Dev dev = getDevById(devId);
				if (dev == null) {
					Toast.makeText(getApplicationContext(), "������ѡ���豸", Toast.LENGTH_SHORT)
							.show();
					return;
				}
				// ��ת�����ý���

				//intent.setClass(MainActivity.this, DevViewActivity.class);
				Intent tempIntent = new Intent();
				tempIntent.setClass(MainActivity.this, HCHOActivity.class);


				Log.i(TAG, "ItemClickListener dev��" + dev.getId());
				// MyLog.i(TAG, "��ת�����ý���");DeleteDevThread
				startActivity(tempIntent);// ���½���
				break;

			case DELETE_SUCCEED:
				Toast.makeText(MainActivity.this, "ɾ���豸�ɹ���", Toast.LENGTH_SHORT)
						.show();
				new GetDevListThread().start();
				break;
			case DELETE_ERROR:
				Toast.makeText(MainActivity.this, "ɾ���豸ʧ�ܣ�", Toast.LENGTH_SHORT)
						.show();

				break;

			default:
				break;

			}
		}

	};

	SocketBinder socketBinder;
	SocketService socketService;
	boolean isBinderConnected = false;

	IntentFilter intentFilter = null;
	SocketIsConnectReceiver socketConnectReceiver = new SocketIsConnectReceiver();

	private class SocketIsConnectReceiver extends BroadcastReceiver {// �̳���BroadcastReceiver������
		@Override
		public void onReceive(Context context, Intent intent) {// ��дonReceive����

			if (intent.getBooleanExtra("conn", false)) {
				Log.i(TAG, "socket���ӳɹ���");
			} else {
				Log.i(TAG, "socket����ʧ�ܡ�");
			}
		}
	}

	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			Log.i(TAG, "=============onServiceConnected");
			socketBinder = (SocketBinder) service;
			socketService = socketBinder.getService();
			socketService.myMethod();

			isBinderConnected = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			Log.i(TAG, "xxxxxxxxxxxxxxxxxxxxxxxxxxxonServiceDisconnected");
			isBinderConnected = false;
			socketBinder = null;
			socketService = null;
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // ע��˳��
		setContentView(R.layout.activity_main);

		TextView title = (TextView) findViewById(R.id.titleMain);
		title.setClickable(true);
		title.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass( MainActivity.this , LoginActivity.class );
				startActivity(intent);
				finish();
			}

		});

		btnRefresh = (Button) findViewById(R.id.setupBtnRefresh);
		btnRefresh.setOnClickListener(new BtnRefreshOnClickListener());

		btnAddDev = (Button) findViewById(R.id.mainBtnAddDev);
		btnAddDev.setOnClickListener(new BtnAddDevOnClickListener());

		listView = (ListView) this.findViewById(R.id.devListView);

		new GetDevThread().start();

	}

//	private void changeDevList() {
//		List<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
//		for (Dev dev : Cfg.listDev) {
//			HashMap<String, Object> item = new HashMap<String, Object>();
//			item.put("id", dev.getId());
//			item.put("name", dev.getNickName());
//			item.put("state", dev.isOnLine() ? "����" : "������");
//			data.add(item);
//		}
//		// ����SimpleAdapter�����������ݰ󶨵�item��ʾ�ؼ���
//		SimpleAdapter adapter = new MySimpleAdapter(this, data,
//				R.layout.devitem, new String[] { "id", "name", "state" },
//				new int[] { R.id.devId, R.id.devName, R.id.devStat });
//		// ʵ���б����ʾ
//		listView.setAdapter(adapter);
//		// ɾ���ָ���
//		listView.setDivider(null);
//	}

	private void getDevList() {
		List<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();

		if(Cfg.devInfo == null) {
			return;
		}
		for (String devID : Cfg.devInfo) {
			HashMap<String, Object> item = new HashMap<String, Object>();
			item.put("id", devID);
			item.put("name", "δ����");
			data.add(item);
		}
		// ����SimpleAdapter�����������ݰ󶨵�item��ʾ�ؼ���
		SimpleAdapter adapter = new MySimpleAdapter(this, data,
				R.layout.devitem, new String[] { "id", "name"},
				new int[] { R.id.devId, R.id.devName});
		// ʵ���б����ʾ
		listView.setAdapter(adapter);
		// ɾ���ָ���
		listView.setDivider(null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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
			// Cfg.listDev =new
			// DevDao(MainActivity.this.getBaseContext()).getDevList();
			// changeDevList();
			Message message = new Message();

			Log.v("GetDevListThread", "GetDevListThread start..");

			List<Dev> listDev = HttpConnectService.getDeviceList(Cfg.userName,
					new String(Cfg.torken));

			for (Dev dev : listDev) {
				Log.v("GetDevListThread", "dev:" + dev);

			}
			if (listDev.size() > 0) {
				Cfg.listDev = listDev;
				message.what = GET_DEV_SUCCEED;
			}
			message.what = GET_DEV_SUCCEED;
			handler.sendMessage(message);
		}
	}

	class GetDevThread extends Thread {

		@Override
		public void run() {
			// Cfg.listDev =new
			// DevDao(MainActivity.this.getBaseContext()).getDevList();
			// changeDevList();
			Message message = new Message();

			LoginServer.LoginServerMethod();

			message.what = GET_DEV_SUCCEED;

			handler.sendMessage(message);
		}
	}

	/**
	 * ˢ�� ��ť�����¼�
	 * 
	 * @author Administrator
	 * 
	 */
	class BtnRefreshOnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			finish();
			Intent intent = new Intent(MainActivity.this, MainActivity.class);
			startActivity(intent);
		}
	}


	class BtnAddDevOnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClass(MainActivity.this, AddDevice.class);
			startActivity(intent);// ���½���
		}
	}

	// ��ȡ����¼�
	private final class ItemClickListener implements OnItemClickListener {

		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			ListView listView = (ListView) parent;
			HashMap<String, Object> data = (HashMap<String, Object>) listView
					.getItemAtPosition(position);
			String devId = (String) data.get("id");

			Log.i(TAG, "ItemClickListener devId��" + devId);
//			Toast.makeText(getApplicationContext(), "ѡ���豸" + devId, 0).show();
			Dev dev = getDevById(devId);
			if (dev == null) {
//				Toast.makeText(getApplicationContext(), "������ѡ���豸", 0).show();
				return;
			}
			// ��ת�����ý���
			Intent intent = new Intent();
			intent.setClass(MainActivity.this, DevViewActivity.class);

			Bundle bundle = new Bundle();
			bundle.putString("devId", dev.getId());
			intent.putExtras(bundle);

			Log.i(TAG, "ItemClickListener dev��" + dev.getId());
			// MyLog.i(TAG, "��ת�����ý���");
			startActivity(intent);// ���½���

		}
	}

	/**
	 * ͨ���豸id��ȡ�豸����
	 * 
	 * @param id
	 *            �豸id
	 * @return �豸����
	 */
	private Dev getDevById(String id) {
		if (id == null) {
			return null;
		}
		for (Dev dev : Cfg.listDev) {
			if (dev.getId().equals(id)) {
				return dev;
			}
		}
		return null;
	}

	class MySimpleAdapter extends SimpleAdapter {

		// protected static final int BUTTON_DELETE = 0;
		// protected static final int BUTTON_ADD = 0;
		public MySimpleAdapter(Context context,
				List<? extends Map<String, ?>> data, int resource,
				String[] from, int[] to) {
			super(context, data, resource, from, to);
			// TODO Auto-generated constructor stub
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			final int mPosition = position;
			convertView = super.getView(position, convertView, parent);
			ImageView buttonAdd = (ImageView) convertView
					.findViewById(R.id.devControl);// idΪ���Զ��岼���а�ť��id
			buttonAdd.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					// mHandler.obtainMessage(BUTTON_ADD, mPosition, 0)
					// .sendToTarget();

					Message message = new Message();
					message.what = BUTTON_CONTROL;
					message.arg1 = mPosition;
					handler.sendMessage(message);

				}
			});
			ImageView buttonDelete = (ImageView) convertView
					.findViewById(R.id.devDelete);
			buttonDelete.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Message message = new Message();
					message.what = BUTTON_DELETE;
					message.arg1 = mPosition;

					HashMap<String, Object> data1 = (HashMap<String, Object>) listView
							.getItemAtPosition(message.arg1);
					final String deleteDevId = (String) data1.get("id");

					Log.i(TAG, "ItemClickListener devId��" + deleteDevId);

					if (deleteDevId == null) {

						message.what = DELETE_ERROR;
						handler.sendMessage(message);

						return;
					}
					//����"ȷ��ɾ��"��ʾ��
					AlertDialog.Builder deleteAlert = new AlertDialog.Builder(MainActivity.this);
					deleteAlert.setTitle("ȷ��ɾ�����豸?");
					deleteAlert.setIcon(R.drawable.delete_alert);

					deleteAlert.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							new DelDevThread(deleteDevId).start();
							return;
						}
					});

					deleteAlert.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

							return;
						}
					});
					deleteAlert.show();
				}
			});
			return convertView;
		}

	}

	class DelDevThread extends Thread {
		String id;

		public DelDevThread(String strId) {
			id = strId;

		}

		@Override
		public void run() {

			if (id.isEmpty()) {
				return;
			}

			Message message = new Message();
			message.what = DELETE_ERROR;
			Gson gson = new Gson();

			String[] paramsName = {"userName","deviceId"};
			String[] paramsValue = {Cfg.userName,id};

			setServerURL removeUser= new setServerURL();

			jsonResult = removeUser.sendParamToServer("removeDeviceById", paramsName, paramsValue);
			try {
				getResult = gson.fromJson(jsonResult
						, com.demo.smarthome.server.ServerReturnResult.class);
			}
			catch (JsonSyntaxException e){
				e.printStackTrace();
			}


			switch (Integer.parseInt(getResult.getCode()))
			{
				case Cfg.CODE_SUCCESS:
					message.what = BUTTON_DELETE;
					break;
				default:
					message.what = DELETE_ERROR;
					break;
			}
			handler.sendMessage(message);

		}
	}
}
