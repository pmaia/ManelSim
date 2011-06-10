package ddg.model;

import ddg.kernel.JEEvent;
import ddg.kernel.JETime;

/**
 * It gathers all information about a transfer between a client and a data server or 
 * between two data servers. 
 * 
 * @author Ricardo Araujo Santos - ricardo@lsd.ufcg.edu.br
 */
public class TransferInfo {

	private final JETime time;
	private final long transactionID;
	private final long fileSize;
	long amountTransfered;
	private final JEEvent event;

	/**
	 * @param time
	 * @param transactionID
	 * @param fileSize
	 * @param event
	 */
	public TransferInfo( JETime time, long transactionID, long fileSize, JEEvent event ) {

		this.time = time;
		this.transactionID = transactionID;
		this.fileSize = fileSize;
		this.event = event;
		amountTransfered = 0;
	}

	/**
	 * @return the time
	 */
	public JETime getTime() {
		return time;
	}

	/**
	 * @return the transactionID
	 */
	public long getTransactionID() {
		return transactionID;
	}

	/**
	 * @return the fileSize
	 */
	public long getFileSize() {
		return fileSize;
	}

	public boolean isOver(long currentTransfer) {
		return fileSize - (amountTransfered + currentTransfer)<= 0;
	}

	/**
	 * @return the event
	 */
	public JEEvent getEvent() {
		return event;
	}
}
