/**
 * 
 */
package org.openntf.domino.utils;
import java.util.logging.Logger;


import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.ArrayList;
//import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector; 

import lotus.domino.Document;

//import com.czarnowski.base.util.CzarCore;
//import com.czarnowski.base.util.Strings;

/**
 * Collections utilities library
 * 
 * @author Devin S. Olson (dolson@czarnowski.com)
 */
public class Collections {
	private static final Logger log_ = Logger.getLogger(Collections.class.getName());
	private static final long serialVersionUID = 1L;
	
	


	public static final String CLASSNAME = "org.openntf.domino.utils.Collections";


	/**
	 * Zero-Argument Constructor
	 */
	public Collections() {
	}


	/**
	 * Gets or generates an List of Strings from an Item on a Document
	 * 
	 * @param source
	 *            Document from which to get or generate the result.
	 * 
	 * @param itemname
	 *            Name of item from which to get the content
	 * 
	 * @return List of Strings retrieved or generated from the input. Returns null on error.
	 */
	public static List<String> getListStrings(final Document source, final String itemname) {
		try {
			if (null == source) { throw new IllegalArgumentException("Source document is null"); }
			if (Strings.isBlankString(itemname)) { throw new IllegalArgumentException("ItemName is blank or null"); }

			return (source.hasItem(itemname)) ? Collections.getListStrings(source.getItemValue(itemname)) : null;

		} catch (final Exception e) {
			CzarCore.logException(Collections.CLASSNAME, e, "itemname: " + itemname);
		}

		return null;
	}


	/**
	 * Gets or generates an List of Strings from a Vector
	 * 
	 * @param vector
	 *            Vector from which to get or generate the result.
	 * 
	 * @return List of Strings retrieved or generated from the input. Returns null on error.
	 */

	@SuppressWarnings("unchecked")
	public static List<String> getListStrings(final Vector vector) {
		try {
			return (null == vector) ? null : Collections.list(vector.elements());
		} catch (final Exception e) {
			CzarCore.logException(Collections.CLASSNAME, e, "vector: " + vector);
		}

		return null;
	}


	/**
	 * Gets or generates an List of Strings from an AbstractCollection
	 * 
	 * @param collection
	 *            AbstractCollection from which to get or generate the result.
	 * 
	 * @return List of Strings retrieved or generated from the input. Returns null on error.
	 */
	public static List<String> getListStrings(final AbstractCollection collection) {
		try {
			if ((null != collection) && (collection.size() > 0)) {
				final List<String> result = new ArrayList<String>();
				if (collection.iterator().next() instanceof Object) {
					// treat as an object
					for (final Object o : collection) {
						if (null != o) {
							result.add(o.toString());
						}
					}

				} else {
					// treat as a primitive
					final Iterator it = collection.iterator();
					while (it.hasNext()) {
						result.add(String.valueOf(it.next()));
					}
				}

				return result;
			}

		} catch (final Exception e) {
			CzarCore.logException(Collections.CLASSNAME, e, "collection: " + collection);
		}

		return null;
	}


	/**
	 * Gets or generates an List of Strings from an AbstractMap
	 * 
	 * @param map
	 *            AbstractMap from which to get or generate the result. Only the Values will be retrieved, the Keys will
	 *            are ignored.
	 * 
	 * @return List of Strings retrieved or generated from the input. Returns null on error.
	 */
	public static List<String> getListStrings(final AbstractMap map) {
		try {
			return ((null != map) && (map.size() > 0)) ? Collections.getListStrings(map.values()) : null;

		} catch (final Exception e) {
			CzarCore.logException(Collections.CLASSNAME, e, "map: " + map);
		}

		return null;
	}


	/**
	 * Gets or generates an List of Strings from a String
	 * 
	 * @param string
	 *            String from which to get or generate the result.
	 * 
	 * @return List of Strings retrieved or generated from the input. Returns null on error.
	 */
	public static List<String> getListStrings(final String string) {
		try {
			if (!Strings.isBlankString(string)) {
				final List<String> result = new ArrayList<String>();
				result.add(string);
				return result;
			}

		} catch (final Exception e) {
			CzarCore.logException(Collections.CLASSNAME, e, "string: " + string);
		}

		return null;
	}


	/**
	 * Gets or generates an List of Strings from a array of Strings
	 * 
	 * @param strings
	 *            Array of Strings from which to get or generate the result.
	 * 
	 * @return List of Strings retrieved or generated from the input. Returns null on error.
	 */
	public static List<String> getListStrings(final String[] strings) {
		try {
			if ((null != strings) && (strings.length > 0)) {
				final List<String> result = new ArrayList<String>();
				for (final String s : strings) {
					result.add(s);
				}
				return result;
			}

		} catch (final Exception e) {
			CzarCore.logException(Collections.CLASSNAME, e, "strings: " + strings);
		}

		return null;
	}


	/**
	 * Gets or generates an List of Strings from an Object
	 * 
	 * @param object
	 *            Object from which to get or generate the result. Attempts to retrieve the values from the object.
	 * 
	 * @return List of Strings retrieved or generated from the input. Returns null on error.
	 * 
	 */
	public static List<String> getListStrings(final Object object) {
		String classname = null;
		try {
			if (null != object) {
				classname = object.getClass().getName();

				if (object instanceof Vector) { return Collections.getListStrings((Vector) object); }
				if (object instanceof AbstractCollection) { return Collections.getListStrings((AbstractCollection) object); }
				if (object instanceof AbstractMap) { return Collections.getListStrings((AbstractMap) object); }
				if (object instanceof String) { return Collections.getListStrings((String) object); }
				if (object instanceof String[]) { return Collections.getListStrings((String[]) object); }
				if (classname.equalsIgnoreCase("java.lang.String[]") || classname.equalsIgnoreCase("[Ljava.lang.String;")) { return Collections
				        .getListStrings((String[]) object); }
				if (classname.equalsIgnoreCase("java.lang.String")) { return Collections.getListStrings((String) object); }

				throw new IllegalArgumentException("Unsupported Class:" + classname);
			}

		} catch (final Exception e) {
			CzarCore.logException(Collections.CLASSNAME, e, "object classname: " + classname, "object: ", object);
		}

		return null;
	}


	/**
	 * Gets or generates a TreeSet of Strings from an Object
	 * 
	 * @param object
	 *            Object from which to get or generate the result. Attempts to retrieve the string values from the
	 *            object.
	 * 
	 * @return TreeSet of Strings retrieved or generated from the input. Returns null on error.
	 * 
	 */
	public static TreeSet<String> getTreeSetStrings(final Object object) {
		try {
			final List<String> al = Collections.getListStrings(object);
			return (null == al) ? null : new TreeSet<String>(al);

		} catch (final Exception e) {
			CzarCore.logException(Collections.CLASSNAME, e, "object: " + object);
		}

		return null;
	}


	/**
	 * Gets or generates an Array of Strings from an Object
	 * 
	 * @param object
	 *            Object from which to get or generate the result. Attempts to retrieve the string values from the
	 *            object.
	 * 
	 * @return Array of Strings retrieved or generated from the input. Returns null on error.
	 * 
	 */
	public static String[] getStringArray(final Object object) {
		try {
			final List<String> al = Collections.getListStrings(object);
			return (null == al) ? null : al.toArray(new String[al.size()]);
		} catch (final Exception e) {
			CzarCore.logException(Collections.CLASSNAME, e, "object: " + object);
		}

		return null;
	}


	/**
	 * Gets or generates an Array of Strings from an Object
	 * 
	 * Result array will contain only unique values and will be sorted according to the String object's natural sorting
	 * method
	 * 
	 * @param object
	 *            Object from which to get or generate the result. Attempts to retrieve the string values from the
	 *            object.
	 * 
	 * @return Array of Strings retrieved or generated from the input. Returns null on error.
	 * 
	 * @see java.lang.String#compareTo(String)
	 * 
	 */
	public static String[] getSortedUnique(final Object object) {
		try {
			final TreeSet<String> ts = Collections.getTreeSetStrings(object);
			return ((null == ts) || (ts.size() < 1)) ? null : (String[]) ts.toArray();
		} catch (final Exception e) {
			CzarCore.logException(Collections.CLASSNAME, e, "object: " + object);
		}

		return null;
	}


	/**
	 * Compares two String[] objects
	 * 
	 * Arguments are first compared by existence, then by # of elements, then by values.
	 * 
	 * @param stringarray0
	 *            First String[] to compare.
	 * 
	 * @param stringarray1
	 *            Second String[] to compare.
	 * 
	 * @param descending
	 *            flags indicating comparison order. true = descending, false = ascending.
	 * 
	 * @return a negative integer, zero, or a positive integer indicating if the first object is less than, equal to, or
	 *         greater than the second object.
	 * 
	 * @throws RuntimeException
	 * @see java.lang.Comparable#compareTo(Object)
	 * @see packers.czardev.util.Core#LESS_THAN
	 * @see packers.czardev.util.Core#EQUAL
	 * @see packers.czardev.util.Core#GREATER_THAN
	 */
	public static int compareStringArrays(final String[] stringarray0, final String[] stringarray1, final boolean descending) {
		try {
			if (null == stringarray0) {
				return (null == stringarray1) ? CzarCore.EQUAL : (descending) ? CzarCore.GREATER_THAN : CzarCore.LESS_THAN;
			} else if (null == stringarray1) { return (descending) ? CzarCore.LESS_THAN : CzarCore.GREATER_THAN; }

			if (stringarray0.length < stringarray1.length) { return (descending) ? CzarCore.GREATER_THAN : CzarCore.LESS_THAN; }
			if (stringarray1.length < stringarray0.length) { return (descending) ? CzarCore.LESS_THAN : CzarCore.GREATER_THAN; }

			for (int i = 0; i < stringarray0.length; i++) {
				final int result = stringarray0[i].compareTo(stringarray1[i]);
				if (CzarCore.EQUAL != result) { return (descending) ? -result : result; }
			}
			return CzarCore.EQUAL;

		} catch (final Exception e) {
			CzarCore.logException(Collections.CLASSNAME, e, "stringarray0:" + stringarray0, "stringarray1:" + stringarray1,
			        "descending:" + descending);
			throw new RuntimeException("EXCEPTION in Collections.compareStringArrays()");
		}
	}


	/**
	 * Compares two TreeSet<String> objects
	 * 
	 * Arguments are first compared by existence, then by size, then by values.
	 * 
	 * @param treeset0
	 *            First TreeSet<String> to compare.
	 * 
	 * @param treeset1
	 *            Second TreeSet<String> to compare.
	 * 
	 * @param descending
	 *            flags indicating comparison order. true = descending, false = ascending.
	 * 
	 * @return a negative integer, zero, or a positive integer indicating if the first object is less than, equal to, or
	 *         greater than the second object.
	 * 
	 * @throws RuntimeException
	 * @see java.lang.Comparable#compareTo(Object)
	 * @see packers.czardev.util.Core#LESS_THAN
	 * @see packers.czardev.util.Core#EQUAL
	 * @see packers.czardev.util.Core#GREATER_THAN
	 */
	public static int compareTreeSetStrings(final TreeSet<String> treeset0, final TreeSet<String> treeset1, final boolean descending) {
		try {
			if (null == treeset0) {
				return (null == treeset1) ? CzarCore.EQUAL : (descending) ? CzarCore.GREATER_THAN : CzarCore.LESS_THAN;
			} else if (null == treeset1) { return (descending) ? CzarCore.LESS_THAN : CzarCore.GREATER_THAN; }

			if (treeset0.size() < treeset1.size()) { return (descending) ? CzarCore.GREATER_THAN : CzarCore.LESS_THAN; }
			if (treeset1.size() < treeset0.size()) { return (descending) ? CzarCore.LESS_THAN : CzarCore.GREATER_THAN; }

			// Compare as string arrays
			return Collections.compareStringArrays(Collections.getStringArray(treeset0), Collections.getStringArray(treeset1),
			        descending);

		} catch (final Exception e) {
			CzarCore.logException(Collections.CLASSNAME, e, "treeset0:" + treeset0, "treeset1:" + treeset1, "descending:" + descending);
			throw new RuntimeException("EXCEPTION in Collections.compareTreeSetStrings()");
		}
	}


	public static TreeSet<String> getTreeSetStringsBeginsWith(final Object source, final String prefix) {
		try {
			final TreeSet<String> temp = Collections.getTreeSetStrings(source);
			if ((null != temp) && (temp.size() > 0)) {
				final TreeSet<String> result = new TreeSet<String>();
				for (final String s : temp) {
					if (CzarCore.startsWithIgnoreCase(s, prefix)) {
						result.add(s);
					}
				}

				return (result.size() > 0) ? result : null;
			}

		} catch (final Exception e) {
			CzarCore.logException(Collections.CLASSNAME, "Prefix: " + prefix);
		}

		return null;
	}

}

}
