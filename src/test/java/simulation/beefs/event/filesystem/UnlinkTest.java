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
import simulation.beefs.model.Machine;
import simulation.beefs.model.MetadataServer;
import simulation.beefs.model.ReplicatedFile;
import core.EventScheduler;
import core.EventSourceMultiplexer;
import core.Time;
import core.Time.Unit;

public class UnlinkTest {
	
	private static final Time TO_SLEEP_TIMEOUT = new Time(15*60, Unit.SECONDS);
	private static final Time TRANSITION_DURATION = new Time(2500, Unit.MILLISECONDS);
	
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
		Machine jurupoca = new Machine("jurupoca", TO_SLEEP_TIMEOUT, TRANSITION_DURATION);
		
		Set<DataServer> dataServers = new HashSet<DataServer>();
		dataServers.add(new DataServer(jurupoca));
		MetadataServer metadataServer = new MetadataServer(dataServers, "random", 0, timeToCoherence, timeToDelete);
		FileSystemClient client = new FileSystemClient(jurupoca, metadataServer, true);
		
		client.createOrOpen(filePath);
		
		replay(eventSourceMock);
		
		Unlink unlink = new Unlink(client, unlinkTime, filePath);
		unlink.process();
		
		verify(eventSourceMock);
	}
	
	@Test
	public void testUnlinkNonExistentFile() {
		Machine jurupoca = new Machine("jurupoca", TO_SLEEP_TIMEOUT, TRANSITION_DURATION);
		
		Set<DataServer> dataServers = new HashSet<DataServer>();
		dataServers.add(new DataServer(jurupoca));
		MetadataServer metadataServer = new MetadataServer(dataServers, "random", 0, timeToCoherence, timeToDelete);
		FileSystemClient client = new FileSystemClient(jurupoca, metadataServer, true );
		
		replay(eventSourceMock);
		
		Unlink unlink = new Unlink(client, unlinkTime, filePath);
		unlink.process();
		
		verify(eventSourceMock);
	}
	
	@Test
	public void testUnlinkReplicatedFile() {
		Machine jurupoca = new Machine("jurupoca", TO_SLEEP_TIMEOUT, TRANSITION_DURATION);
		Machine cherne = new Machine("cherne", TO_SLEEP_TIMEOUT, TRANSITION_DURATION);
		Machine pepino = new Machine("pepino", TO_SLEEP_TIMEOUT, TRANSITION_DURATION);
		
		Set<DataServer> dataServers = new HashSet<DataServer>();
		dataServers.add(new DataServer(jurupoca));
		dataServers.add(new DataServer(cherne));
		dataServers.add(new DataServer(pepino));
		MetadataServer metadataServer = new MetadataServer(dataServers, "random", 2, timeToCoherence, timeToDelete);
		FileSystemClient client = new FileSystemClient(jurupoca, metadataServer, true);
		
		ReplicatedFile file = client.createOrOpen(filePath);
		
		eventSourceMock.addNewEvent(new DeleteFileReplicas(unlinkTime.plus(timeToDelete), file));
		replay(eventSourceMock);
		
		Unlink unlink = new Unlink(client, unlinkTime, filePath);
		unlink.process();
		
		verify(eventSourceMock);
	}
}
