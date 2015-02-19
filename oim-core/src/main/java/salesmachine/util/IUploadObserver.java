package salesmachine.util;

public interface IUploadObserver {
	public void ReportSuccessfulUpload(String filename, String remotePath);
	public boolean IsUploaded(String filename, String remotePath);
	public void Commit();
}
