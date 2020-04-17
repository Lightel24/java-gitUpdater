package fr.lightel24.gitupdater;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteService {
	private final Logger logger = LoggerFactory.getLogger(GitUpdater.class);
	private String ownerName;
	private String repoName;
	
	public GitRelease fetchLatestRelease(String ownerName, String repoName) {
		this.ownerName = ownerName;
		this.repoName = repoName;
		String data = apiRequest("https://api.github.com/repos/"+ownerName+"/"+repoName+"/releases/latest");
		
		return new GitRelease(getStringAttributeValue("name",data), getStringAttributeValue("tag_name",data), parseAssets(data));
	}
	
	public void downloadAndStore(String download,File file) throws ClientProtocolException, IOException {
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(download);
		HttpResponse response = client.execute(request);
		   // Get the file
		   BufferedInputStream reader = new BufferedInputStream(response.getEntity().getContent());
		   BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(file));
		   int Byte;
		   while((Byte = reader.read()) != -1) writer.write(Byte);
		   reader.close();
		   writer.close();
	}
	
	private String apiRequest(String url) {
		StringBuilder data = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		HttpResponse response;
		try {
			response = client.execute(request);
		   // Get the response
		   BufferedReader rd = new BufferedReader
		       (new InputStreamReader(
		       response.getEntity().getContent()));
		   String line = "";
		   while ((line = rd.readLine()) != null) {
			   data.append(line);
		   }
		   
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data.toString();
	}
	
	
	/*
	 * Return a GitAsset array wich contains informations about the assets to download.
	 * Requires the whole data received from the server as it sorts information itself.
	 */
	private GitAsset[] parseAssets(String data) {
		int start = data.indexOf("\"assets\":")+8;
		String temps = data.substring(data.indexOf("[",start)+1, data.indexOf("]",start+3));
		String[] separation = temps.split("\\},\\{");
		GitAsset[] assets = new GitAsset[separation.length];
		for(int i=0; i<separation.length;i++) {
			String name = getStringAttributeValue("name",separation[i]);
			String id = getIntAttributeValue("id",separation[i]);
			
			/*On récupere le lien de telechargement*/
			String assetData = apiRequest("https://api.github.com/repos/"+ownerName+"/"+repoName+"/releases/assets/"+id);
			String downloadUrl = getStringAttributeValue("browser_download_url",assetData);
			assets[i] = new GitAsset(name,id,downloadUrl);
		}
		return assets;
	}
	
	private String getIntAttributeValue(String attribute, String data) {
		int start = data.indexOf("\""+attribute+"\"")+attribute.length()+3;
		return data.substring(start, data.indexOf(",",start));
	}

	private String getStringAttributeValue(String attribute,String data) {
		int start = data.indexOf("\""+attribute+"\"")+attribute.length()+4;
		return data.substring(start, data.indexOf("\"",start));
	}
}
