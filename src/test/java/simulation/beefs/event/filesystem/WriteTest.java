package simulation.beefs.event.filesystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import simulation.beefs.model.DataServer;
import simulation.beefs.model.FileSystemClient;
import simulation.beefs.model.Machine;
import simulation.beefs.model.MetadataServer;
import simulation.beefs.model.ReplicatedFile;
import core.Time;
import core.Time.Unit;
import core.TimeInterval;

public class WriteTest {
	
	private static final Time TO_SLEEP_TIMEOUT = new Time(15*60, Unit.SECONDS);
	private static final Time TRANSITION_DURATION = new Time(2500, Unit.MILLISECONDS);
	
	private FileSystemClient client;
	
	@Before
	public void setup() {
		Machine jurupoca = new Machine("jurupoca", TO_SLEEP_TIMEOUT, TRANSITION_DURATION);
		
		Time timeToCoherence = new Time(5 * 60, Unit.SECONDS);
		Time timeToDelete = new Time(5 * 60, Unit.SECONDS);
		Set<DataServer> dataServers = new HashSet<DataServer>();
		dataServers.add(new DataServer(jurupoca));
		MetadataServer metadataServer = new MetadataServer(dataServers, "random", 0, timeToCoherence, timeToDelete);
		client = new FileSystemClient(jurupoca, metadataServer, false);		
	}
	
	@Test
	public void testWriteIntervalsAfterOneWrite() {
		Time eventStart = Time.GENESIS;
		Time duration = new Time(3, Unit.MILLISECONDS);
		Write write = 
				new Write(client, eventStart, duration, "/home/josebiades/texto.txt", 15 , 1024);
		
		write.process();
		
		ReplicatedFile file = client.createOrOpen("/home/josebiades/texto.txt");
		DataServer ds = file.getPrimary();
		List<TimeInterval> writeIntervals = ds.getWriteIntervals();
		
		assertEquals(1024L, file.getSize());
		assertFalse(file.areReplicasConsistent());
		assertEquals(1, writeIntervals.size());
		assertEquals(new TimeInterval(eventStart, eventStart.plus(duration)), writeIntervals.get(0));
	}
	
	@Test
	public void testWriteIntervalsAfterNonOverlappedWrites() {
		Time event1Start = Time.GENESIS;
		Time event1Duration = new Time(5, Unit.MILLISECONDS);
		Write write = 
				new Write(client, event1Start, event1Duration, "/home/josebiades/texto.txt", 15 , 1024);
		write.process();
		
		Time event2Start = new Time(6, Unit.MILLISECONDS);
		Time event2Duration = new Time(5, Unit.MILLISECONDS);
		write =	new Write(client, event2Start, event2Duration, "/home/josebiades/texto.txt", 15 , 1024);
		write.process();
		
		ReplicatedFile file = client.createOrOpen("/home/josebiades/texto.txt");
		DataServer ds = file.getPrimary();
		List<TimeInterval> writeIntervals = ds.getWriteIntervals();
		
		assertEquals(1024L, file.getSize());
		assertFalse(file.areReplicasConsistent());
		assertEquals(2, writeIntervals.size());
		TimeInterval expectedTimeInterval = new TimeInterval(event1Start, event1Start.plus(event1Duration));
		assertTrue(writeIntervals.contains(expectedTimeInterval));
		expectedTimeInterval = new TimeInterval(event2Start, event2Start.plus(event2Duration));
		assertTrue(writeIntervals.contains(expectedTimeInterval));
	}
	
	@Test
	public void testWriteIntervalsAfterOverlappedWrites() {
		Time event1Start = Time.GENESIS;
		Time event1Duration = new Time(5, Unit.MILLISECONDS);
		Write write = 
				new Write(client, event1Start, event1Duration, "/home/josebiades/texto.txt", 15 , 1024);
		write.process();
		
		Time event2Start = new Time(2, Unit.MILLISECONDS);
		Time event2Duration = new Time(5, Unit.MILLISECONDS);
		write =	new Write(client, event2Start, event2Duration, "/home/josebiades/texto.txt", 15 , 1024);
		write.process();
		
		ReplicatedFile file = client.createOrOpen("/home/josebiades/texto.txt");
		DataServer ds = file.getPrimary();
		List<TimeInterval> writeIntervals = ds.getWriteIntervals();
		
		assertEquals(1024L, file.getSize());
		assertFalse(file.areReplicasConsistent());
		assertEquals(1, writeIntervals.size());
		TimeInterval expectedTimeInterval = new TimeInterval(Time.GENESIS, event2Start.plus(event2Duration));
		assertTrue(writeIntervals.contains(expectedTimeInterval));
	}
	
	@Test
	public void testWriteIntervalsAfterOverlappedAndNonOverlappedWrites() {
		String filePath = "/home/patrick/teste.txt";
		Time zero = Time.GENESIS;
		Time five = new Time(5, Unit.MILLISECONDS);
		Time ten = new Time(10, Unit.MILLISECONDS);
		Time fifteen = new Time(15, Unit.MILLISECONDS);
		
		Write write = new Write(client, zero, five, filePath, 15, 1024);
		write.process();
		
		write = new Write(client, ten, five, filePath, 15, 1024);
		write.process();
		
		write = new Write(client, zero, five, filePath, 15, 1024);
		write.process();
		
		ReplicatedFile file = client.createOrOpen(filePath);
		List<TimeInterval> writeIntervals = file.getPrimary().getWriteIntervals();
		
		assertEquals(1024L, file.getSize());
		assertFalse(file.areReplicasConsistent());
		assertEquals(2, writeIntervals.size());
		TimeInterval expectedTimeInterval = new TimeInterval(Time.GENESIS, five);
		assertTrue(writeIntervals.contains(expectedTimeInterval));
		expectedTimeInterval = new TimeInterval(ten, fifteen);
		assertTrue(writeIntervals.contains(expectedTimeInterval));
	}
	
	@Test
	public void testWritesChangingFileSize() {
		String filePath = "/home/patrick/teste.txt";
		Time zero = Time.GENESIS;
		Time five = new Time(5, Unit.MILLISECONDS);
		Time ten = new Time(10, Unit.MILLISECONDS);

		Write write = new Write(client, zero, five, filePath, 15, 1024);
		write.process();
		
		write = new Write(client, five, five, filePath, 1024, 2048);
		write.process();
		
		ReplicatedFile file = client.createOrOpen(filePath);
		List<TimeInterval> writeIntervals = file.getPrimary().getWriteIntervals();
		
		assertEquals(2048L, file.getSize());
		assertFalse(file.areReplicasConsistent());
		assertEquals(1, writeIntervals.size());
		TimeInterval expectedTimeInterval = new TimeInterval(Time.GENESIS, ten);
		assertTrue(writeIntervals.contains(expectedTimeInterval));
	}

}
