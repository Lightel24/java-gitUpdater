package fr.lightel24.gitupdater;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* @author PETIT Gabriel
* alias Lightel24
*/

public class GitUpdater {
	
	public static final int MANIFEST_VERSION = 0;
	public static final int SPECIFIED_VERSION = 1;
	
	public static final int SEMVER_CHECK_TAGNAME = 0;
	public static final int SEMVER_CHECK_RELEASENAME = 1;
	public static final int DIFFERENCE_CHECK_TAGNAME = 2;
	public static final int DIFFERENCE_CHECK_RELEASENAME = 3;
	
	public static final int IS_UP_TO_DATE = 1;
	public static final int IS_NOT_UP_TO_DATE = -1;
	public static final int IS_SAME_AS_REMOTE = 2;
	public static final int IS_DIFFERENT_FROM_REMOTE = -2;
	
	
	private final Logger logger = LoggerFactory.getLogger(GitUpdater.class);
	private RemoteService remote;
	private int versionSource;
	private int versionCheck;
	private String versionName;
	private String ownerName;
	private String repoName;
	private GitRelease latestRelease;
	
	public GitUpdater() {
		init();
	}
	
	private void init() {
		remote = new RemoteService();
		versionSource = MANIFEST_VERSION;
		versionName = "";
		versionCheck = DIFFERENCE_CHECK_TAGNAME;
	}
	

	/*
	 * Set the version's checking method.
	 */
	public void setVersionCheckMethod(int method) {
		if(method != SEMVER_CHECK_TAGNAME && method != DIFFERENCE_CHECK_RELEASENAME && method != DIFFERENCE_CHECK_TAGNAME && method != SEMVER_CHECK_RELEASENAME ) {
			versionCheck = DIFFERENCE_CHECK_TAGNAME;
			logger.warn("Unknown method. Versioning method has been set to DIFFERENCE_CHECK_TAGNAME");
		}else {
			versionCheck = method;
		}
	}

	/*
	 * Set the source of the version
	 */
	public void setVersionSource(int vsource) {
		if(vsource!=MANIFEST_VERSION && vsource!=SPECIFIED_VERSION ) {
			versionSource = SPECIFIED_VERSION;
			logger.warn("Unknown source. Source has been set to SPECIFIED_VERSION");
		}else {
			logger.error("Method unimplemented (Has to be specified)");
			versionSource = vsource;
		}
	}
	
	/*
	 * Set the version's string representation to check.
	 */
	public void setVersionString(String name) {
		if(versionSource != SPECIFIED_VERSION) {
			logger.warn("VersionName has been set so the source is now: SPECIFIED_SOURCE");
			versionSource = SPECIFIED_VERSION;
		}
		versionName = name;
	}
	
	/*
	 * Set the repo in wich to check version
	 */
	public void setRepo(String ownerName, String repoName) {
		this.ownerName = ownerName;
		this.repoName = repoName;
	}
	
	/*
	 * Return the object representing the latest release
	 * */
	
	public GitRelease getLatestRelease() throws VersionCheckException {
		if(latestRelease==null) {
			throw new VersionCheckException("The lastest release has to be fetch before this fonction. Call forceFetchLatestRelease() or isUpToDate()");
		}else {
			return latestRelease;
		}
	}
	
	/*
	 * Force to fetch the latest release object
	 * WARNING! Not thread safe. 
	 */
	public void forceFetchLatestRelease() throws VersionCheckException {
		if(repoName!=null && ownerName!=null) {
			latestRelease = remote.fetchLatestRelease(ownerName,repoName);
		}else {
			logger.error("The repo isn't set!");
			throw new VersionCheckException("The repo informations are incomplete, call setRepo() before this");
		}
	}
	
	/*
	 * Force to fetch the latest release object in a async manner
	 */
	public void asyncForceFetchLatestRelease(UpdateCallback callback){
		new Thread() {
			@Override
			public void run() {
				boolean success = false;
				UpdateEvent ev = new UpdateEvent(UpdateEvent.OPERATION_ENDED);
				try {
					forceFetchLatestRelease();
					success=true;
				} catch (VersionCheckException e) {
					e.printStackTrace();
					ev.setVersionCheckException(e);
				}
				if(!success) {
					ev.setFailFlag(true);
				}
				callback.onCheckEnd(ev);
			}
		}.start();
	}
	
	/*
	 *Checks if the software is up to date. WARNING! Not thread safe.
	 */
	public int isUpToDate() throws VersionCheckException{
		forceFetchLatestRelease();
		int isuptodate = IS_UP_TO_DATE; //So we do not prevent from executing if the version check fails.
		switch(versionCheck) {
			case SEMVER_CHECK_TAGNAME:
				isuptodate = semverCheck(latestRelease.getTag_name());
			break;
			case SEMVER_CHECK_RELEASENAME:
				isuptodate = semverCheck(latestRelease.getName());
			break;
			case DIFFERENCE_CHECK_TAGNAME:
				isuptodate = diffCheckTagname();
			break;
			case DIFFERENCE_CHECK_RELEASENAME:
				isuptodate = diffCheckReleasename();
			break;
		}
		return isuptodate;
	}

	/*
	 *Sets the version name then
	 *Checks if the software is up to date. WARNING! Not thread safe.
	 */
	public int isUpToDate(String versionName) throws VersionCheckException{
		setVersionString(versionName);
		return isUpToDate();
	}
	
	/*
	 *Checks if the software is up to date in async manner
	 */
	public void asyncIsUpToDate(UpdateCallback callback) {
		new Thread() {
			@Override
			public void run() {
				int isuptodate = IS_UP_TO_DATE;
				boolean success = false;
				try {
					isuptodate = isUpToDate();
					success=true;
				} catch (VersionCheckException e) {
					e.printStackTrace();
				}
				UpdateEvent ev = new UpdateEvent(UpdateEvent.CHECK_RESULT, isuptodate);
				if(!success) {
					ev.setFailFlag(true);
				}
				callback.onCheckEnd(ev);
			}
		}.start();
	}
	
	/*
	 *Sets the version name then
	 *Checks if the software is up to date in async manner
	 */
	public void asyncIsUpToDate(String versionName,UpdateCallback callback) throws VersionCheckException{
		setVersionString(versionName);
		asyncIsUpToDate(callback);
	}
	
	/*
	 * Download the specified asset and write it to the specified path
	 * WARNING! Not thread safe!
	 */
	public void downloadAsset(GitAsset asset, File file) throws ClientProtocolException, IOException {
		remote.downloadAndStore(asset.getDownloadURL(), file);
	}
	
	
	/*------------VERSION VERIFICATION METHODS--------------*/
	private int diffCheckReleasename() {
		if(versionName.equalsIgnoreCase(latestRelease.getName())) {
			return IS_UP_TO_DATE;
		}else {
			return IS_NOT_UP_TO_DATE;
		}
	}

	private int diffCheckTagname() {
		if(versionName.equalsIgnoreCase(latestRelease.getTag_name())) {
			return IS_UP_TO_DATE;
		}else {
			return IS_NOT_UP_TO_DATE;
		}
	}
	
	/*Format: x.y.z. ... .b 	numbers only */
	private int semverCheck(String remotever) throws VersionCheckException {
		int value = IS_UP_TO_DATE;
		try {
			
			//Parse the versionName
			String temp[] = versionName.split("\\.");
			String temp2[] = remotever.split("\\.");
			
			if(temp.length == temp2.length) {
				
				int versionval[] = new int[temp.length];
				int releaseversionval[] = new int[temp2.length];
				
				for(int i = 0; i<temp.length;i++) {
					versionval[i] = Integer.parseInt(temp[i]);
					releaseversionval[i] = Integer.parseInt(temp2[i]);
				}
				
				int i =0;
				while(i<versionval.length && versionval[i]>=releaseversionval[i]) {
					i++;
				}
				if(i<versionval.length) {
					value = IS_NOT_UP_TO_DATE;
				}
			}else {
				value = IS_NOT_UP_TO_DATE;
			}
		}catch(NumberFormatException ex) {
			throw new VersionCheckException("The version string is not a valid semantic version format. local: " + versionName + " 	remote: " + remotever);
		}		
		return value;
	}
}
