package org.multiapp.client.log4j2;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.appender.rolling.RolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.tinyvfs.core.TVFSPaths;

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Plugin(name = "MyCustomAppender3", category = "Core", elementType = "appender", printObject = true)
public class MyCustomImpl extends AbstractAppender {


	private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
	private final Lock readLock = rwLock.readLock();
	private final String fileName;
	private final String filePattern;

	protected MyCustomImpl(String name, Filter filter,
	                       Layout<? extends Serializable> layout, final boolean ignoreExceptions,
	                       String fileName, String filePattern) {
		super(name, filter, layout, ignoreExceptions);
		this.fileName = fileName;
		this.filePattern = filePattern;
	}

	@PluginFactory
	public static MyCustomImpl createAppender(
			@PluginAttribute("name") String name,
			@PluginElement("Layout") Layout<? extends Serializable> layout,
			@PluginElement("Filter") final Filter filter,
			@PluginAttribute("otherAttribute") String otherAttribute,
			@PluginElement("Policy") TriggeringPolicy policy,
			@PluginElement("Strategy") RolloverStrategy strategy,
			@PluginAttribute("fileName") String fileName,
			@PluginAttribute("filePattern") String filePattern) {
		if (name == null) {
			LOGGER.error("No name provided for MyCustomAppenderImp");
			return null;
		}
		if (layout == null) {
			layout = PatternLayout.createDefaultLayout();
		}

//		if (strategy == null) {
//			strategy = DefaultRolloverStrategy.createStrategy((String)null, (String)null, (String)null, String.valueOf(-1), (Action[])null, true, this.configuration);
//		}
//
//		if (strategy == null) {
//			strategy = DefaultRolloverStrategy.createStrategy((String)null, (String)null, (String)null, String.valueOf(-1), (Action[])null, true, this.configuration);
//		}

		if (fileName == null) {
			fileName = "app.log";
		}

		return new MyCustomImpl(name, filter, layout, true, fileName, filePattern);

	}

	@Override
	public void append(LogEvent event) {
		readLock.lock();
		try {
			final byte[] bytes = getLayout().toByteArray(event);
// here I am printing logs into console
			System.out.println("LOG(" + fileName + "): " + new String(bytes, "UTF-8"));
			Path p = TVFSPaths.getAbsolutePath("log", fileName);
			Files.write(p, bytes, StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
		} catch (Exception ex) {
			if (!ignoreExceptions()) {
				throw new AppenderLoggingException(ex);
			}
		} finally {
			readLock.unlock();
		}
	}
}
