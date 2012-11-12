package simulation.beefs.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulation.beefs.event.filesystem.Read;
import simulation.beefs.event.filesystem.Write;
import core.EventScheduler;
import core.Time;
import core.Time.Unit;

/**
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public class FileSystemClient {
	
	private final Time ONE_SECOND = new Time(1, Unit.SECONDS);
	
	private static final Logger logger = LoggerFactory
			.getLogger(FileSystemClient.class);
	
	private final MetadataServer metadataServer;
	private final Machine host;
	
	private final boolean wakeOnLan;
	
	private long readsWhileClientSleeping = 0;
	private long readsWhileDataServerSleeping = 0;
	private long writesWhileClientSleeping = 0;
	private long writesWhileDataServerSleeping = 0;
	
	/**
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
			Machine primaryHost = primary.getHost();
			
			if(primaryHost.isReachable()) {
				
				logger.info("op={}" +
							"client_host{} ds_host={}" +
							"filepath={} " +
							"bytesTransfered={}" +
							"begin={} duration={}",
							new Object[] {
								"read",
								getHost().getName(), primaryHost.getName(), 
								filePath,
								bytesTransfered, 
								begin, duration}
							);
				primary.reportRead(begin, duration);
			} else if(wakeOnLan){
				primaryHost.wakeOnLan(begin);
				
				Time delta = primaryHost.getTransitionDuration().plus(ONE_SECOND);
				EventScheduler.schedule(
						new Read(this, begin.plus(delta), duration, filePath, bytesTransfered));
			} else {
				readsWhileDataServerSleeping++;
			}
		}
	}
	
	public void write(String filePath, long fileSize, long bytesTransfered, Time begin, Time duration) {
		
		if(!host.isReachable()) {
			writesWhileClientSleeping++;
		} else {
			ReplicatedFile replicatedFile = createOrOpen(filePath);
			
			DataServer primary = replicatedFile.getPrimary();
			if(primary.getHost().isReachable()) {
				replicatedFile.setSize(fileSize);
				
				logger.info("op={}" +
						"client_host{} ds_host={}" +
						"filepath={} " +
						"bytesTransfered={}" +
						"begin={} duration={}",
						new Object[] {
							"write",
							getHost().getName(), primary.getHost().getName(), 
							filePath,
							bytesTransfered, 
							begin, duration}
						);
				replicatedFile.setReplicasAreConsistent(false);

				primary.reportWrite(begin, duration);
			} else if(wakeOnLan) {
				primary.getHost().wakeOnLan(begin);
				
				Time delta = primary.getHost().getTransitionDuration().plus(ONE_SECOND);
				EventScheduler.schedule(
						new Write(this, begin.plus(delta), duration, filePath, bytesTransfered, fileSize));
			} else {
				writesWhileDataServerSleeping++;
			}
		}
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

	public long readsWhileClientSleeping() {//try to remove
		return readsWhileClientSleeping;
	}

	public long readsWhileDataServerSleeping() {//try to remove
		return readsWhileDataServerSleeping;
	}

	public long writesWhileDataServerSleeping() {//try to remove
		return writesWhileDataServerSleeping;
	}

	public long writesWhileClientSleeping() {//try to remove
		return writesWhileClientSleeping;
	}

}