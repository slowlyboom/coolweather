package util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import model.City;
import model.County;
import model.Province;
import model.Weather;
import android.text.TextUtils;
import db.CoolWeatherDB;

/*解析和处理服务器返回的数据*/
public class Utility {
	/*
	 * 解析和处理服务器返回省级数据
	 * */
	public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB,String response){
		if(!TextUtils.isEmpty(response)){
			try{
				JSONArray allProvinces=new JSONArray(response);
				for(int i=0;i<allProvinces.length();i++){
					JSONObject provinceObject=allProvinces.getJSONObject(i);
					Province province=new Province();
					province.setProvinceName(provinceObject.getString("name"));
					province.setProvinceCode(provinceObject.getString("id"));
					coolWeatherDB.saveProvince(province);
				}
				return true;
			}catch(JSONException e){ 
				e.printStackTrace();
			}
		}
		return false;
	}
	/*
	 * 解析和处理服务器返回城市数据
	 * */
	
	public  static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB,String response,int provinceId){
		if(!TextUtils.isEmpty(response)){
			try{
				JSONArray allCites=new JSONArray(response);
				for(int i=0;i<allCites.length();i++){
					JSONObject cityObject=allCites.getJSONObject(i);
					City city=new City();
					city.setCityName(cityObject.getString("name"));
					city.setCityCode(cityObject.getString("id"));
					city.setProvinceId(provinceId);
					coolWeatherDB.saveCity(city);
				}
				return true;
			}catch(JSONException e){
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/*
	 * 解析和处理服务器返回的县级数据
	 * */
	public  static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB,String response,int cityId){
		if(!TextUtils.isEmpty(response)){
			try{
				JSONArray allCounties=new JSONArray(response);
				for(int i=0;i<allCounties.length();i++){
					JSONObject countyObject=allCounties.getJSONObject(i);
					County county=new County();
					county.setCountyName(countyObject.getString("name"));
					county.setCountyCode(countyObject.getString("weather_id"));
					county.setCityId(cityId);
					coolWeatherDB.saveCounty(county);
				}
				return true;
			}catch(JSONException e){
				e.printStackTrace();
			}
		}
		return false;
	}

	/*
	 * 将返回的JSON数据解析成Weather实体类
	 * */
	public static Weather handleWeatherResponse(String response){
		try{
			JSONObject jsonObject=new JSONObject(response);
			JSONArray jsonArray=jsonObject.getJSONArray("HeWeather");
			String weatherContent=jsonArray.getJSONObject(0).toString();
			return new Gson().fromJson(weatherContent,Weather.class);
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
}

