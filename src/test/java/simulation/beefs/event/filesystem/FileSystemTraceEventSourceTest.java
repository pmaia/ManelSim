package simulation.beefs.event.filesystem;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import simulation.beefs.model.DataServer;
import simulation.beefs.model.FileSystemClient;
import simulation.beefs.model.Machine;
import simulation.beefs.model.MetadataServer;
import simulation.beefs.util.FakeFileSystemTraceStream;
import core.Event;
import core.EventSource;
import core.Time;
import core.Time.Unit;

/**
 * 
 * @author Patrick Maia
 *
 */
public class FileSystemTraceEventSourceTest {
	
	private static final Time TO_SLEEP_TIMEOUT = new Time(15*60, Unit.SECONDS);
	private static final Time TRANSITION_DURATION = new Time(2500, Unit.MILLISECONDS);
	
	private FileSystemClient client;
	
	@Before
	public void setup() {
		Machine jurupoca = new Machine("jurupoca", TO_SLEEP_TIMEOUT, TRANSITION_DURATION);
		
		Set<DataServer> dataServers = new HashSet<DataServer>();
		dataServers.add(new DataServer(jurupoca));
		MetadataServer metadataServer = new MetadataServer(dataServers, "random", 0, Time.GENESIS, Time.GENESIS);
		client = new FileSystemClient(jurupoca, metadataServer, true);
	}

	@Test
	public void testNoEventsAreMissed() {
		InputStream eventsStream = new FakeFileSystemTraceStream(10, null, 0, 0, 0);
		EventSource eventSource = new FileSystemTraceEventSource(client, eventsStream);
		
		int eventCount = 0;
		while(eventSource.getNextEvent() != null) {
			eventCount++;
		}
		
		assertEquals(10, eventCount);
	}
	
	@Test
	public void testEventsAttributesAreOk() throws Exception {
		InputStream eventsStream = 
				new FakeFileSystemTraceStream(10, "/home/patrick/mestrado/dissertacao.txt", 1587, 15, 1024);
		EventSource eventSource = new FileSystemTraceEventSource(client, eventsStream);
		
		Event event;
		while((event = eventSource.getNextEvent()) != null) {
			Field [] fields = event.getClass().getDeclaredFields();
			for(Field field : fields) {
				field.setAccessible(true);
				if(field.getName().equals("bytesTransfered")) {
					assertEquals(15L, field.get(event));
				} else if(field.getName().equals("fileSize")) {
					assertEquals(1024L, field.get(event));
				} else if(field.getName().equals("filePath")) {
					assertEquals("/home/patrick/mestrado/dissertacao.txt", field.get(event));
				} else if(field.getName().equals("duration")) {
					assertEquals(new Time(1587, Unit.MICROSECONDS), field.get(event));
				} else if(field.getName().equals("client")) {
					assertEquals(client, field.get(event));
				}
			}
		}
	}
}
