package activity;


import model.Forecast;
import model.Weather;

import service.AutoUpdateService;
import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;

import com.example.coolweather.R;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class WeatherActivity extends Activity implements OnClickListener{
	private ScrollView weatherLayout;
	private TextView titleCity;
	private TextView titleUpdateTIme;
	private TextView degreeText;
	private TextView weatherInfoText;
	private LinearLayout forecastLayout;
	private TextView aqiText;
	private TextView pm25Text;
	private TextView comfortText;
	private TextView carWashText;
	private TextView sportText;
	private Button switchCity;
	private Button refreshWeather;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weather);
		weatherLayout=(ScrollView)findViewById(R.id.weather_layout);
		//初始化各个控件
		titleCity=(TextView)findViewById(R.id.titile_city);
		titleUpdateTIme=(TextView)findViewById(R.id.title_update_time);
		degreeText=(TextView)findViewById(R.id.degree_text);
		weatherInfoText=(TextView)findViewById(R.id.weather_info_text);
		forecastLayout=(LinearLayout)findViewById(R.id.forecast_layout);
		aqiText=(TextView)findViewById(R.id.aqi_text);
		pm25Text=(TextView)findViewById(R.id.pm25_text);
		comfortText=(TextView)findViewById(R.id.comfort_text);
		carWashText=(TextView)findViewById(R.id.car_wash_text);
		sportText=(TextView)findViewById(R.id.sport_text);
		switchCity=(Button)findViewById(R.id.switch_city);
		refreshWeather=(Button)findViewById(R.id.refresh_weather);
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		String weatherString=prefs.getString("weather", null);
		String weatherId=getIntent().getStringExtra("weather_id");
		if(weatherString!=null&&weatherId==null){
			//有缓存时直接解析天气数据
			Weather weather=Utility.handleWeatherResponse(weatherString);
			showWeatherInfo(weather);
		}else{
			weatherLayout.setVisibility(View.INVISIBLE);
			//没缓存，请求城市天气信息
			requestWeather(weatherId);
		}
	}
	@Override
	public void onClick(View v){
		switch(v.getId()){
		case R.id.switch_city:
			Intent intent =new Intent(this,ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			break;
		case R.id.refresh_weather:
			SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
			String weatherId=prefs.getString("weatherId", null);
			if(weatherId!=null){
				requestWeather(weatherId);
			}
			break;
		default:
			break;
		}
	}
	/*
	 * 根据天气id请求城市天气信息
	 * */
	public void requestWeather(final String weatherId){
		String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=42aecd37799945469448ca7413154ac5";
		HttpUtil.sendHttpRequest(weatherUrl, new HttpCallbackListener(){

			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				final String responseText=response;
				final Weather weather=Utility.handleWeatherResponse(responseText);
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if(weather!=null&&"ok".equals(weather.status)){
							SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
							editor.putString("weather", responseText);
							editor.commit();
							showWeatherInfo(weather);
						}else{
							Toast.makeText(WeatherActivity.this, "获取天气信息失败1", Toast.LENGTH_SHORT).show();
						}
					}
					
				});
			}

			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				e.printStackTrace();
				runOnUiThread(new Runnable(){
					public void run() {
						Toast.makeText(WeatherActivity.this, "获取天气信息失败2", Toast.LENGTH_SHORT).show();
					}
				
				});
			}
		});
	}
	
	/*
	 * 处理并展示Weather实体类中的数据
	 * */
	private void showWeatherInfo(Weather weather){
		String cityName=weather.basic.cityName;
		String updateTime=weather.basic.update.updateTime.split(" ")[1];
		String degree=weather.now.temperature+"°C";
		String weatherInfo=weather.now.more.info;
		titleCity.setText(cityName);
		titleUpdateTIme.setText(updateTime);
		degreeText.setText(degree);
		weatherInfoText.setText(weatherInfo);
		forecastLayout.removeAllViews();
		for(Forecast forecast:weather.forecastList){
			View view=LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout,false);
			TextView dateText=(TextView)view.findViewById(R.id.date_text);
			TextView infoText=(TextView)view.findViewById(R.id.info_text);
			TextView maxText=(TextView)view.findViewById(R.id.max_text);
			TextView minText=(TextView)view.findViewById(R.id.min_text);
			dateText.setText(forecast.date);
			infoText.setText(forecast.more.info);
			maxText.setText(forecast.temperature.max+"°C");
			minText.setText(forecast.temperature.min+"°C");
			forecastLayout.addView(view);
		}
		if(weather.aqi!=null){
			aqiText.setText(weather.aqi.city.aqi);
			pm25Text.setText(weather.aqi.city.pm25);
		}
		String comfort="舒适度:"+weather.suggestion.comfort.info;
		String carWash="洗车指数:"+weather.suggestion.carWash.info;
		String sport="运动建议:"+weather.suggestion.sport.info;
		comfortText.setText(comfort);
		carWashText.setText(carWash);
		sportText.setText(sport);
		weatherLayout.setVisibility(View.VISIBLE);
		Intent intent=new Intent(this,AutoUpdateService.class);
		startService(intent);
	}
}
