import java.io.IOException;
import java.util.Properties;
import java.util.ArrayList;
import java.util.List;

import java.util.Scanner;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

class PastebinHelper {

//api_dev_key=1322796dd09ccbcfac63455fa3cb4699&
//api_user_key=e0dad492a63063e27622ca7c22e8e918
//endpoint=http://pastebin.com/api/api_post.php

	static String convertStreamToString(java.io.InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	public static String postString (String name, String code, Properties props) throws Exception{

		String pastebinEndpoint = props.getProperty("pastebin_endpoint");
		String pastebinApiDevKey = props.getProperty("pastebin_api_dev_key");
		String pastebinApiUserKey = props.getProperty("pastebin_api_user_key");
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(pastebinEndpoint);
		List <NameValuePair> nvps = new ArrayList <NameValuePair>();
		nvps.add(new BasicNameValuePair("api_dev_key", pastebinApiDevKey));
		nvps.add(new BasicNameValuePair("api_user_key", pastebinApiUserKey));
		nvps.add(new BasicNameValuePair("api_option", "paste"));
		nvps.add(new BasicNameValuePair("api_paste_private", "0"));
		nvps.add(new BasicNameValuePair("api_paste_expire_date", "1H"));
		nvps.add(new BasicNameValuePair("api_paste_name", name));
		nvps.add(new BasicNameValuePair("api_paste_code", code));
		httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		CloseableHttpResponse response2 = httpclient.execute(httpPost);
		String returner;
		try {
			System.out.println(response2.getStatusLine());
			System.out.println(convertStreamToString(response2.getEntity().getContent()));
			returner = convertStreamToString(response2.getEntity().getContent());
			HttpEntity entity2 = response2.getEntity();
			// do something useful with the response body
			// and ensure it is fully consumed
			EntityUtils.consume(entity2);
		} finally {
			response2.close();
		}
		return returner;
	}

/*
	public static void main(String[] args) throws Exception {
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost("http://pastebin.com/api/api_post.php");
		List <NameValuePair> nvps = new ArrayList <NameValuePair>();
		nvps.add(new BasicNameValuePair("api_dev_key", "1322796dd09ccbcfac63455fa3cb4699"));
		nvps.add(new BasicNameValuePair("api_user_key", "e0dad492a63063e27622ca7c22e8e918"));
		nvps.add(new BasicNameValuePair("api_option", "paste"));
		nvps.add(new BasicNameValuePair("api_paste_private", "0"));
		nvps.add(new BasicNameValuePair("api_paste_expire_date", "1H"));
		nvps.add(new BasicNameValuePair("api_paste_name", "DONT READ THIS"));
		nvps.add(new BasicNameValuePair("api_paste_code", "IT IS SURELY A WASTE OF TIME"));
		httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		CloseableHttpResponse response2 = httpclient.execute(httpPost);

		try {
			System.out.println(response2.getStatusLine());
			System.out.println(convertStreamToString(response2.getEntity().getContent()));
			HttpEntity entity2 = response2.getEntity();
			// do something useful with the response body
			// and ensure it is fully consumed
			EntityUtils.consume(entity2);
		} finally {
			response2.close();
		}
	}
*/
}
