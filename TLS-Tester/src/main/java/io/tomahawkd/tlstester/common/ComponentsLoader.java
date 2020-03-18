package io.tomahawkd.tlstester.common;

import io.tomahawkd.tlstester.config.ArgConfigurator;
import io.tomahawkd.tlstester.config.ExtensionArgDelegate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public enum ComponentsLoader {

	INSTANCE;

	private Logger logger = LogManager.getLogger(ComponentsLoader.class);
	private List<ClassLoader> classLoadersList;

	ComponentsLoader() {
		classLoadersList = new ArrayList<>();
		classLoadersList.add(ClasspathHelper.contextClassLoader());
		classLoadersList.add(ClasspathHelper.staticClassLoader());
	}

	public void loadExtensions() {
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
}
