/**
 * 
 */
package org.openntf.domino.design.impl;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.openntf.domino.Database;
import org.openntf.domino.Document;
import org.openntf.domino.design.DesignBaseNamed;

/**
 * @author jgallagher
 * 
 */
public abstract class AbstractDesignBaseNamed extends AbstractDesignBase implements DesignBaseNamed {
	@SuppressWarnings("unused")
	private static final Logger log_ = Logger.getLogger(AbstractDesignBaseNamed.class.getName());
	private static final long serialVersionUID = 1L;

	/**
	 * @param document
	 */
	protected AbstractDesignBaseNamed(final Document document) {
		super(document);
	}

	protected AbstractDesignBaseNamed(final Database database) {
		super(database);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openntf.domino.design.DesignBase#getAliases()
	 */
	@Override
	public List<String> getAliases() {
		return Arrays.asList(getDxl().getAttribute("alias").split("\\|"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openntf.domino.design.DesignBase#getName()
	 */
	@Override
	public String getName() {
		return getDocumentElement().getAttribute("name");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openntf.domino.design.DesignBase#setAlias(java.lang.String)
	 */
	@Override
	public void setAlias(final String alias) {
		getDocumentElement().setAttribute("alias", alias);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openntf.domino.design.DesignBase#setAliases(java.lang.Iterable)
	 */
	@Override
	public void setAliases(final Iterable<String> aliases) {
		StringBuilder result = new StringBuilder();
		boolean added = false;
		for (String alias : aliases) {
			if (added)
				result.append("|");
			result.append(alias);
			added = true;
		}
		getDocumentElement().setAttribute("alias", result.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openntf.domino.design.DesignBase#setName(java.lang.String)
	 */
	@Override
	public void setName(final String name) {
		getDocumentElement().setAttribute("name", name);
	}
}
