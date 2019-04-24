package io.tomahawkd.tlsattacker;

import de.rub.nds.tlsattacker.attacks.config.HeartbleedCommandConfig;
import de.rub.nds.tlsattacker.attacks.config.delegate.GeneralAttackDelegate;
import de.rub.nds.tlsattacker.attacks.impl.HeartbleedAttacker;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.config.delegate.ClientDelegate;
import de.rub.nds.tlsattacker.core.config.delegate.GeneralDelegate;
import io.tomahawkd.common.log.Logger;

public class HeartBleedTester {

	private static final String DEFAULT_PORT = "443";

	private static final Logger logger = Logger.getLogger(KeyExchangeTester.class);

	public static boolean test(String host) {

		if (host.split(":").length == 1) host = host + ":" + DEFAULT_PORT;

		logger.info("Starting test heartbleed on " + host);
		GeneralDelegate generalDelegate = new GeneralAttackDelegate();
		generalDelegate.setQuiet(true);

		HeartbleedCommandConfig heartbleed = new HeartbleedCommandConfig(generalDelegate);
		ClientDelegate delegate = (ClientDelegate) heartbleed.getDelegate(ClientDelegate.class);
		delegate.setHost(host);

		Config config = heartbleed.createConfig();
		delegate.applyDelegate(config);

		return new HeartbleedAttacker(heartbleed, config).isVulnerable();
	}
}
