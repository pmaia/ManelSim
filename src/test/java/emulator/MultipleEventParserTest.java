/**
 * Copyright (C) 2009 Universidade Federal de Campina Grande
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package emulator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.PriorityQueue;

import kernel.Event;
import model.FileSystemClient;
import model.Machine;

import org.junit.Test;

import emulator.event.filesystem.FileSystemTraceEventSource;

/**
 * A suite of tests to the MultipleSourceEventParser class
 *
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public class MultipleEventParserTest {
	
	@Test
	public void eventOrderingTest() {
		EventSource [] eventSources = new EventSource[3];
		
		InputStream trace1 = new FakeFileSystemTraceStream(0);
		InputStream trace2 = new FakeFileSystemTraceStream(60);
		InputStream trace3 = new FakeFileSystemTraceStream(30);
		
		PriorityQueue<Event> eventsGeneratedBySimulationQueue = new PriorityQueue<Event>();
		
		Machine machine1 = new Machine(eventsGeneratedBySimulationQueue, "cherne", 30 * 60);
		Machine machine2 = new Machine(eventsGeneratedBySimulationQueue, "palhaco", 30 * 60);
		Machine machine3 = new Machine(eventsGeneratedBySimulationQueue, "abelhinha", 30 * 60);
		
		FileSystemClient client1 = new FileSystemClient(eventsGeneratedBySimulationQueue, machine1, null, false);
		FileSystemClient client2 = new FileSystemClient(eventsGeneratedBySimulationQueue, machine2, null, false);
		FileSystemClient client3 = new FileSystemClient(eventsGeneratedBySimulationQueue, machine3, null, false);
		
		eventSources[0] = new FileSystemTraceEventSource(client1, trace1);
		eventSources[1] = new FileSystemTraceEventSource(client2, trace2);
		eventSources[2] = new FileSystemTraceEventSource(client3, trace3);
		
		EventSource multipleSourceParser = new MultipleEventSource(eventSources, eventsGeneratedBySimulationQueue);
		
		Event currentEvent = multipleSourceParser.getNextEvent();
		Event nextEvent = null;
		while((nextEvent = multipleSourceParser.getNextEvent()) != null) {
			assertTrue(currentEvent.getScheduledTime().compareTo(nextEvent.getScheduledTime()) <= 0);
			currentEvent = nextEvent;
		}
	}
	
	@Test
	public void eventsDeliveredCountTest() {
		EventSource [] parsers = new EventSource[3];
		
		InputStream trace1 = new FakeFileSystemTraceStream(50);
		InputStream trace2 = new FakeFileSystemTraceStream(1000);
		InputStream trace3 = new FakeFileSystemTraceStream(50);
		
		PriorityQueue<Event> eventsGeneratedBySimulationQueue = new PriorityQueue<Event>();
		
		Machine machine1 = new Machine(eventsGeneratedBySimulationQueue, "cherne", 30 * 60);
		Machine machine2 = new Machine(eventsGeneratedBySimulationQueue, "palhaco", 30 * 60);
		Machine machine3 = new Machine(eventsGeneratedBySimulationQueue, "abelhinha", 30 * 60);
		
		FileSystemClient client1 = new FileSystemClient(eventsGeneratedBySimulationQueue, machine1, null, false);
		FileSystemClient client2 = new FileSystemClient(eventsGeneratedBySimulationQueue, machine2, null, false);
		FileSystemClient client3 = new FileSystemClient(eventsGeneratedBySimulationQueue, machine3, null, false);
		
		parsers[0] = new FileSystemTraceEventSource(client1, trace1);
		parsers[1] = new FileSystemTraceEventSource(client2, trace2);
		parsers[2] = new FileSystemTraceEventSource(client3, trace3);
		
		EventSource multipleSourceParser = new MultipleEventSource(parsers, eventsGeneratedBySimulationQueue);
		
		int eventCount = 0;
		while(multipleSourceParser.getNextEvent() != null) {
			eventCount++;
		}
		
		assertEquals(1100, eventCount);
	}
	 
	public static void main(String[] args) throws IOException {
		FakeFileSystemTraceStream res = new FakeFileSystemTraceStream(1000000);
		BufferedReader br = new BufferedReader(new InputStreamReader(res));
		String line;
		while((line = br.readLine()) != null) {
			System.out.println(line);
		}
	}
	
}
