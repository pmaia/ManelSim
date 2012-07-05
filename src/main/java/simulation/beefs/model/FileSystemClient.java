package simulation.beefs.model;


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