package simulation.beefs.model;

import simulation.beefs.event.filesystem.Read;
import core.EventScheduler;
import core.Time;
import core.Time.Unit;


/**
 * 
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 *
 */
public class FileSystemClient {
	
	private final Time ONE_SECOND = new Time(1, Unit.SECONDS);

	private final MetadataServer metadataServer;
	
	private final Machine host;
	
	private final boolean wakeOnLan;
	
	private long readsWhileClientSleeping = 0;
	
	private long readsWhileDataServerSleeping = 0;
	
	/**
	 * 
	 * @param host the {@link Machine} in which this client is running
	 * @param metadataServer the {@link MetadaServer} common to all clients on this system
	 * @param wakeOnLan indicates if this client must or must not wakes up target servers when they are sleeping 
	 */
	public FileSystemClient(Machine host, MetadataServer metadataServer, boolean wakeOnLan) {
		this.metadataServer = metadataServer;
		this.host = host;
		this.wakeOnLan = wakeOnLan;
	}
	
	public ReplicatedFile createOrOpen(String fullpath) {
		return metadataServer.createOrOpen(this, fullpath);
	}
	
	public void read(String filePath, long bytesTransfered, Time begin, Time duration) {
		if(!host.isReachable()) {
			readsWhileClientSleeping++;
		} else {
			ReplicatedFile file = createOrOpen(filePath);
		
			DataServer primary = file.getPrimary();
			if(primary.getHost().isReachable()) {
				primary.reportRead(begin, duration);
			} else if(wakeOnLan){
				primary.getHost().wakeOnLan(begin);
				
				Time newAttemptTime = primary.getHost().getTransitionDuration().plus(ONE_SECOND);
				EventScheduler.schedule(
						new Read(this, begin.plus(newAttemptTime), duration, filePath, bytesTransfered));
			} else {
				readsWhileDataServerSleeping++;
			}
		}
	}
	
	public void write(String filePath, long fileSize, Time begin, Time duration) {
		ReplicatedFile replicatedFile = createOrOpen(filePath);
		replicatedFile.setSize(fileSize);
		replicatedFile.setReplicasAreConsistent(false);
		
		DataServer primary = replicatedFile.getPrimary();
		primary.reportWrite(begin, duration);
	}
	
	public MetadataServer getMetadataServer() {
		return metadataServer;
	}

	public Machine getHost() {
		return host;
	}

	public void close(String filePath) {
		metadataServer.close(filePath);		
	}

	public void delete(String filePath) {
		metadataServer.delete(filePath);		
	}

	public long readsWhileClientSleeping() {
		return readsWhileClientSleeping;
	}

	public long readsWhileDataServerSleeping() {
		return readsWhileDataServerSleeping;
	}

}