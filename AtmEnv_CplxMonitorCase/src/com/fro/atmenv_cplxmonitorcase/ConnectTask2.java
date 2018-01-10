package com.fro.atmenv_cplxmonitorcase;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import com.fro.util.FROPm25;
import com.fro.util.FROSun;
import com.fro.util.FROTemHum;
import com.fro.util.StreamUtil;

/**
 * Created by Jorble on 2016/3/4.
 */
public class ConnectTask2 extends AsyncTask<Void, Void, Void> {

	private Context context;
	private Data data;
	TextView info_tv;

	private Float sun;
	private Float tem;
	private Float hum;
	private Float pm25;

	private byte[] read_buff;

	private Socket sunSocket;
	private Socket temHumSocket;
	private Socket pm25Socket;

	private boolean CIRCLE = false;


	public ConnectTask2() {
		
	}

	/**
	 * 更新界面
	 */
	@Override
	protected void onProgressUpdate(Void... values) {
		

	}

	/**
	 * 准备
	 */
	@Override
	protected void onPreExecute() {
		
	}

	/**
	 * 子线程任务
	 * 
	 * @param params
	 * @return
	 */
	@Override
	protected Void doInBackground(Void... params) {
		// 连接
		sunSocket = getSocket(Const.SUN_IP, Const.SUN_PORT);
		temHumSocket = getSocket(Const.TEMHUM_IP, Const.TEMHUM_PORT);
		pm25Socket = getSocket(Const.PM25_IP, Const.PM25_PORT);
		// 循环读取数据
		while (CIRCLE) {
			try {
				// 如果全部连接成功
				if (sunSocket != null && temHumSocket != null && pm25Socket != null) {
					Const.statue=1;
					// if (temHumSocket!=null) {
					// 查询光照度
					StreamUtil.writeCommand(sunSocket.getOutputStream(), Const.SUN_CHK);
					Thread.sleep(Const.time / 3);
					read_buff = StreamUtil.readData(sunSocket.getInputStream());
					sun = FROSun.getData(Const.SUN_LEN, Const.SUN_NUM, read_buff);
					if (sun != null) {
						Const.sun = (int) (float) sun;
					}
					Log.i(Const.TAG, "Const.sun=" + Const.sun);

					// 查询温湿度
					StreamUtil.writeCommand(temHumSocket.getOutputStream(), Const.TEMHUM_CHK);
					Thread.sleep(Const.time / 3);
					read_buff = StreamUtil.readData(temHumSocket.getInputStream());
					tem = FROTemHum.getTemData(Const.TEMHUM_LEN, Const.TEMHUM_NUM, read_buff);
					hum = FROTemHum.getHumData(Const.TEMHUM_LEN, Const.TEMHUM_NUM, read_buff);
					if (tem != null && hum != null) {
						Const.tem = (int) (float) tem;
						Const.hum = (int) (float) hum;
					}
					Log.i(Const.TAG, "Const.tem=" + Const.tem);
					Log.i(Const.TAG, "Const.hum=" + Const.hum);

					// 查询PM2.5
					StreamUtil.writeCommand(pm25Socket.getOutputStream(), Const.PM25_CHK);
					Thread.sleep(Const.time / 3);
					read_buff = StreamUtil.readData(pm25Socket.getInputStream());
					pm25 = FROPm25.getData(Const.PM25_LEN, Const.PM25_NUM, read_buff);
					if (pm25 != null) {
						Const.pm25 = (int) (float) pm25;
					}
					Log.i(Const.TAG, "Const.pm25=" + Const.pm25);
				}else{
					Const.statue=2;
				}
				// 更新界面
				publishProgress();
				Thread.sleep(200);

			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 建立连接并返回socket，若连接失败返回null
	 * 
	 * @param ip
	 * @param port
	 * @return
	 */
	private Socket getSocket(String ip, int port) {
		Socket mSocket = new Socket();
		InetSocketAddress mSocketAddress = new InetSocketAddress(ip, port);
		// socket连接
		try {
			// 设置连接超时时间为3秒
			mSocket.connect(mSocketAddress, 3000);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 检查是否连接成功
		if (mSocket.isConnected()) {
			Log.i(Const.TAG, ip + "连接成功！");
			return mSocket;
		} else {
			Log.i(Const.TAG, ip + "连接失败！");
			return null;
		}
	}

	public void setCIRCLE(boolean cIRCLE) {
		CIRCLE = cIRCLE;
	}

	@Override
	protected void onCancelled() {
		info_tv.setTextColor(context.getResources().getColor(R.color.gray));
		info_tv.setText("请点击连接！");
	}

	/**
	 * 关闭socket
	 */
	void closeSocket() {
		try {
			if (sunSocket != null) {
				sunSocket.close();
			}
			if (temHumSocket != null) {
				temHumSocket.close();
			}
			if (pm25Socket != null) {
				pm25Socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static class Data {
		String sun = "";
		String tem = "";
		String hum = "";
		String pm25 = "";

		public String getSun() {
			return sun;
		}

		public void setSun(String sun) {
			this.sun = sun;
		}

		public String getTem() {
			return tem;
		}

		public void setTem(String tem) {
			this.tem = tem;
		}

		public String getHum() {
			return hum;
		}

		public void setHum(String hum) {
			this.hum = hum;
		}

		public String getPm25() {
			return pm25;
		}

		public void setPm25(String pm25) {
			this.pm25 = pm25;
		}

	}

	public static interface Update {
		void update();
	}
}
