/*
 * Copyright Paul Withers, Intec 2011-2013
=======
 * Copyright 2013
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 *
 * Some significant enhancements here from the OpenNTF version
 *
 * 1. Everything is designed to work regardless of ExtLib packages
 *
 * 2. _logDbName and debug level set from logging.properties.
 *
 * 3. setThisAgent(boolean) method has been added. By default it gets the current page.
 * Otherwise it gets the previous page. Why? Because if we've been redirected to an error page,
 * we want to know which page the ACTUAL error occurred on.
 *
 *
 * Nathan T. Freeman, GBS Jun 20, 2011
 * Developers notes...
 *
 * Because the log methods are static, one simply needs to call..
 *
 * OpenLogItem.logError(session, throwable)
 *
 * or...
 *
 * OpenLogItem.logError(session, throwable, message, level, document)
 *
 * or...
 *
 * OpenLogItem.logEvent(session, message, level, document)
 * 
 */

package org.openntf.domino.logging;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import lotus.domino.NotesException;

import org.openntf.domino.Database;
import org.openntf.domino.Document;
import org.openntf.domino.ExceptionDetails;
import org.openntf.domino.ExceptionDetails.Entry;
import org.openntf.domino.RichTextItem;
import org.openntf.domino.Session;
import org.openntf.domino.utils.Factory;

import com.ibm.commons.util.StringUtil;

/**
 * @author withersp The Class OpenLogItem.
 * 
 */
public class LogItemOpenLog {

	private static enum LogType {
		TYPE_ERROR("Error"), TYPE_EVENT("Event");

		private final String value_;

		private LogType(final String value) {
			value_ = value;
		}

		public String getValue() {
			return value_;
		}
	}

	/*
	 * ======================================================= <HEADER> NAME: OpenLogClass script library VERSION: 20070321a AUTHOR(S):
	 * Julian Robichaux ( http://www.nsftools.com ) ORIGINAL SOURCE: The OpenLog database, available as an open-source project at
	 * http://www.OpenNTF.org HISTORY: 20070321a: Added startTime global to mark when this particular agent run began, so you can group
	 * multiple errors/events together more easily (see the "by Database and Start Time" view) 20060314a: fixed bug where logErrorEx would
	 * set the message to the severity type instead of the value of msg. 20041111a: made SEVERITY_ and TYPE_ constants public. 20040928a:
	 * added callingMethodDepth variable, which should be incremented by one in the Synchronized class so we'll get a proper reference to
	 * the calling method; make $PublicAccess = "1" when we create new log docs, so users with Depositor access to this database can still
	 * create log docs. 20040226a: removed synchronization from all methods in the main OpenLogItem class and added the new
	 * SynchronizedOpenLogItem class, which simply extends OpenLogItem and calls all the public methods as synchronized methods. Added
	 * olDebugLevel and debugOut public members to report on internal errors. 20040223a: add variables for user name, effective user name,
	 * access level, user roles, and client version; synchronized most of the public methods. 20040222a: this version got a lot less
	 * aggressive with the Notes object recycling, due to potential problems. Also made LogErrorEx and LogEvent return "" if an error
	 * occurred (to be consistent with LogError); added OpenLogItem(Session s) constructor; now get server name from the Session object, not
	 * the AgentContext object (due to differences in what those two things will give you); add UseDefaultLogDb and UseCustomLogDb methods;
	 * added getLogDatabase method, to be consistent with LotusScript functions; added useServerLogWhenLocal and logToCurrentDatabase
	 * variables/options 20040217a: this version made the agentContext object global and fixed a problem where the agentContext was being
	 * recycled in the constuctor (this is very bad) 20040214b: initial version
	 * 
	 * DISCLAIMER: This code is provided "as-is", and should be used at your own risk. The authors make no express or implied warranty about
	 * anything, and they will not be responsible or liable for any damage caused by the use or misuse of this code or its byproducts. No
	 * guarantees are made about anything.
	 * 
	 * That being said, you can use, modify, and distribute this code in any way you want, as long as you keep this header section intact
	 * and in a prominent place in the code. </HEADER> =======================================================
	 * 
	 * This class contains generic functions that can be used to log events and errors to the OpenLog database. All you have to do it copy
	 * this script library to any database that should be sending errors to the OpenLog database, and add it to your Java agents using the
	 * Edit Project button (see the "Using This Database" doc in the OpenLog database for more details).
	 * 
	 * At the beginning of your agent, create a global instance of this class like this:
	 * 
	 * private OpenLogItem oli = new OpenLogItem();
	 * 
	 * and then in all the try/catch blocks that you want to send errors from, add the line:
	 * 
	 * oli.logError(e);
	 * 
	 * where "e" is the Exception that you caught. That's all you have to do. The LogError method will automatically create a document in
	 * the OpenLog database that contains all sorts of information about the error that occurred, including the name of the agent and
	 * function/sub that it occurred in.
	 * 
	 * For additional functionality, you can use the LogErrorEx function to add a custom message, a severity level, and/or a link to a
	 * NotesDocument to the log doc.
	 * 
	 * In addition, you can use the LogEvent function to add a notification document to the OpenLog database.
	 * 
	 * You'll notice that I trap and discard almost all of the Exceptions that may occur as the methods in this class are running. This is
	 * because the class is normally only used when an error is occurring anyway, so there's not sense in trying to pass any new errors back
	 * up the stack.
	 * 
	 * The master copy of this script library resides in the OpenLog database. All copies of this library in other databases should be set
	 * to inherit changes from that database.
	 */

	/** The Constant serialVersionUID. */
	protected final String _logFormName = "LogEvent";
	protected String _logDbName = "";
	protected String _thisDatabase;
	protected String _thisServer;
	protected String _thisAgent;
	protected Boolean _logSuccess = true;
	protected String _accessLevel;
	protected Vector<Object> _userRoles;
	protected Vector<String> _clientVersion;
	protected Level _severity;
	protected String _eventType;
	protected String _message;
	protected Throwable _baseException;
	protected String _errDocUnid;
	protected List<ExceptionDetails.Entry> _exceptionDetails;
	protected String[] _lastWrappedDocs;
	protected transient Session _session;
	protected transient Database _logDb;
	protected transient Database _currentDatabase;
	protected transient Date _startTime;
	protected transient Date _eventTime;
	protected transient Document _errDoc;
	private String _currentDbPath;
	private static PrintStream debugOut = System.err;

	/*
	 * Constructor
	 */
	/**
	 * Instantiates a new open log item.
	 * 
	 * @since org.openntf.domino 1.0.0
	 */
	public LogItemOpenLog() {
		_startTime = new Date();
	}

	/* (non-Javadoc)
	 * @see org.openntf.domino.logging.OpenLogItem#setSession(org.openntf.domino.Session)
	 */

	private void setSession(final Session s) {
		_session = s;
	}

	/* (non-Javadoc)
	 * @see org.openntf.domino.logging.OpenLogItem#getThisServer()
	 */

	private String getThisServer() {
		if (_thisServer == null) {
			try {
				_thisServer = Factory.getSession().getServerName();
				if (_thisServer == null)
					_thisServer = "";
			} catch (Exception e) {
				debugPrint(e);
			}
		}
		return _thisServer;
	}

	private String getThisAgent() {
		if (_thisAgent == null) {
			_thisAgent = Factory.getRunContext().name();
		}
		return _thisAgent;
	}

	/* (non-Javadoc)
	 * @see org.openntf.domino.logging.OpenLogItem#getLogDb()
	 */

	private Database getLogDb() {
		if (_logDb == null) {
			try {
				_logDb = Factory.getSession().getDatabase(getThisServer(), _logDbName, false);
			} catch (Exception e) {
				debugPrint(e);
			}
		}
		// RPr: Locking is no longer supported
		//		} else {
		//			if (Base.isLocked(_logDb)) {
		//				_logDb = Factory.getSession().getDatabase(getThisServer(), getLogDbName(), false);
		//			}
		//		}
		return _logDb;
	}

	/* (non-Javadoc)
	 * @see org.openntf.domino.logging.OpenLogItem#getCurrentDatabase()
	 */

	private Database getCurrentDatabase() {
		/*
		 * BaseOpenLogItem gets shared between calls and _currentDatabase is resurrected.
		 * So check _currentDbPath variable is actual current path
		 */
		if (!StringUtil.equals(_currentDbPath, Factory.getSession().getCurrentDatabase().getFilePath())) {
			try {
				_currentDatabase = Factory.getSession().getCurrentDatabase();
				_currentDbPath = _currentDatabase.getFilePath();
			} catch (Exception e) {
				debugPrint(e);
			}
		}
		return _currentDatabase;
	}

	/* (non-Javadoc)
	 * @see org.openntf.domino.logging.OpenLogItem#getAccessLevel()
	 */

	private String getAccessLevel() {
		if (_accessLevel == null) {
			try {
				Database db = getCurrentDatabase();
				if (db != null) {
					switch (db.getCurrentAccessLevel()) {
					case 0:
						_accessLevel = "0: No Access";
						break;
					case 1:
						_accessLevel = "1: Depositor";
						break;
					case 2:
						_accessLevel = "2: Reader";
						break;
					case 3:
						_accessLevel = "3: Author";
						break;
					case 4:
						_accessLevel = "4: Editor";
						break;
					case 5:
						_accessLevel = "5: Designer";
						break;
					case 6:
						_accessLevel = "6: Manager";
						break;
					}
				}
			} catch (Exception e) {
				debugPrint(e);
			}
		}
		return _accessLevel;
	}

	/* (non-Javadoc)
	 * @see org.openntf.domino.logging.OpenLogItem#getUserRoles()
	 */

	private Vector<Object> getUserRoles() {
		if (_userRoles == null) {
			_userRoles = Factory.getSession().evaluate("@UserRoles");
		}
		return _userRoles;
	}

	/* (non-Javadoc)
	 * @see org.openntf.domino.logging.OpenLogItem#getClientVersion()
	 */

	private Vector<String> getClientVersion() {
		if (_clientVersion == null) {
			_clientVersion = new Vector<String>();
			try {
				String cver = Factory.getSession().getNotesVersion();
				if (cver != null) {
					if (cver.indexOf("|") > 0) {
						_clientVersion.addElement(cver.substring(0, cver.indexOf("|")));
						_clientVersion.addElement(cver.substring(cver.indexOf("|") + 1));
					} else {
						_clientVersion.addElement(cver);
					}
				}
			} catch (Exception e) {
				debugPrint(e);
			}
		}
		return _clientVersion;
	}

	/* (non-Javadoc)
	 * @see org.openntf.domino.logging.OpenLogItem#getMessage()
	 */

	private String getMessage() {
		if (_message != null && _message.length() > 0)
			return _message;
		String ret = null;
		if (_baseException != null)
			ret = _baseException.getMessage();
		return (ret == null) ? "" : ret;
	}

	/* (non-Javadoc)
	 * @see org.openntf.domino.logging.OpenLogItem#getErrDoc()
	 */

	private Document getErrDoc() {
		if (_errDoc == null && _errDocUnid != null) {
			try {
				_errDoc = getCurrentDatabase().getDocumentByUNID(_errDocUnid);
			} catch (Exception ee) {
				//Attempt to log for document not in current database
			}
		}
		return _errDoc;
	}

	/* (non-Javadoc)
	 * @see org.openntf.domino.logging.OpenLogItem#setErrDoc(org.openntf.domino.Document)
	 */

	private void setErrDoc(final Document doc) {
		if (doc != null) {
			_errDoc = doc;
			try {
				_errDocUnid = doc.getUniversalID();
			} catch (Exception ee) { // Added PW
				debugPrint(ee); // Added PW
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.openntf.domino.logging.OpenLogItem#setLogDbName(java.lang.String)
	 */

	void setLogDbName(final String newLogPath) {
		_logDbName = newLogPath;
	}

	/* (non-Javadoc)
	 * @see org.openntf.domino.logging.OpenLogItem#setEventType(org.openntf.domino.logging.OpenLogItem.LogType)
	 */

	private void setEventType(final LogType typeError) {
		_eventType = typeError.getValue();
	}

	void logError(final Session s, final LogRecord record, final LogRecordAdditionalInfo lrai) {
		try {
			setSession(s);
			_baseException = record.getThrown();
			_message = record.getMessage();
			Level l = record.getLevel();
			_severity = (l == null) ? Level.WARNING : l;
			_eventTime = new Date(record.getMillis());
			setEventType(LogType.TYPE_ERROR);
			setErrDoc(null);
			_exceptionDetails = lrai.getExceptionDetails();
			_lastWrappedDocs = lrai.getLastWrappedDocs();
			_logSuccess = writeToLog();

		} catch (Exception e) {
			debugPrint(e);
			_logSuccess = false;
		}
	}

	/* (non-Javadoc)
	 * @see org.openntf.domino.logging.OpenLogItem#getStackTrace(java.lang.Throwable, int)
	 */

	private ArrayList<String> getStackTrace(final Throwable ee) {
		ArrayList<String> v = new ArrayList<String>(32);
		if (ee == null) {
			v.add("***NO STACK TRACE***");
			return v;
		}
		try {
			StringWriter sw = new StringWriter();
			ee.printStackTrace(new PrintWriter(sw));
			StringTokenizer st = new StringTokenizer(sw.toString(), "\n");
			while (st.hasMoreTokens())
				v.add(st.nextToken().trim());
		} catch (Exception e) {
			debugPrint(e);
		}

		return v;
	}

	/* (non-Javadoc)
	 * @see org.openntf.domino.logging.OpenLogItem#writeToLog()
	 */

	private boolean writeToLog() {
		// exit early if there is no database
		Database db = getLogDb();
		if (db == null) {
			return false;
		}

		boolean retval = false;
		Document logDoc = null;
		RichTextItem rtitem = null;
		Database docDb = null;

		try {
			logDoc = db.createDocument();

			logDoc.appendItemValue("Form", _logFormName);

			if (_baseException != null) {
				StackTraceElement ste = _baseException.getStackTrace()[0];
				if (_baseException instanceof NotesException) {
					logDoc.replaceItemValue("LogErrorNumber", ((NotesException) _baseException).id);
					logDoc.replaceItemValue("LogErrorMessage", ((NotesException) _baseException).text);
				} else {
					// Fixed next line
					logDoc.replaceItemValue("LogErrorMessage", getMessage());
				}
				logDoc.replaceItemValue("LogErrorLine", ste.getLineNumber());
				logDoc.replaceItemValue("LogFromMethod", ste.getClassName() + "." + ste.getMethodName());
			}

			if (LogType.TYPE_EVENT.getValue().equals(_eventType)) {
				logDoc.replaceItemValue("LogStackTrace", getStackTrace(_baseException));
			} else {
				logDoc.replaceItemValue("LogStackTrace", getStackTrace(_baseException));
			}
			logDoc.replaceItemValue("LogSeverity", _severity.getName());
			logDoc.replaceItemValue("LogEventTime", _eventTime);
			logDoc.replaceItemValue("LogEventType", _eventType);
			logDoc.replaceItemValue("LogMessage", getMessage());
			logDoc.replaceItemValue("LogFromDatabase", getCurrentDatabase().getFilePath());
			logDoc.replaceItemValue("LogFromServer", getThisServer());
			logDoc.replaceItemValue("LogFromAgent", getThisAgent());
			// Fixed next line
			logDoc.replaceItemValue("LogAgentLanguage", "Java");
			logDoc.replaceItemValue("LogUserName", Factory.getSession().getUserName());
			logDoc.replaceItemValue("LogEffectiveName", Factory.getSession().getEffectiveUserName());
			logDoc.replaceItemValue("LogAccessLevel", getAccessLevel());
			logDoc.replaceItemValue("LogUserRoles", getUserRoles());
			logDoc.replaceItemValue("LogClientVersion", getClientVersion());
			logDoc.replaceItemValue("LogAgentStartTime", _startTime);
			if (_exceptionDetails == null) {
				logDoc.replaceItemValue("LogExceptionDetails", "* Not available *");
			} else {
				Vector<String> v = new Vector<String>(_exceptionDetails.size());
				for (Entry ed : _exceptionDetails) {
					v.add(ed.toString());
				}
				logDoc.replaceItemValue("LogExceptionDetails", v);
			}
			if (_lastWrappedDocs == null) {
				logDoc.replaceItemValue("LogLastWrappedDocuments", "* Not available *");
			} else {
				Vector<String> v = new Vector<String>(_lastWrappedDocs.length);
				for (String docID : _lastWrappedDocs) {
					v.add(docID.toString());
				}
				logDoc.replaceItemValue("LogLastWrappedDocuments", v);
			}
			if (getErrDoc() != null) {
				docDb = getErrDoc().getParentDatabase();
				rtitem = logDoc.createRichTextItem("LogDocInfo");
				rtitem.appendText("The document associated with this event is:");
				rtitem.addNewLine(1);
				rtitem.appendText("Server: " + docDb.getServer());
				rtitem.addNewLine(1);
				rtitem.appendText("Database: " + docDb.getFilePath());
				rtitem.addNewLine(1);
				rtitem.appendText("UNID: " + getErrDoc().getUniversalID());
				rtitem.addNewLine(1);
				rtitem.appendText("Note ID: " + getErrDoc().getNoteID());
				rtitem.addNewLine(1);
				rtitem.appendText("DocLink: ");
				rtitem.appendDocLink(_errDoc, getErrDoc().getUniversalID());
			}

			// make sure Depositor-level users can add documents too
			logDoc.appendItemValue("$PublicAccess", "1");

			logDoc.save(true);
			retval = true;
		} catch (Exception e) {
			debugPrint(e);
			retval = false;
		}

		return retval;
	}

	/* (non-Javadoc)
	 * @see org.openntf.domino.logging.OpenLogItem#debugPrint(java.lang.Throwable)
	 */

	private void debugPrint(final Throwable ee) {
		if ((ee == null) || (debugOut == null))
			return;

		String debugMsg = ee.toString();
		if (ee instanceof NotesException) {
			NotesException ne = (NotesException) ee;
			debugMsg = "Notes error " + ne.id + ": " + ne.text;
		}
		debugOut.println("OpenLogItem error: " + debugMsg);

		debugOut.print("OpenLogItem error trace: ");
		ee.printStackTrace(debugOut);
	}
}
