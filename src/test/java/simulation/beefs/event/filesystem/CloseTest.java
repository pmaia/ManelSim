package simulation.beefs.event.filesystem;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import simulation.beefs.model.DataServer;
import simulation.beefs.model.FileSystemClient;
import simulation.beefs.model.MetadataServer;
import simulation.beefs.model.ReplicatedFile;
import core.EventScheduler;
import core.EventSourceMultiplexer;
import core.Time;
import core.Time.Unit;

/**
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public class CloseTest {

	private String filePath = "/home/patrick/zerooo.txt";
	private EventSourceMultiplexer eventSourceMock;
	private Time closeTime = Time.GENESIS;
	private Time timeToCoherence = new Time(5 * 60, Unit.SECONDS);
	
	@Before
	public void setup() {
		eventSourceMock = createMock(EventSourceMultiplexer.class);
		EventScheduler.setup(Time.GENESIS, new Time(Long.MAX_VALUE, Unit.MICROSECONDS), eventSourceMock);
	}
	
	@Test
	public void testCloseNonModifiedFile() {
		Set<DataServer> dataServers = new HashSet<DataServer>();
		dataServers.add(new DataServer());
		MetadataServer metadataServer = new MetadataServer(dataServers, "random", 0, timeToCoherence);
		FileSystemClient client = new FileSystemClient("jurupoca", metadataServer );
		
		client.createOrOpen(filePath);
		
		replay(eventSourceMock);
		Close close = new Close(client, closeTime, filePath);
		close.process();
	}
	
	@Test
	public void testCloseNonModifiedFileWithReplicas() {
		Set<DataServer> dataServers = new HashSet<DataServer>();
		dataServers.add(new DataServer());
		dataServers.add(new DataServer());
		dataServers.add(new DataServer());
		MetadataServer metadataServer = new MetadataServer(dataServers, "random", 2, timeToCoherence);
		FileSystemClient client = new FileSystemClient("jurupoca", metadataServer );
		
		replay(eventSourceMock);
		Close close = new Close(client, closeTime, filePath);
		close.process();
	}
	
	@Test
	public void testCloseModifiedFileWithReplicas() {
		Set<DataServer> dataServers = new HashSet<DataServer>();
		dataServers.add(new DataServer());
		dataServers.add(new DataServer());
		dataServers.add(new DataServer());
		MetadataServer metadataServer = new MetadataServer(dataServers, "random", 2, timeToCoherence);
		FileSystemClient client = new FileSystemClient("jurupoca", metadataServer);
		
		ReplicatedFile file = client.createOrOpen(filePath);
		file.setReplicasAreConsistent(false);
		
		eventSourceMock.addNewEvent(new UpdateFileReplicas(closeTime.plus(timeToCoherence), filePath));
		replay(eventSourceMock);
		
		Close close = new Close(client, closeTime, filePath);
		close.process();
		
		verify(eventSourceMock);
	}
	
	@Test
	public void testCloseNonExistentFile() {
		Set<DataServer> dataServers = new HashSet<DataServer>();
		dataServers.add(new DataServer());
		MetadataServer metadataServer = new MetadataServer(dataServers, "random", 0, timeToCoherence);
		FileSystemClient client = new FileSystemClient("jurupoca", metadataServer );
		
		replay(eventSourceMock);
		Close close = new Close(client, closeTime, filePath);
		close.process();
	}
}
