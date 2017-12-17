package org.multiapp.client.log4j2;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractOutputStreamAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.appender.OutputStreamManager;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Plugin(name = "MyCustomAppender", category = "Core", elementType = "appender", printObject = true)
public class CustomAppender extends AbstractOutputStreamAppender {

	private static OutputStream out = System.out;
	private static OutputStreamManagerFactory factory = new OutputStreamManagerFactory();
	private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
	private final Lock readLock = rwLock.readLock();

//	@PluginFactory
//	public static CustomAppender createAppender(@PluginAttribute("name") String name,
//	                                            @PluginAttribute("ignoreExceptions") boolean ignoreExceptions,
//	                                            @PluginAttribute("immediateFlush") boolean immediateFlush,
//	                                            @PluginElement("Layout") Layout layout,
//	                                            @PluginElement("Filters") Filter filter) {
//
//		if (name == null) {
//			LOGGER.error("No name provided for StubAppender");
//			return null;
//		}
//
//		OutputStreamManager manager = OutputStreamManager.StubManager.getStubManager(name);
//		if (manager == null) {
//			return null;
//		}
//		if (layout == null) {
//			layout = PatternLayout.createDefaultLayout();
//		}
//		return new CustomAppender(name, layout, filter, ignoreExceptions, immediateFlush, manager);
//	}


	protected CustomAppender(String name, Layout layout, Filter filter, boolean ignoreExceptions, boolean immediateFlush, OutputStreamManager manager) {
		super(name, layout, filter, ignoreExceptions, immediateFlush, manager);
	}

//	// Your custom appender needs to declare a factory method
//	// annotated with `@PluginFactory`. Log4j will parse the configuration
//	// and call this factory method to construct an appender instance with
//	// the configured attributes.
//	@PluginFactory
//	public static CustomAppender createAppender(
//			@PluginAttribute("name") String name,
//			@PluginElement("Layout") Layout<? extends Serializable> layout,
//			@PluginElement("Filter") final Filter filter,
//			@PluginAttribute("otherAttribute") String otherAttribute) {
//		if (name == null) {
//			LOGGER.error("No name provided for MyCustomAppenderImpl");
//			return null;
//		}
//		if (layout == null) {
//			layout = PatternLayout.createDefaultLayout();
//		}
//		ManagerFactory<OutputStreamManager, Object> manager = new ManagerFactory<OutputStreamManager, Object>() {
//			@Override
//			public OutputStreamManager createManager(String s, Object o) {
//				return null;
//			}
//		};
//		//OutputStreamManager outputStreamManager=OutputStreamManager.getManager();
//		OutputStreamManager outputStreamManager = OutputStreamManager.getManager(name, manager, null);
//		//OutputStreamManager outputStreamManager=new OutputStreamManager(out,"toto",layout,true);
//
//		//StubManager manager = StubManager.getStubManager(name);
//
//		return new CustomAppender(name, layout, filter, true, true, outputStreamManager);
//	}

	/**
	 * Creates an OutputStream Appender.
	 *
	 * @param layout The layout to use or null to get the default layout.
	 * @param filter The Filter or null.
	 * @param target an output stream.
	 * @param follow If true will follow changes to the underlying output stream.
	 *               Use false as the default.
	 * @param name   The name of the Appender (required).
	 * @param ignore If {@code "true"} (default) exceptions encountered when
	 *               appending events are logged; otherwise they are propagated to
	 *               the caller. Use true as the default.
	 * @return The ConsoleAppender.
	 */
	@PluginFactory
	public static CustomAppender createAppender(Layout<? extends Serializable> layout, final Filter filter,
	                                            final OutputStream target, final String name,
	                                            final boolean follow, final boolean ignore) {
		if (name == null) {
			LOGGER.error("No name provided for OutputStreamAppender");
			return null;
		}
		if (layout == null) {
			layout = PatternLayout.createDefaultLayout();
		}
		return new CustomAppender(name, layout, filter, ignore, true, getManager(target, follow, layout));
	}

	private static OutputStreamManager getManager(final OutputStream target, final boolean follow,
	                                              final Layout<? extends Serializable> layout) {
		//final OutputStream os = new CloseShieldOutputStream(target);
		final OutputStream os = target;
		final String managerName = target.getClass().getName() + "@" + Integer.toHexString(target.hashCode()) + '.'
				+ follow;
		return OutputStreamManager.getManager(managerName, new FactoryData(os, managerName, layout), factory);
	}

	@PluginBuilderFactory
	public static Builder newBuilder() {
		return new Builder();
	}

	// The append method is where the appender does the work.
	// Given a log event, you are free to do with it what you want.
	// This example demonstrates:
	// 1. Concurrency: this method may be called by multiple threads concurrently
	// 2. How to use layouts
	// 3. Error handling
	@Override
	public void append(LogEvent event) {
		readLock.lock();
		try {
			final byte[] bytes = getLayout().toByteArray(event);
			System.out.write(bytes);
		} catch (Exception ex) {
			if (!ignoreExceptions()) {
				throw new AppenderLoggingException(ex);
			}
		} finally {
			readLock.unlock();
		}
	}

	private static class FactoryData {
		private final Layout<? extends Serializable> layout;
		private final String name;
		private final OutputStream os;

		/**
		 * Builds instances.
		 *
		 * @param os     The OutputStream.
		 * @param type   The name of the target.
		 * @param layout A Serializable layout
		 */
		public FactoryData(final OutputStream os, final String type, final Layout<? extends Serializable> layout) {
			this.os = os;
			this.name = type;
			this.layout = layout;
		}
	}

	/**
	 * Creates the manager.
	 */
	private static class OutputStreamManagerFactory implements ManagerFactory<OutputStreamManager, FactoryData> {

		/**
		 * Creates an OutputStreamManager.
		 *
		 * @param name The name of the entity to manage.
		 * @param data The data required to create the entity.
		 * @return The OutputStreamManager
		 */
		@Override
		public OutputStreamManager createManager(final String name, final FactoryData data) {
			//return new OutputStreamManager(data.os, data.name, data.layout, true);
			//return OutputStreamManager.getManager(name, this, data);
			//return CustomOutputStreamManager.getManager(name, this, data);
			return new CustomOutputStreamManager(out, name, data.layout, true);
		}
	}

	public static class Builder implements org.apache.logging.log4j.core.util.Builder<CustomAppender> {

		private Filter filter;

		private boolean follow = false;

		private boolean ignoreExceptions = true;

		private Layout<? extends Serializable> layout = PatternLayout.createDefaultLayout();

		private String name;

		private OutputStream target = System.out;

		@Override
		public CustomAppender build() {
			return new CustomAppender(name, layout, filter, ignoreExceptions, false, getManager(target, follow, layout));
		}

		public Builder setFilter(final Filter aFilter) {
			this.filter = aFilter;
			return this;
		}

		public Builder setFollow(final boolean shouldFollow) {
			this.follow = shouldFollow;
			return this;
		}

		public Builder setIgnoreExceptions(final boolean shouldIgnoreExceptions) {
			this.ignoreExceptions = shouldIgnoreExceptions;
			return this;
		}

		public Builder setLayout(final Layout<? extends Serializable> aLayout) {
			this.layout = aLayout;
			return this;
		}

		public Builder setName(final String aName) {
			this.name = aName;
			return this;
		}

		public Builder setTarget(final OutputStream aTarget) {
			this.target = aTarget;
			return this;
		}
	}

	private static class CustomOutputStreamManager extends OutputStreamManager {

		private OutputStream os;
		private byte[] footer = null;

		protected CustomOutputStreamManager(OutputStream os, String streamName, Layout<?> layout, boolean writeHeader) {
			super(os, streamName, layout, writeHeader);
			this.os = os;
		}
		/**
		 * Create a Manager.
		 * @param name The name of the stream to manage.
		 * @param factory The factory to use to create the Manager.
		 * @param data The data to pass to the Manager.
		 * @return An OutputStreamManager.
		 */
//		public static OutputStreamManager getManager(String name, ManagerFactory<OutputStreamManager, Object> factory,
//		                                             Object data) {
//			return AbstractManager.getManager(name, factory, data);
//		}

//		/**
//		 * Set the header to write when the stream is opened.
//		 *
//		 * @param header The header.
//		 */
//		public synchronized void setHeader(byte[] header) {
//			if (header != null) {
//				try {
//					this.os.write(header, 0, header.length);
//				} catch (IOException ioe) {
//					LOGGER.error("Unable to write header", ioe);
//				}
//			}
//		}
//
//		/**
//		 * Set the footer to write when the stream is closed.
//		 *
//		 * @param footer The footer.
//		 */
//		public synchronized void setFooter(byte[] footer) {
//			if (footer != null) {
//				this.footer = footer;
//			}
//		}
//
//		/**
//		 * Default hook to write footer during close.
//		 */
//		public void releaseSub() {
//			if (footer != null) {
//				write(footer);
//			}
//			close();
//		}
//
//		/**
//		 * Return the status of the stream.
//		 *
//		 * @return true if the stream is open, false if it is not.
//		 */
//		public boolean isOpen() {
//			return getCount() > 0;
//		}
//
//		protected OutputStream getOutputStream() {
//			return os;
//		}
//
//		protected void setOutputStream(OutputStream os) {
//			this.os = os;
//		}
//
//		/**
//		 * Some output streams synchronize writes while others do not. Synchronizing here insures that
//		 * log events won't be intertwined.
//		 *
//		 * @param bytes  The serialized Log event.
//		 * @param offset The offset into the byte array.
//		 * @param length The number of bytes to write.
//		 * @throws AppenderRuntimeException if an error occurs.
//		 */
//		protected synchronized void write(byte[] bytes, int offset, int length) {
//			//System.out.println("write " + count);
//			try {
//				os.write(bytes, offset, length);
//			} catch (IOException ex) {
//				String msg = "Error writing to stream " + getName();
//				throw new RuntimeException(msg, ex);
//			}
//		}

		/**
		 * Some output streams synchronize writes while others do not. Synchronizing here insures that
		 * log events won't be intertwined.
		 *
		 * @param bytes The serialized Log event.
		 * @throws AppenderRuntimeException if an error occurs.
		 */
//		protected void write(byte[] bytes) {
//			write(bytes, 0, bytes.length);
//		}
//
//		public void close() {
//			if (os == System.out || os == System.err) {
//				return;
//			}
//			try {
//				os.close();
//			} catch (IOException ex) {
//				LOGGER.error("Unable to close stream " + getName() + ". " + ex);
//			}
//		}
//
//		/**
//		 * Flush any buffers.
//		 */
//		public void flush() {
//			try {
//				os.flush();
//			} catch (IOException ex) {
//				String msg = "Error flushing stream " + getName();
//				throw new RuntimeException(msg, ex);
//			}
//		}
	}

}
