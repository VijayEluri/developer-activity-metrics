package org.chaoticbits.devactivity.devnetwork.factory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.chaoticbits.devactivity.DBUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.mysql.jdbc.Connection;


public class LoadSVNtoDB {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LoadSVNtoDB.class);

	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	private File input;
	private DBUtil dbUtil;

	public LoadSVNtoDB(DBUtil dbUtil, File input) {
		this.dbUtil = dbUtil;
		this.input = input;
	}

	public void run() throws Exception {
		log.debug("Opening SVN XML document...");
		Document document = getXMLDocument(input);
		DocumentTraversal traversal = (DocumentTraversal) document;

		NodeIterator iterator = traversal.createNodeIterator(document.getDocumentElement(),
				NodeFilter.SHOW_ELEMENT, null, true);
		String author = "Oops - not loaded!";
		String revision = "Oops - not loaded!";
		Timestamp date = null;
		String message = "Oops - not loaded!";
		Connection conn = dbUtil.getConnection();
		PreparedStatement svnLogInsert = conn
				.prepareStatement("INSERT INTO SVNLog(Revision, AuthorName, AuthorDate, Message) "
						+ "VALUES (?,?,?,?)");
		PreparedStatement svnLogFilesInsert = conn
				.prepareStatement("INSERT INTO SVNLogFiles(Revision, Filepath, Action) " + "VALUES (?,?,?)");
		log.debug("Traversing the SVN XML document...");
		for (Node n = iterator.nextNode(); n != null; n = iterator.nextNode()) {
			if ("logentry".equals(n.getNodeName())) {
				revision = n.getAttributes().getNamedItem("revision").getNodeValue();
			} else if ("author".equals(n.getNodeName())) {
				author = n.getTextContent();
			} else if ("date".equals(n.getNodeName())) {
				date = parseDate(filter(n.getTextContent()));
			} else if ("msg".equals(n.getNodeName())) {
				message = n.getTextContent();
				svnLogInsert.setString(1, revision);
				svnLogInsert.setString(2, author);
				svnLogInsert.setTimestamp(3, date);
				if (message.length() > 5000) {
					log.warn("Message truncated for r" + revision + ", " + author + "[" + date + "]");
					message = message.substring(0, 5000);
				}
				svnLogInsert.setString(4, message);
				svnLogInsert.addBatch();
			} else if ("path".equals(n.getNodeName())) {
				String action = n.getAttributes().getNamedItem("action").getNodeValue();
				String filepath = n.getTextContent();
				filepath = filter(filepath);
				svnLogFilesInsert.setString(1, revision);
				svnLogFilesInsert.setString(2, filepath);
				svnLogFilesInsert.setString(3, action);
				svnLogFilesInsert.addBatch();
			}
		}
		log.debug("Executing batch inserts...");
		svnLogInsert.executeBatch();
		svnLogFilesInsert.executeBatch();
		DBUtil.closeConnection(conn, svnLogFilesInsert, svnLogInsert);
	}

	public static Document getXMLDocument(File string) throws ParserConfigurationException, SAXException,
			IOException, FileNotFoundException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new BufferedReader(new FileReader(string))));
		return document;
	}

	public static Timestamp parseDate(String dateStr) {
		try {
			if (dateStr.contains("2001-03-11")) {
				dateStr = "2001-03-11T03:08:32.000000Z"; // daylight savings bug
			}
			if (dateStr.contains("2000-03-12")) {
				dateStr = "2000-03-12T03:09:32.000000Z"; // daylight savings bug
			}
			if (dateStr.contains("2002-03-10")) {
				dateStr = "2002-03-10T03:09:32.000000Z"; // daylight savings bug
			}
			if (dateStr.contains("1998-03-08")) {
				dateStr = "1998-03-08T03:09:32.000000Z"; // daylight savings bug
			}
			java.util.Date parsedDate = DATE_FORMAT.parse(dateStr);
			return new Timestamp(parsedDate.getTime());
		} catch (ParseException e) {
			return null;
		}
	}

	private String filter(String filepath) {
		return filepath.trim();
	}
}
