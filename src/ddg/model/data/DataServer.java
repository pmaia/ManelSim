package ddg.model.data;

import java.util.HashMap;
import java.util.Map;

import ddg.kernel.JEEventScheduler;
import ddg.model.DDGClient;
import ddg.model.File;
import ddg.model.HasNotSpaceOnDeviceException;
import ddg.model.Machine;

/**
 * DataServer representation.
 * 
 * @author Thiago Emmanuel Pereira da Cunha Silva - thiagoepdc@lsd.ufcg.edu.br
 * @author Ricardo Araujo Santos - ricardo@lsd.ufcg.edu.br
 */
public class DataServer implements Comparable<DataServer> {

	private final String id;
	private final Machine machine;
	private final double diskSize;
	private double availableDiskSize;

	private final Map<String, File> files;

	/**
	 * Default constructor using fields.
	 * 
	 * @param scheduler
	 * @param machine
	 * @param diskSize
	 */
	public DataServer(JEEventScheduler scheduler, Machine machine,
			double diskSize) {
		this.machine = machine;
		this.id = "ds" + machine.deploy(this) + machine;
		this.diskSize = diskSize;
		this.availableDiskSize = this.diskSize;
		this.files = new HashMap<String, File>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(DataServer o) {

		// more_available < less_available

		double diff = getAvailableDiskSize() - o.getAvailableDiskSize();
		// the direct subtraction can raise a flow error
		if (diff > 0) {
			return -1;
		} else if (diff < 0) {
			return 1;
		}

		return 0;
	}

	/**
	 * @param fileName
	 */
	void removeFileReplica(String fileName) {
		File removed = files.remove(fileName);
		availableDiskSize += removed.getSize();
	}

	/**
	 * @param fileName
	 * @param size
	 */
	void createFile(String fileName, long size) {

		File file = new File(fileName, size);
		long addition = file.getSize();

		if (hasSpaceOnDevice(addition)) {
			availableDiskSize -= addition;

			files.put(fileName, file);

		} else {
			throw new HasNotSpaceOnDeviceException(addition, this);
		}

	}

	/**
	 * @param fileName
	 * @param offset
	 * @param length
	 * @param client
	 */
	void writeFile(String fileName, long offset, long length, DDGClient client) {

		File file = getFile(fileName);

		long newSize = Math.max(file.getSize(), offset + length);
		long addition = newSize - file.getSize();

		if (hasSpaceOnDevice(addition)) {
			availableDiskSize -= addition;
			file.setSize(newSize);
		} else {
			throw new HasNotSpaceOnDeviceException(addition, this);
		}
	}

	/**
	 * 
	 * @param fileSize
	 * @return
	 */
	public boolean hasSpaceOnDevice(long addition) {
		return availableDiskSize >= addition;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ddg.DataServer#getId()
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * @return the machine
	 */
	public Machine getMachine() {
		return machine;
	}

	/**
	 * @return the diskSize
	 */
	public double getDiskSize() {
		return diskSize;
	}

	public boolean isFull() {
		return getAvailableDiskSize() == 0;
	}

	/**
	 * @return the availableDiskSize
	 */
	public double getAvailableDiskSize() {
		return availableDiskSize;
	}

	/**
	 * @param fileName
	 * @return
	 */
	public boolean containsFile(String fileName) {
		return files.containsKey(fileName);
	}

	/**
	 * @param fileName
	 * @return
	 */
	private File getFile(String fileName) {
		return files.get(fileName);
	}

	/**
	 * @param fileName
	 * @return
	 */
	public long getFileSize(String fileName) {
		return getFile(fileName).getSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataServer other = (DataServer) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return id;
	}

}