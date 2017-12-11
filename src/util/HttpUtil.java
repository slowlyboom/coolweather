package util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.DownloadManager.Request;


public class HttpUtil {
	public static void sendHttpRequest(final String address,final HttpCallbackListener listener){
		 new Thread(new Runnable(){
			 public void run(){
				 HttpURLConnection connection=null;
				 try{
					 URL url=new URL(address);
					 connection=(HttpURLConnection)url.openConnection();
					 connection.setRequestMethod("GET");
					 connection.setRequestProperty("contentType","utf-8");
					 connection.setConnectTimeout(8000);
					 connection.setReadTimeout(8000);
					 InputStream in=connection.getInputStream();
					 BufferedReader reader=new BufferedReader(new InputStreamReader(in,"utf-8"));
					 StringBuilder respone=new StringBuilder();
					 String line;
					 while((line=reader.readLine())!=null){
						 respone.append(line);
					 }
					 if(listener!=null){
						 listener.onFinish(respone.toString());
					 }
				 }catch(Exception e){
					 if(listener!=null){
						 listener.onError(e);
					 }
				 }finally{
					 if(connection!=null){
						 connection.disconnect();
					 }
				 }
			 }
		 }).start();
	 }
}


