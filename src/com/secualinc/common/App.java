/**
 * The class of application. 
 * @author Hoang
 * @email phamvuhoang@gmail.com
 * @createDate 2016.01.09
 */

package com.secualinc.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import android.app.Application;
import android.content.Context;

public class App extends Application {
	
	private static Context context;
	static Properties properties;

	/* Keep Login Info */
	public static String loginID = null; 	
	public static String password = null;
	public static String serverUrl = null; 	
	
	/*
	 * Configuration key
	 */
	public static final String SETTINGS = "SecualConfig";
	
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Application#onCreate()
	 */
    public void onCreate(){
        super.onCreate();
        try {
        	App.context = getApplicationContext();
        	App.loadConfig();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
    }

    public static Context getAppContext() {
        return App.context;
    }
    
	/*
	 * Load configurations from configuration from file (app.properties)
	 */
	private static void loadConfig() throws Exception{
		try{
			if (properties == null ){
				InputStream is =  App.context.getAssets().open("app.config");
				if (is != null){
					properties = new Properties();
					properties.load(is);
					is.close();
				}
				else
				{
					throw new IOException();
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}
	}
	
	/*
	 * get String value from app.properties
	 */
    public static String getConfig(String key){
		try{
			loadConfig();
			return properties.getProperty(key);
		}
		catch(Exception ex){
			ex.printStackTrace();
			return "";
		}
    }
    
	/*
	 * get Int value from app.properties
	 */
    public static int getIntConfig(String key){
		try{
			loadConfig();
			return Integer.parseInt(properties.getProperty(key));
		}
		catch(Exception ex){
			ex.printStackTrace();
			return 0;
		}

    }
    
    /*
     * clear session data.
     */
	public static void clearData(){
		App.loginID = null;
	}
	
}
