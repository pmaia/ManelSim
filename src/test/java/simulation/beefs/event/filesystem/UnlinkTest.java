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

public class UnlinkTest {
	private String filePath = "/home/patrick/zerooo.txt";
	private EventSourceMultiplexer eventSourceMock;
	private Time unlinkTime = Time.GENESIS;
	private Time timeToCoherence = new Time(5 * 60, Unit.SECONDS);
	private Time timeToDelete = new Time(5 * 60, Unit.SECONDS);
	
	@Before
	public void setup() {
		eventSourceMock = createMock(EventSourceMultiplexer.class);
		EventScheduler.setup(Time.GENESIS, new Time(Long.MAX_VALUE, Unit.MICROSECONDS), eventSourceMock);
	}
	
	@Test
	public void testUnlinkNonReplicatedFile() {
		Set<DataServer> dataServers = new HashSet<DataServer>();
		dataServers.add(new DataServer());
		MetadataServer metadataServer = new MetadataServer(dataServers, "random", 0, timeToCoherence, timeToDelete);
		FileSystemClient client = new FileSystemClient("jurupoca", metadataServer );
		
		client.createOrOpen(filePath);
		
		replay(eventSourceMock);
		
		Unlink unlink = new Unlink(client, unlinkTime, filePath);
		unlink.process();
		
		verify(eventSourceMock);
	}
	
	@Test
	public void testUnlinkNonExistentFile() {
		Set<DataServer> dataServers = new HashSet<DataServer>();
		dataServers.add(new DataServer());
		MetadataServer metadataServer = new MetadataServer(dataServers, "random", 0, timeToCoherence, timeToDelete);
		FileSystemClient client = new FileSystemClient("jurupoca", metadataServer );
		
		replay(eventSourceMock);
		
		Unlink unlink = new Unlink(client, unlinkTime, filePath);
		unlink.process();
		
		verify(eventSourceMock);
	}
	
	@Test
	public void testUnlinkReplicatedFile() {
		Set<DataServer> dataServers = new HashSet<DataServer>();
		dataServers.add(new DataServer());
		dataServers.add(new DataServer());
		dataServers.add(new DataServer());
		MetadataServer metadataServer = new MetadataServer(dataServers, "random", 2, timeToCoherence, timeToDelete);
		FileSystemClient client = new FileSystemClient("jurupoca", metadataServer );
		
		ReplicatedFile file = client.createOrOpen(filePath);
		
		eventSourceMock.addNewEvent(new DeleteFileReplicas(unlinkTime.plus(timeToDelete), file));
		replay(eventSourceMock);
		
		Unlink unlink = new Unlink(client, unlinkTime, filePath);
		unlink.process();
		
		verify(eventSourceMock);
	}
}
