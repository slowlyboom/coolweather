package activity;

import java.util.ArrayList;
import java.util.List;

import com.example.coolweather.R;

import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;

import model.City;
import model.County;
import model.Province;

import db.CoolWeatherDB;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity{
	public static final int LEVEL_PROVINCE=0;
	public static final int LEVEL_CITY=1;
	public static final int LEVEL_COUNTY=2;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String> dataList=new ArrayList<String>();
	
	/*省列表*/
	private List<Province> provinceList;
	
	/*市列表*/
	private List<City> cityList;
	
	/*县列表*/
	private List<County> countyList;
	
	/*选中的省份*/
	private Province selectedProvince;
	
	/*选中的城市*/
	private City selectedCity;
	
	/*当前选中的级别*/
	private int currentLevel;
	
	private boolean isFromWeatherActivity;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		isFromWeatherActivity=getIntent().getBooleanExtra("from_weather_activity", false);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		String weatherString=prefs.getString("weather", null);
		if(weatherString!=null&&!isFromWeatherActivity){
			//有缓存时直接解析天气数据
			Intent intent=new Intent(ChooseAreaActivity.this,WeatherActivity.class);
			startActivity(intent);
			finish();
		}
		listView=(ListView)findViewById(R.id.list_view);
		titleText=(TextView)findViewById(R.id.title_text);
		adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataList);
		listView.setAdapter(adapter);
		coolWeatherDB=CoolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?>arg0,View view,int index,long arg3){
				if(currentLevel==LEVEL_PROVINCE){
					selectedProvince=provinceList.get(index);
					queryCities();
				}else if(currentLevel==LEVEL_CITY){
					selectedCity=cityList.get(index);
					queryCounties();
				}else if(currentLevel==LEVEL_COUNTY){
					String weatherId=countyList.get(index).getCountyCode();
					Intent intent=new Intent(ChooseAreaActivity.this,WeatherActivity.class);
					intent.putExtra("weather_id", weatherId);
					startActivity(intent);
					finish();
				}
			}
			
		});
		queryProvinces();   //加载省级数据
	}
	
	/*
	 * 查询全国所有的省，优先从数据库查询；如果没有查询到，再从服务器上查询。
	 * */
	
	private void queryProvinces(){
		provinceList=coolWeatherDB.loadProvinces();
		if(provinceList.size()>0){
			dataList.clear();
			for(Province province:provinceList){
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel=LEVEL_PROVINCE;
		}else{
			queryFromServer(null,"province");
		}
	}
	/*
	 * 查询选中省所包含的市，优先从数据库查询；如果没有查询到，再从服务器上查询。
	 * */
	private void queryCities(){
		cityList=coolWeatherDB.loadCitys(selectedProvince.getId());
		if(cityList.size()>0){
			dataList.clear();
			for(City city:cityList){
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel=LEVEL_CITY;
		}else{
			queryFromServer(selectedProvince.getProvinceCode(),"city");
		}
	}
	/*
	 * 查询选中市所包含的县，优先从数据库查询；如果没有查询到，再从服务器上查询。
	 * */
	private void queryCounties(){
		countyList=coolWeatherDB.loadCountys(selectedCity.getId());
		if(countyList.size()>0){
			dataList.clear();
			for(County county:countyList){
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel=LEVEL_COUNTY;
		}else{
			queryFromServer(selectedCity.getCityCode(),"county");
		}
	}
	
	/*
	 * 根据传入的代号和等级类型从服务器上查询省，市，县的数据
	 * */
	private void queryFromServer(final String code,final String type){
		String address;
		if(!TextUtils.isEmpty(code)){
			if("city".equals(type)){
				address="http://guolin.tech/api/china/"+code;
			}else{
				String pro=selectedProvince.getProvinceCode();
				address="http://guolin.tech/api/china/"+pro+"/"+code;
			}
		}else{
			address="http://guolin.tech/api/china/";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address,new HttpCallbackListener(){
			public void onFinish(String response){
				boolean result=false;
				if("province".equals(type)){
					result=Utility.handleProvincesResponse(coolWeatherDB, response);
				}else if("city".equals(type)){
					result=Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());
				}else if("county".equals(type)){
					result=Utility.handleCountiesResponse(coolWeatherDB, response, selectedCity.getId());
				}
				
				if(result){
					runOnUiThread(new Runnable(){
						public void run(){
							closeProgressDialog();
							if("province".equals(type)){
								queryProvinces();
							}else if("city".equals(type)){
								queryCities();
							}else if("county".equals(type)){
								queryCounties();
							}
						}
					});
				}
			}
			public void onError(Exception e){
				runOnUiThread(new Runnable(){
					public void run(){
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
					}
				});
			}
			
			
		});
	}
	/*
	 * 显示进度对话框
	 **/
	private void showProgressDialog(){
		if(progressDialog==null){
			progressDialog=new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	/*
	 * 关闭进度对话框
	 * */
	private void closeProgressDialog(){
		if(progressDialog!=null){
			progressDialog.dismiss();
		}
	}
	
	/*捕捉Back按键，根据当前的级别来判断返回上一级列表，还是退出*/
	public void onBackPressed(){
		if(currentLevel==LEVEL_COUNTY){
			queryCities();
		}else if (currentLevel==LEVEL_CITY){
			queryProvinces();
		}else{
			if(isFromWeatherActivity){
				Intent intent=new Intent(ChooseAreaActivity.this,WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}
}
