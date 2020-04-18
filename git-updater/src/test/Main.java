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
		up.setVersionCheckMethod(GitUpdater.SEMVER_CHECK_TAGNAME);
		try {
			
			up.setRepo("apache","airflow");
			up.asyncIsUpToDate("1.10.9a",new UpdateCallback() {
				@Override
				public void onCheckEnd(UpdateEvent ev) {
					if(ev.getMessageType().equals(UpdateEvent.CHECK_RESULT) && ev.isUpToDate()==GitUpdater.IS_UP_TO_DATE) {
						
						System.out.println("Programme à jour on telecharge paas");
					}else {
						System.out.println("Programme pas à jour on telecharge aps!");
					}
				}
			});
			
		} catch (VersionCheckException e) {
			e.printStackTrace();
		}
	}

}
