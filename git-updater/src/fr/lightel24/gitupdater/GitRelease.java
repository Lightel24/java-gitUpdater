package fr.lightel24.gitupdater;

public class GitRelease {
	private String name;
	private String tag_name;
	private GitAsset[] assets;
	
	/**
	 * @param name
	 * @param tag_name
	 * @param assets
	 */
	public GitRelease(String name, String tag_name, GitAsset[] assets) {
		super();
		this.name = name;
		this.tag_name = tag_name;
		this.assets = assets;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the tag_name
	 */
	public String getTag_name() {
		return tag_name;
	}

	/**
	 * @return the assets
	 */
	public GitAsset[] getAssets() {
		return assets;
	}
	
	public String toString() {
		String message = 
				"Git release: "+name+"\rTag name: "+tag_name+"\rContains "+assets.length+" assets.";
		return message ; 
	}
	
}
