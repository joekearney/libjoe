package joe.util;

import static com.google.common.base.Predicates.*;
import static com.google.common.base.Throwables.*;
import static com.google.common.collect.Maps.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

public class PropertyUtils {
	private PropertyUtils() {}

	/**
	 * Function to transform from a file path to a property map based on the
	 * contents.
	 * 
	 * @param fileName name of the file to parse
	 * @return {@link ImmutableMap} of properties found in the file
	 * @throws FileNotFoundException if the file did not exist
	 * @throws IOException if there was a problem reading the file
	 * @throws IllegalArgumentException if the file was malformed and could not
	 *             be parsed
	 */
	public static final Function<String, Map<String, String>> READ_PROP_FILE_TO_MAP = new Function<String, Map<String, String>>() {
		@Override
		public Map<String, String> apply(String path) {
			try {
				return loadPropertiesFile(path);
			} catch (IOException e) {
				throw propagate(e);
			}
		}
	};

	/**
	 * Gets a live view over the entries in the current system properties map
	 * that map a {@link String} key to a {@code String} value. This will
	 * typically be everything, but typed more usefully than a {@code Hashtable<Object, Object>}.
	 * <p>
	 * Consider copying the result with {@link ImmutableMap#copyOf(Map)} in order to avoid the extra performance penalty
	 * on each lookup.
	 * 
	 * @return view of the {@code String} entries in the system property map
	 */
	public static Map<String, String> getSystemPropertyStrings() {
		return getStringEntries(System.getProperties());
	}
	/**
	 * Gets a live view over the entries in the specified map that map a {@link String} key to a {@code String} value.
	 * <p>
	 * Consider copying the result with {@link ImmutableMap#copyOf(Map)} in order to avoid the extra performance penalty
	 * on each lookup.
	 * 
	 * @param map over which to provide a view
	 * @return view of the {@code String} entries in the map
	 */
	@SuppressWarnings("unchecked")
	// casts are safe, we can only ever see String instances
	public static Map<String, String> getStringEntries(Map<?, ?> map) {
		return (Map<String, String>) (Map<?, ?>) filterValues(filterKeys(map, instanceOf(String.class)),
				instanceOf(String.class));
	}

	/**
	 * Loads a properties file into a map, returning a immutable view.
	 * 
	 * @param fileName name of the file to parse
	 * @return {@link ImmutableMap} of properties found in the file
	 * @throws FileNotFoundException if the file did not exist
	 * @throws IOException if there was a problem reading the file
	 * @throws IllegalArgumentException if the file was malformed and could not
	 *             be parsed
	 */
	public static Map<String, String> loadPropertiesFile(String fileName) throws FileNotFoundException, IOException,
			IllegalArgumentException {
		return loadPropertiesFile(new File(fileName));
	}
	/**
	 * Loads a properties file into a map, returning a immutable view.
	 * 
	 * @param file file to parse
	 * @return {@link ImmutableMap} of properties found in the file
	 * @throws FileNotFoundException if the file did not exist
	 * @throws IOException if there was a problem reading the file
	 * @throws IllegalArgumentException if the file was malformed and could not
	 *             be parsed
	 */
	public static Map<String, String> loadPropertiesFile(File file) throws FileNotFoundException, IOException,
			IllegalArgumentException {
		return loadPropertiesStream(new FileInputStream(file));
	}
	/**
	 * Loads a properties file into a map if, returning a immutable view, if the
	 * file exists. Returns an empty map otherwise.
	 * 
	 * @param file file to parse
	 * @return {@link ImmutableMap} of properties found in the file if the file
	 *         exists, or an empty map otherwise
	 * @throws FileNotFoundException if the file did not exist
	 * @throws IOException if there was a problem reading the file
	 * @throws IllegalArgumentException if the file was malformed and could not
	 *             be parsed
	 */
	public static Map<String, String> loadPropertiesFileIfExists(String fileName) throws FileNotFoundException,
			IOException {
		return loadPropertiesFileIfExists(fileName == null ? null : new File(fileName));
	}
	/**
	 * Loads a properties file into a map if, returning a immutable view, if the
	 * file exists. Returns an empty map otherwise.
	 * 
	 * @param file file to parse
	 * @return {@link ImmutableMap} of properties found in the file if the file
	 *         exists, or an empty map otherwise
	 * @throws FileNotFoundException if the file did not exist
	 * @throws IOException if there was a problem reading the file
	 * @throws IllegalArgumentException if the file was malformed and could not
	 *             be parsed
	 */
	public static Map<String, String> loadPropertiesFileIfExists(File file) throws FileNotFoundException, IOException {
		return file != null && file.exists() ? loadPropertiesFile(file) : emptyPropertiesMap();
	}
	/**
	 * Loads an input stream containing {@code key=value} pairs into a map,
	 * returning a immutable view. The parameter is typically an {@link InputStream} over a properties file.
	 * 
	 * @param inputStream stream to parse
	 * @return {@link ImmutableMap} of properties found in the stream
	 * @throws IOException if there was a problem reading from the stream
	 * @throws IllegalArgumentException if the stream was malformed and could
	 *             not be parsed
	 * @see #loadPropertiesFile(String)
	 */
	public static Map<String, String> loadPropertiesStream(InputStream inputStream) throws IOException,
			IllegalArgumentException {
		Properties properties = new Properties();
		properties.load(inputStream);
		// don't sort results, keep them in declared order
		return ImmutableMap.copyOf(getStringEntries(properties));
	}
	/** Returns an empty map, typed for ease of use. */
	public static Map<String, String> emptyPropertiesMap() {
		return ImmutableMap.of();
	}

	static final Pattern PROPERTY_KEY_PATTERN = Pattern.compile("\\$\\{([^\\}]*)\\}");
	public static Function<String, String> propertyResolverFromMap(final Map<String, String> properties) {
		return new Function<String, String>() {
			@Override
			public String apply(final String input) {
				String result = input;
				boolean changed = false;

				do {
					StringBuffer sb = new StringBuffer();
					if (changed = resolvePropertyName(result, false, sb)) {
						result = sb.toString();
					}
				} while (changed);

				return result;
			}
			private boolean resolvePropertyName(String input, boolean isKey, StringBuffer sb) {
				if (isKey) {
					String value = properties.get(input);
					if (value != null) {
						sb.append(value);
						return true;
					}
				}

				Matcher matcher = PROPERTY_KEY_PATTERN.matcher(input);

				boolean changed = false;
				int furthestIndexOfInputCopied = 0;
				while (matcher.find()) {
					sb.append(input.substring(furthestIndexOfInputCopied, matcher.start()));
					changed |= resolvePropertyName(matcher.group(1), true, sb);
					furthestIndexOfInputCopied = matcher.end(); // not end(1), want to skip the '}'
				}
				
				sb.append(input.substring(furthestIndexOfInputCopied));

				return changed;
			}
		};
	}
}
