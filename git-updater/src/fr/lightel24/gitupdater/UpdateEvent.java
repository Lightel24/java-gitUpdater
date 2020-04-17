package fr.lightel24.gitupdater;

public class UpdateEvent {
	
	public static final String CHECK_RESULT = "check_result";
	public static final String OPERATION_ENDED = "end_op";
	
	private String messageType;
	private int isUpToDate;
	private boolean failFlag;
	private VersionCheckException ex;
	
	public UpdateEvent(String type, int update){
		messageType = type;
		isUpToDate = update;
		failFlag = false;
	}
	
	public UpdateEvent(String type) {
		messageType = type;
		failFlag = false;
	}

	public int isUpToDate() {
		return isUpToDate;
	}
	
	public String getMessageType() {
		return messageType;
	}

	public void setFailFlag(boolean b) {
		failFlag = b;
	}

	public boolean hasFail() {
		return failFlag;
	}

	public VersionCheckException getError() {
		return ex;
	}

	public void setVersionCheckException(VersionCheckException ex) {
		this.ex = ex;
	}	
}
