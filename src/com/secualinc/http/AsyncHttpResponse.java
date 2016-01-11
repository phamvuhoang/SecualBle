/**
 * The class base receive response from server. 
 * @author Hoang
 * @email phamvuhoang@gmail.com
 * @createDate 2016.01.09
 */

package com.secualinc.http;

import com.loopj.android.http.AsyncHttpResponseHandler;
import cz.msebera.android.httpclient.Header;

public class AsyncHttpResponse extends AsyncHttpResponseHandler{
	
	private int mStatus;
	public int getStatus(){
		return mStatus;
	}
	public void setStatus(int status){
		this.mStatus = status;
	}
	
	private Header[] mHeaders;
	public Header[] getHeader(){
		return mHeaders;
	}
	public void setHeader(Header[] headers){
		this.mHeaders = headers;
	}
	
	private byte[] mData;
	public byte[] getData(){
		return mData;
	}
	public void setData(byte[] data){
		this.mData = data;
	}
	
    @Override
    public void onFinish() {
    	super.onFinish();
    	
    }

    @Override
    public void onStart() {
    	super.onStart();
    }

	@Override
	public void onFailure(int status, Header[] headers, byte[] data, Throwable arg3) {
		super.onStart();
    	this.setStatus(status);
    	this.setHeader(headers);
    	this.setData(data);
	}

	@Override
	public void onSuccess(int status, Header[] headers, byte[] data) {
		super.onStart();
		this.setStatus(status);
    	this.setHeader(headers);
    	this.setData(data);
	}

}
