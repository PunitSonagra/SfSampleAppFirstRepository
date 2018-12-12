package com.infinityinfoway.sfsampleapp.api;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.os.StrictMode;

import com.infinityinfoway.sfsampleapp.config.Config;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


@SuppressWarnings("deprecation")
@SuppressLint("NewApi")
public class DataConnectivity 
{
	private String aID="",str_Random="",str_Ran="",P1="",P2="",FinalP="";
	private String android_id="";
	private String strhname="",strhvalue="";
	private boolean IsAuthHandler=false;

//=====================================================================================
// Call Web Service
//=====================================================================================
	public String callLocationWebService(String str_SoapAction, String str_Envelope, boolean IsAuthHandler) {

		//-------------------------------------------------------------------------------

		try{
			char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
			StringBuilder sb = new StringBuilder();
			Random random = new Random();
			for (int i = 0; i < 8; i++)
			{
				char c = chars[random.nextInt(chars.length)];
				sb.append(c);
			}
			str_Random = sb.toString();
			aID = "";

			char[] chars2 = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
			StringBuilder sb2 = new StringBuilder();
			Random random2 = new Random();
			for (int i = 0; i < 16; i++)
			{
				char c = chars2[random2.nextInt(chars2.length)];
				sb2.append(c);
			}
			android_id = sb2.toString();
			if (aID == null || aID.isEmpty() || aID.equals(""))
			{
				aID = android_id;
			}

			str_Ran = "4082RWk8h271" + "" + str_Random;

			str_Ran = str_Ran.replaceAll("9", "j");
			str_Ran = str_Ran.replaceAll("0", "Y");
			str_Ran = str_Ran.replaceAll("5", "p");
			str_Ran = str_Ran.replaceAll("8", "B");
			str_Ran = str_Ran.replaceAll("7", "m");
			str_Ran = str_Ran.replaceAll("4", "c");
			str_Ran = str_Ran.replaceAll("1", "g");
			str_Ran = str_Ran.replaceAll("3", "F");
			str_Ran = str_Ran.replaceAll("6", "U");
			str_Ran = str_Ran.replaceAll("2", "t");

			aID = aID.replaceAll("9", "jEr");
			aID = aID.replaceAll("0", "Ysd");
			aID = aID.replaceAll("5", "pre");
			aID = aID.replaceAll("8", "Bde");
			aID = aID.replaceAll("7", "msd");
			aID = aID.replaceAll("4", "cvr");
			aID = aID.replaceAll("1", "gaw");
			aID = aID.replaceAll("3", "FsA");
			aID = aID.replaceAll("6", "UEr");
			aID = aID.replaceAll("2", "tQS");

			P1 = str_Ran;
			P2 = aID;
			FinalP = str_Ran + "" + aID;

			Date today = new Date();
			SimpleDateFormat curFormaterdate = new SimpleDateFormat("ddMMyyyy",java.util.Locale.getDefault());
			SimpleDateFormat curFormateryear = new SimpleDateFormat("dd",java.util.Locale.getDefault());
			String currentday = curFormateryear.format(today);
			int cday = Integer.parseInt(currentday);
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE, -cday);
			Date yesterday = calendar.getTime();
			SimpleDateFormat curFormateryesterday = new SimpleDateFormat("dd",java.util.Locale.getDefault());
			String current_date = curFormaterdate.format(today);
			String current_day = curFormateryear.format(today);
			String yesterday_day = curFormateryesterday.format(yesterday);
			long hname = Integer.parseInt(current_date)* Integer.parseInt(yesterday_day);
			long hvalue = Integer.parseInt(current_date)* Integer.parseInt(current_day);
			strhname = Long.toString(hname);
			strhvalue = Long.toString(hvalue);
		}catch (Exception ignored){}


//-------------------------------------------------------------------------------
		final DefaultHttpClient httpClient1 = new DefaultHttpClient();
		HttpParams params1 = httpClient1.getParams();
		HttpConnectionParams.setConnectionTimeout(params1, 180000);
		HttpConnectionParams.setSoTimeout(params1, 180000);
		HttpProtocolParams.setUseExpectContinue(httpClient1.getParams(), true);


		HttpPost httpPost1 = new HttpPost(Config.location_resStr_URL);
		String str_SOAPActURL = Config.location_str_SOAPActURL;
		httpPost1.setHeader("soapaction", str_SOAPActURL + str_SoapAction);
		httpPost1.setHeader("Content-Type", "text/xml; charset=utf-8");
		httpPost1.setHeader(strhname, strhvalue);
		httpPost1.setHeader("Private1", FinalP);


		String root = Environment.getExternalStorageDirectory().toString();
		File myDir = new File(root + "/SfSampleApp/Request/");

		myDir.mkdirs();

		String filename=str_SoapAction+".xml";
		File file = new File (myDir, filename);

		try {
			OutputStream output = new FileOutputStream(file);
			output.write(str_Envelope.getBytes());
			output.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

// -------------------------------------------------------------------------------------------------------------------
		String respString1 = "";
		try
		{
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
			HttpEntity entity1 = new StringEntity(str_Envelope);
			httpPost1.setEntity(entity1);

			long startTime = System.currentTimeMillis();
			System.out.println("<< Request : "+str_SoapAction+" >> \n"+str_Envelope);

			ResponseHandler<String> rh1 = new ResponseHandler<String>()
			{
				@Override
				public String handleResponse(HttpResponse response)throws IOException
				{
					HttpEntity entity = response.getEntity();
					StringBuilder out1 = new StringBuilder();
					byte[] b = EntityUtils.toByteArray(entity);
					out1.append(new String(b, 0, b.length));
					return out1.toString();
				}
			};
			respString1 = httpClient1.execute(httpPost1, rh1);

			long elapsedTime = System.currentTimeMillis() - startTime;
			int seconds = (int) ((elapsedTime / 1000) % 60);
			System.out.println("<< Response : "+str_SoapAction+"("+ elapsedTime +" milis,"+ seconds +" sec ) >> \n"+respString1);
		}
		catch (Exception e)
		{
			//System.out.println("exception :"+"("+elapsedTime+" milis,"+seconds+" sec ) >> " + e);
		}
		httpClient1.getConnectionManager().shutdown();
		return respString1;
	}


	
//=====================================================================================
// XML Document Builder
//=====================================================================================
	public Document XMLfromString(String str_ResponseXML)
	{
		Document doc;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try 
		{
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(str_ResponseXML));
			doc = db.parse(is);
		} 
		catch (ParserConfigurationException e)
		{
			//System.out.println("XML parse error: " + e.getMessage());
			return null;
		} 
		catch (SAXException e)
		{
			//System.out.println("Wrong XML file structure: " + e.getMessage());
			return null;
		} 
		catch (IOException e)
		{
			//System.out.println("I/O exeption: " + e.getMessage());
			return null;
		}catch (Exception e)
		{
			//System.out.println("I/O exeption: " + e.getMessage());
			return null;
		}
		return doc;
	}
}