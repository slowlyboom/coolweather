package service;

import reciever.AutoUpdateReceiver;
import model.Weather;
import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;
import activity.WeatherActivity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class AutoUpdateService extends Service{
	public IBinder onBind(Intent intent){
		return null;
	}
	public int onStartCommand(Intent intent,int flags,int startId){
		new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				updateWeather();
			}
			
		}).start();
		AlarmManager manager=(AlarmManager)getSystemService(ALARM_SERVICE);
		int anHour=8*60*60*1000;
		long triggerAtTime=SystemClock.elapsedRealtime()+anHour;
		Intent i=new Intent(this,AutoUpdateReceiver.class);
		PendingIntent pi=PendingIntent.getBroadcast(this, 0,i,0);
		manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
		return super.onStartCommand(intent, flags, startId);
	}
	
	/*
	 * 更新天气信息
	 * */
	
	private void updateWeather(){
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		String weatherString=prefs.getString("weather", null);
		if(weatherString!=null){
			Weather weather=Utility.handleWeatherResponse(weatherString);
			String weatherId=weather.basic.weatherId;
			String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=42aecd37799945469448ca7413154ac5";
		
			HttpUtil.sendHttpRequest(weatherUrl,new HttpCallbackListener(){
	
				@Override
				public void onFinish(String response) {
					// TODO Auto-generated method stub
					final String responseText=response;
					final Weather weather=Utility.handleWeatherResponse(responseText);
					
					if(weather!=null&&"ok".equals(weather.status)){
						SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
						editor.putString("weather", responseText);
						editor.commit();
						
					}
				}
				public void onError(Exception e) {
					// TODO Auto-generated method stub
					e.printStackTrace();
					
				}
			});
		}
			
	}
}
