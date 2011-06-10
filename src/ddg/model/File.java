package ddg.model;


/**
 * @author Ricardo Araujo Santos - ricardo@lsd.ufcg.edu.br
 * @author Thiago Emmanuel Pereira da Cunha Silva, thiago.manel@gmail.com
 */
public class File {
	
	private final String name;
	private long size;
	
	/**
	 * @param name
	 * @param size
	 */
	public File(String name, long size) {
		this.name = name;
		this.size = size;
	}
	
	/**
	 * @return the size
	 */
	public long getSize() {
		return size;
	}
	
	/**
	 * @param size the size to set
	 */
	public void setSize( long size ) {
		this.size = size;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
		result = prime * result + (int) ( size ^ ( size >>> 32 ) );
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals( Object obj ) {

		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;
		File other = (File) obj;
		if ( name == null ) {
			if ( other.name != null )
				return false;
		} else if ( !name.equals( other.name ) )
			return false;
		if ( size != other.size )
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name + " s" + size;
	}

}