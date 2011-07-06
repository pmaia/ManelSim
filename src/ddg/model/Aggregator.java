package ddg.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ddg.model.data.DataServer;

/**
 * Information aggregator and filter.
 * 
 * @author Thiago Emmanuel Pereira da Cunha Silva, thiago.manel@gmail.com
 * @author Ricardo Araujo Santos - ricardo@lsd.ufcg.edu.br
 */
public class Aggregator {

	public enum DataOperation {
		READ, WRITE
	};

	private List<String> loginlogs;
	private final Map<DataOperation, long[]> opstraces;
	private final Map<String, Map<DataOperation, long[]>> file2Data;
	private final List<String> generalLogs = new LinkedList<String>();

	private static Aggregator instance = new Aggregator();

	public static Aggregator getInstance() {
		return instance;
	}

	private Aggregator() {

		this.opstraces = new HashMap<DataOperation, long[]>();
		// (0, 0, 0, 0) # (totalbytes_co-located, totalbytes,
		// totalops_co-located, totalops)
		// long[] opsTraces = new long[4];
		this.opstraces.put(DataOperation.READ, new long[4]);
		this.opstraces.put(DataOperation.WRITE, new long[4]);

		this.file2Data = new HashMap<String, Map<DataOperation, long[]>>();
		this.loginlogs = new LinkedList<String>();
	}

	/**
	 * @param filename
	 *            TODO
	 * @param dataOperation
	 * @param length
	 * @param wasLocal
	 * @param machine
	 *            TODO
	 */
	public void reportDataOperation(String filename,
			DataOperation dataOperation, long length, boolean wasLocal,
			String machine) {

		// (0, 0, 0, 0) # (totalbytes_co-located, total bytes,
		// totalops_co-located, total ops)
		long[] traces = opstraces.get(dataOperation);
		updateTrace(traces, wasLocal, length);

		if (!file2Data.containsKey(filename)) {

			Map<DataOperation, long[]> filedata = new HashMap<DataOperation, long[]>();
			filedata.put(DataOperation.WRITE, new long[4]);
			filedata.put(DataOperation.READ, new long[4]);
			file2Data.put(filename, filedata);
		}

		long[] data = file2Data.get(filename).get(dataOperation);
		updateTrace(data, wasLocal, length);
	}

	private void updateTrace(long[] trace, boolean wasLocal, long length) {

		trace[1] += length;
		trace[3] += 1;

		// TAKE CARE USING THIS METHOD, DO NOT CALL IT ON REPLICA MANUTENTION
		// DATA TRANSFER, IN DOING SO IT WILL BE UNBALANCED
		if (wasLocal) {
			trace[0] += length;
			trace[2] += 1;
		}
	}

	public void reportlogin(DDGClient client, long now) {
		DataServer dataServer = client.getMachine().getDeployedDataServers()
				.get(0);
		loginlogs.add(dataServer + "\t" + dataServer.getAvailableDiskSize()
				+ "\t" + now);
	}

	public void report(String logline) {
		generalLogs.add(logline);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {

		StringBuffer buffer = new StringBuffer();

		long[] readtrace = opstraces.get(DataOperation.READ);
		long[] writetrace = opstraces.get(DataOperation.WRITE);

		appendToBuffer(buffer, readtrace, writetrace);

		for (Entry<String, Map<DataOperation, long[]>> fileData : file2Data
				.entrySet()) {
			buffer.append(fileData.getKey() + "\t");
			appendToBuffer(buffer, fileData.getValue().get(DataOperation.READ),
					fileData.getValue().get(DataOperation.WRITE));
		}

		for (String logline : generalLogs) {
			buffer.append(logline + " \n");
		}

		return buffer.toString();
	}

	private void appendToBuffer(StringBuffer buff, long[] read, long[] write) {

		for (int i = 0; i < read.length; i++) {
			buff.append("\t" + read[i]);
		}

		for (int i = 0; i < write.length; i++) {
			buff.append("\t" + write[i]);
		}

		buff.append("\n");
	}
}