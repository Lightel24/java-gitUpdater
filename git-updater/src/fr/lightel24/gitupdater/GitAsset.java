package fr.lightel24.gitupdater;

public class GitAsset {
	private String name;
	private String id;
	private String downloadURL;
	
	public GitAsset(String name, String id, String downloadURL) {
		this.name = name;
		this.id = id;
		this.downloadURL = downloadURL;
	}
	
	public String getName() {
		return name;
	}
	
	public String getId() {
		return id;
	}

	public String getDownloadURL() {
		return downloadURL;
	}
}
