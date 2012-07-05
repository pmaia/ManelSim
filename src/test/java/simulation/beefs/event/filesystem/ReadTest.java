package simulation.beefs.event.filesystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import simulation.beefs.model.DataServer;
import simulation.beefs.model.FileSystemClient;
import simulation.beefs.model.MetadataServer;
import simulation.beefs.model.ReplicatedFile;
import core.Time;
import core.Time.Unit;
import core.TimeInterval;

/**
 * 
 * @author Patrick Maia
 *
 */
public class ReadTest {
	
	private FileSystemClient client;
	
	@Before
	public void setup() {
		Set<DataServer> dataServers = new HashSet<DataServer>();
		dataServers.add(new DataServer());
		MetadataServer metadataServer = new MetadataServer(dataServers, "random", 0);
		client = new FileSystemClient("jurupoca", metadataServer);
	}
	
	@Test
	public void testFileSizeIsTheSameAfterRead() {
		String filePath = "/home/patrick/cruzeiro.txt";
		ReplicatedFile file = client.createOrOpen(filePath);
		file.setSize(1024);
		
		Read read = new Read(client, Time.GENESIS, new Time(5, Unit.MILLISECONDS), filePath, 10);
		read.process();
		
		assertEquals(1024, file.getSize());
	}
	
	@Test
	public void testReplicasCoherenceAreTheSameAfterRead() {
		String filePath = "/home/patrick/cruzeiro.txt";
		ReplicatedFile file = client.createOrOpen(filePath);
		file.setSize(1024);
		file.setReplicasCoherenceStatus(true);
		
		Read read = new Read(client, Time.GENESIS, new Time(5, Unit.MILLISECONDS), filePath, 10);
		read.process();
		assertEquals(true, file.areReplicasCoherent());
		
		file.setReplicasCoherenceStatus(false);
		read.process();
		assertEquals(false, file.areReplicasCoherent());
	}
	
	@Test
	public void testReadIntervalsAfterOneRead() {
		Time scheduledTime = Time.GENESIS;
		Time duration = new Time(5, Unit.MILLISECONDS);
		String filePath = "/home/patrick/teste.txt";
		long length = 1024;
		
		Read read = new Read(client, scheduledTime, duration, filePath, length);
		read.process();
		
		ReplicatedFile file = client.createOrOpen(filePath);
		List<TimeInterval> readIntervals = file.getPrimary().getReadIntervals();
		
		assertEquals(1, readIntervals.size());
		TimeInterval expectedReadInterval = new TimeInterval(scheduledTime, scheduledTime.plus(duration));
		assertTrue(readIntervals.contains(expectedReadInterval));
	}
	
	@Test
	public void testReadIntervalsAfterNonOverlappedReads() {
		String filePath = "/home/patrick/cruzeiro.txt";
		Time read1Start = Time.GENESIS;
		Time read2Start = new Time(6, Unit.MILLISECONDS);
		Time eventsDuration = new Time(5, Unit.MILLISECONDS);
		
		Read read1 = new Read(client, read1Start, eventsDuration, filePath, 64);
		read1.process();
		Read read2 = new Read(client, read2Start, eventsDuration, filePath, 1024);
		read2.process();
		
		ReplicatedFile file = client.createOrOpen(filePath);
		List<TimeInterval> readIntervals = file.getPrimary().getReadIntervals();
		
		assertEquals(2, readIntervals.size());
		TimeInterval expectedTimeInterval = new TimeInterval(read1Start, read1Start.plus(eventsDuration));
		assertTrue(readIntervals.contains(expectedTimeInterval));
		expectedTimeInterval = new TimeInterval(read2Start, read2Start.plus(eventsDuration));
		assertTrue(readIntervals.contains(expectedTimeInterval));
	}
	
	@Test
	public void testReadIntervalsAfterOverlapperdReads() {
		String filePath = "/home/patrick/cruzeiro.txt";
		Time read1Start = Time.GENESIS;
		Time read2Start = new Time(5, Unit.MILLISECONDS);
		Time duration = new Time(10, Unit.MILLISECONDS);
		
		Read read1 = new Read(client, read1Start, duration, filePath, 1024);
		read1.process();
		
		Read read2 = new Read(client, read2Start, duration, filePath, 1024);
		read2.process();
		
		ReplicatedFile file = client.createOrOpen(filePath);
		List<TimeInterval> readIntervals = file.getPrimary().getReadIntervals();
		
		assertEquals(1, readIntervals.size());
		TimeInterval expectedTimeInterval = new TimeInterval(Time.GENESIS, new Time(15, Unit.MILLISECONDS));
		assertTrue(readIntervals.contains(expectedTimeInterval));
	}
	
	@Test
	public void testReadIntervalsAfterOverlappedAndNonOverlappedReads() {
		String filePath = "/home/patrick/cruzeiro.txt";
		Time read1Start = Time.GENESIS;
		Time read2Start = new Time(3, Unit.MILLISECONDS);
		Time read3Start = new Time(10, Unit.MILLISECONDS);
		Time duration = new Time(5, Unit.MILLISECONDS);
		
		Read read1 = new Read(client, read1Start, duration, filePath, 1024);
		read1.process();
		
		Read read2 = new Read(client, read2Start, duration, filePath, 1024);
		read2.process();
		
		Read read3 = new Read(client, read3Start, duration, filePath, 1024);
		read3.process();
		
		ReplicatedFile file = client.createOrOpen(filePath);
		List<TimeInterval> readIntervals = file.getPrimary().getReadIntervals();
		
		assertEquals(2, readIntervals.size());
		TimeInterval expectedTimeInterval = new TimeInterval(Time.GENESIS, new Time(8, Unit.MILLISECONDS));
		assertTrue(readIntervals.contains(expectedTimeInterval));
		expectedTimeInterval = new TimeInterval(read3Start, read3Start.plus(duration));
		assertTrue(readIntervals.contains(expectedTimeInterval));
	}
	
}
