package io.tomahawkd.tlstester.extensions;

import io.tomahawkd.tlstester.common.FileHelper;
import io.tomahawkd.tlstester.config.ArgConfigurator;
import io.tomahawkd.tlstester.config.ExtensionArgDelegate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public enum ExtensionManager {

	INSTANCE;

	private final Map<Class<? extends ExtensionHandler>, ExtensionHandler> handlers;
	private final List<ClassLoader> classLoadersList;

	private final Logger logger = LogManager.getLogger(ExtensionManager.class);

	ExtensionManager() {
		handlers = new HashMap<>();
		classLoadersList = new ArrayList<>();

		classLoadersList.add(ClasspathHelper.staticClassLoader());
		classLoadersList.add(ClasspathHelper.contextClassLoader());

		// init handler
		loadClasses(ExtensionHandler.class).stream()
				.filter(e -> !Modifier.isInterface(e.getModifiers()) ||
						!Modifier.isAbstract(e.getModifiers()))
				.forEach(e -> {
					try {
						logger.debug("Loading handler {}.", e.getName());
						handlers.put(e, e.newInstance());
					} catch (InstantiationException | IllegalAccessException e1) {
						logger.fatal("Unable to instantiate {}", e.getName(), e1);
						throw new RuntimeException(e1);
					}
				});
	}

	@SuppressWarnings("unchecked")
	public void loadComponents() {
		loadExtensionClassloader();

		outer:
		for (Class<? extends ExtensionPoint> e : loadClasses(ExtensionPoint.class)) {
			if (Modifier.isInterface(e.getModifiers()) ||
					Modifier.isAbstract(e.getModifiers())) continue;

			logger.debug("Processing extension {}", e.getName());

			if (ParameterizedExtensionPoint.class.isAssignableFrom(e)) {
				for (ExtensionHandler handler : handlers.values()) {
					if (!handler.canAccepted(e)) continue;

					if (handler instanceof ParameterizedExtensionHandler) {
						if (((ParameterizedExtensionHandler) handler)
								.accept((Class<ParameterizedExtensionPoint>) e))
							continue outer;
					}
				}
			} else {

				if (Arrays.stream(e.getConstructors())
						.filter(ep -> ep.getParameterCount() == 0).count() < 1) {
					logger.error("Extension point constructor with no param not found, skipping.");
					continue;
				}
				try {
					ExtensionPoint extensionPoint = e.newInstance();
					for (ExtensionHandler handler : handlers.values()) {
						if (!handler.canAccepted(e)) continue;
						if (handler.accept(extensionPoint)) continue outer;
					}
				} catch (InstantiationException | IllegalAccessException e1) {
					logger.fatal("Unable to instantiate {}", e.getName(), e1);
					throw new RuntimeException(e1);
				}
			}
			logger.warn("Extension {} is not accept by any handler.", e.getName());
		}

		for (ExtensionHandler handler : handlers.values()) {
			handler.postInitialization();
		}
	}

	private void loadExtensionClassloader() {
		ExtensionArgDelegate delegate =
				ArgConfigurator.INSTANCE.getByType(ExtensionArgDelegate.class);

		if (delegate.isSafeMode()) {
			logger.info("Running in safe mode, all extensions will be ignored.");
			return;
		}

		String path = delegate.getExtensionPath();
		if (!FileHelper.isDirExist(path)) {
			logger.warn("Directory {} not found, skip loading extensions.", path);
			return;
		}

		try {
			URL[] urls = Files.list(Paths.get(path))
					.peek(p -> logger.debug("Find file: " + p.toAbsolutePath().toString()))
					.filter(p -> p.toString().endsWith(".jar"))
					.peek(p -> logger.debug("Loading jar " + p.toAbsolutePath().toString()))
					.map(p -> {
						try {
							return new URL("file:" + p.toAbsolutePath().toString());
						} catch (MalformedURLException e) {
							logger.warn(
									"Malformed URL file:" + p.toAbsolutePath().toString(),
									e);
							return null;
						}
					}).toArray(URL[]::new);
			URLClassLoader child = new URLClassLoader(urls, this.getClass().getClassLoader());
			classLoadersList.add(child);
			ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0]))
					.forEach(u -> logger.debug("Classpath: " + u));
		} catch (IOException e) {
			logger.warn("Failed to load file", e);
		}
	}

	// util functions

	@SuppressWarnings("unchecked")
	public <T extends ExtensionHandler> T get(Class<T> type) {
		return (T) handlers.get(type);
	}

	public Set<Class<?>> loadClassesByAnnotation(Class<? extends Annotation> annotation) {
		Reflections reflections = new Reflections(new ConfigurationBuilder()
				.setUrls(ClasspathHelper
						.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
				.addClassLoaders(classLoadersList));
		return reflections.getTypesAnnotatedWith(annotation);
	}

	public <T> Set<Class<? extends T>> loadClasses(Class<T> superClass) {
		Reflections reflections = new Reflections(new ConfigurationBuilder()
				.setScanners(new SubTypesScanner(true))
				.setUrls(ClasspathHelper
						.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
				.addClassLoaders(classLoadersList));
		return reflections.getSubTypesOf(superClass);
	}

	public <T> Set<Class<? extends T>> loadClasses(Class<T> superClass, Package packageName) {
		Reflections reflections = new Reflections(new ConfigurationBuilder()
				.setScanners(new SubTypesScanner(true))
				.setUrls(ClasspathHelper
						.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
				.filterInputsBy(
						new FilterBuilder().include(FilterBuilder.prefix(packageName.getName())))
				.addClassLoaders(classLoadersList));
		return reflections.getSubTypesOf(superClass);
	}

	@Nullable
	public Class<?> loadClass(String clazz) {

		logger.debug("Load class {}", clazz);

		Class<?> c = null;
		for (ClassLoader classLoader : classLoadersList) {
			try {
				c = classLoader.loadClass(clazz);
				break;
			} catch (ClassNotFoundException e) {
				logger.debug("Class {} not found in classloader {}",
						clazz, classLoader.getClass().getName());
			}
		}

		return c;
	}
}
