package db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class CoolWeatherOpenHelper extends SQLiteOpenHelper{
	/*Province��*/
	public static final String CRATE_PROVINCE="crate table Province(" +
			"id integer primary ley cutoincrement," +
			"province_name text," +
			"province_code text)";
	/*city ��*/
	public static final String CRATE_CITY="crate table City(" +
			"id integer primary ley cutoincrement," +
			"city_name text," +
			"city_code text" +
			"province_id interger)";
	/*county ��*/
	public static final String CRATE_COUNTY="crate table County(" +
			"id integer primary ley cutoincrement," +
			"county_name text," +
			"county_code text" +
			"city_id interger)";
	
	public CoolWeatherOpenHelper(Context context,String name,CursorFactory factory,int version){
		super(context,name,factory,version);
	}
	
	public void onCreate(SQLiteDatabase db){
		db.execSQL(CRATE_PROVINCE);		//����Province��
		db.execSQL(CRATE_CITY);			//����City��
		db.execSQL(CRATE_COUNTY);		//����County��
	}
	@Override
	public void onUpgrade(SQLiteDatabase db,int oldversion,int newversion){
		
	}

}
