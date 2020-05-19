package io.tomahawkd.tlstester.config;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import io.tomahawkd.tlstester.extensions.ExtensionManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

public class ArgConfigImpl extends AbstractArgDelegate implements ArgConfig {

	@Parameter(names = {"-h", "--help"}, help = true,
			description = "Prints usage for all the existing commands.")
	private boolean help;

	@HiddenField
	private JCommander c;
	@HiddenField
	private final List<ArgDelegate> delegates;

	public ArgConfigImpl() {
		delegates = new ArrayList<>();
		initComponents();
	}

	private void initComponents() {
		Set<Class<? extends ArgDelegate>> d =
				ExtensionManager.INSTANCE.loadClasses(ArgDelegate.class);

		for (Class<? extends ArgDelegate> aClass : d) {
			try {
				// already added
				if (this.getClass().equals(aClass)) continue;
				ArgDelegate delegate = aClass.newInstance();
				delegate.applyDelegate(this);
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
				throw new RuntimeException(
						"Error while loading argument delegate " + aClass.getName(), e);
			}
		}
	}

	@Override
	public void addDelegate(ArgDelegate delegate) {
		delegates.add(delegate);
	}

	@Override
	public void postParsing() {
		if (!help) {
			for (ArgDelegate delegate : delegates) {
				delegate.postParsing();
			}
		} else {
			c.usage();
		}
	}

	@SuppressWarnings("unchecked")
	@NotNull
	public <T extends ArgDelegate> T getByType(@NotNull Class<T> type) {
		for (ArgDelegate delegate : delegates) {
			if (type.equals(delegate.getClass())) return (T) delegate;
		}
		throw new NoSuchElementException("Type " + type.getName() + " not found.");
	}

	@Override
	public ArgDelegate getByString(String type) {
		for (ArgDelegate delegate : delegates) {
			if (type.equals(delegate.getClass().getName())) return delegate;
		}
		throw new NoSuchElementException("Type " + type + " not found.");
	}

	@Override
	public final void parseArgs(String[] args) {
		c = JCommander.newBuilder().addObject(this).addObject(delegates).build();

		try {
			c.parse(args);
			postParsing();
		} catch (ParameterException e) {
			System.err.println(e.getMessage());
			c.usage();
			throw e;
		}
	}

	public boolean isHelp() {
		return help;
	}
}
