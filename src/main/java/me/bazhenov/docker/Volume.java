package me.bazhenov.docker;

/**
 * Provide a basic volume support in a docker container
 */
public @interface Volume {

	/**
	 * @return path <i>inside the container</i>
	 */
	String value();

	/**
	 * @return path <i>at the host</i>
	 */
	String atHost();

	/**
	 * @return should empty directory be created at {@link #atHost()} path if it's missing
	 */
	boolean createDirectoryIfMissing() default true;
}
