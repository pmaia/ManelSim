package simulation.beefs.model;

import core.Time;


/**
 * 
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 *
 */
public class FileSystemClient {

	private final MetadataServer metadataServer;
	
	private final String host;
	
	public FileSystemClient(String host, MetadataServer metadataServer) {
		this.metadataServer = metadataServer;
		this.host = host;
	}
	
	public ReplicatedFile createOrOpen(String fullpath) {
		return metadataServer.createOrOpen(this, fullpath);
	}
	
	public void read(String filePath, Time begin, Time duration) {
		ReplicatedFile file = createOrOpen(filePath);
		
		DataServer primary = file.getPrimary();
		primary.reportRead(begin, duration);
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

	public String getHost() {
		return host;
	}

	public void close(String filePath) {
		metadataServer.close(filePath);		
	}

	public void delete(String filePath) {
		metadataServer.delete(filePath);		
	}

}