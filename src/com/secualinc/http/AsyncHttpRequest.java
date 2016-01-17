/**
 * The class base send request to server 
 * @author Pham Vu Hoang
 * @email phamvuhoang@gmail.com
 * @createDate 2016.01.09
 */

package com.secualinc.http;

import org.json.JSONObject;
import android.content.Context;
import com.loopj.android.http.AsyncHttpClient;
import cz.msebera.android.httpclient.entity.StringEntity;

public class AsyncHttpRequest {
	
	/* Constants */
	protected final String SERVER_BASE_URL = "http://zxc.cz:5000/api/v1/app/gateways";
	protected final String CONTENT_TYPE = "application/json";
	
	/* Variables */
	private String mUrl = "";
	public void setUrl(String url){
		this.mUrl = url;
	}
	public String getUrl(){
		return this.mUrl;
	}
	
	/* properties */
	private JSONObject mParams;
	public void setParams(JSONObject params){
		this.mParams = params;
	}
	public JSONObject getParams(){
		return this.mParams;
	}
	
	private StringEntity mEntity;
	private AsyncHttpClient mClient;
	
	/* Constructors */
	public AsyncHttpRequest() {
	    super();
	    this.mClient = new AsyncHttpClient();
	}
	
	public AsyncHttpRequest(String url) {
	    super();
	    this.mUrl = url;
	    this.mClient = new AsyncHttpClient();
	}
	
	public AsyncHttpRequest(String url, JSONObject params) {
	    super();
	    this.mUrl = url;
	    this.mParams = params;
	    this.mClient = new AsyncHttpClient();
	}
	
	/* Public functions */
	public boolean addParam(String key, Object val){
		if (this.mParams == null){
			this.mParams = new JSONObject();
		}
		try {
			this.mParams.put(key, val);
			this.mEntity = new StringEntity(this.mParams.toString());
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public boolean addParams(String[] key, Object[] val){
		if (this.mParams == null){
			this.mParams = new JSONObject();
		}
		try {
			for (int cnt = 0; cnt < key.length; cnt++){
				this.mParams.put(key[cnt], val[cnt]);
			}
			this.mEntity = new StringEntity(this.mParams.toString());
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public Object makeParam(String key, Object val){
		
		JSONObject rst = new JSONObject();
		
		try {
			rst.put(key, val);
		} catch (Exception e) { }
		return rst;
	}
	
	public Object makeParams(String[] key, Object[] val){
		
		JSONObject rst = new JSONObject();
		try {
			for (int cnt = 0; cnt < key.length; cnt++){
				rst.put(key[cnt], val[cnt]);
			}
		} catch (Exception e) { }
		return rst;
	}
	
	public Object putParam2Object(Object param, String key, Object val){
		JSONObject rst = new JSONObject();
		
		try {
			if (param != null){
				rst = (JSONObject) param;
			}
			rst.put(key, val);
		} catch (Exception e) {  }
		return rst;
	}
	
	public void post2Server(Context cxt, AsyncHttpResponse respone){
		this.mClient.post(cxt, this.SERVER_BASE_URL + this.mUrl, 
				this.mEntity, this.CONTENT_TYPE, respone);
		
	}
	
	public void get2Server(Context cxt, AsyncHttpResponse respone){
		this.mClient.get(cxt, this.SERVER_BASE_URL + this.mUrl, 
				this.mEntity, this.CONTENT_TYPE, respone);
		
	}
	/* Private functions */
}
