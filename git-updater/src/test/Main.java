package test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import fr.lightel24.gitupdater.GitUpdater;
import fr.lightel24.gitupdater.UpdateCallback;
import fr.lightel24.gitupdater.UpdateEvent;
import fr.lightel24.gitupdater.VersionCheckException;

public class Main {

	public static void main(String[] args) {
		GitUpdater up = new GitUpdater();
		up.setVersionCheckMethod(GitUpdater.DIFFERENCE_CHECK_RELEASENAME);
		try {
			
			up.setRepo("apache","airflow");
			up.asyncIsUpToDate("Airflow 1.10.10, 2020-04-09",new UpdateCallback() {
				@Override
				public void onCheckEnd(UpdateEvent ev) {
					if(ev.getMessageType().equals(UpdateEvent.CHECK_RESULT) && ev.isUpToDate()==GitUpdater.IS_UP_TO_DATE) {
						
						try {
							
							System.out.println("Programme à jour on telecharge quand meme!");
							up.downloadAsset(up.getLatestRelease().getAssets()[0], new File(up.getLatestRelease().getAssets()[0].getName()));
						} catch (VersionCheckException | IOException e) {
							e.printStackTrace();
						}
					}
				}
			});
			
		} catch (VersionCheckException e) {
			e.printStackTrace();
		}
	}

}
