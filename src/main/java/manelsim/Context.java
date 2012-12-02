package manelsim;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Patrick Maia
 *
 */
public class Context {
	
	private final EventSourceMultiplexer eventSourceMultiplexer;
	
	private final Map<String, Object> contextObjects = new HashMap<String, Object>();
	
	public Context(EventSourceMultiplexer eventSourceMultiplexer) {
		this.eventSourceMultiplexer = eventSourceMultiplexer;
	}
	
	public EventSourceMultiplexer getEventSourceMultiplexer() {
		return eventSourceMultiplexer;
	}
	
	public void add(String key, Object value) {
		contextObjects.put(key, value);
	}
	
	public Object get(String key) {
		return contextObjects.get(key);
	}

}
