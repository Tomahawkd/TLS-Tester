package io.tomahawkd.tlstester.tlsattacker;

import de.rub.nds.tlsattacker.attacks.config.HeartbleedCommandConfig;
import de.rub.nds.tlsattacker.attacks.config.delegate.GeneralAttackDelegate;
import de.rub.nds.tlsattacker.attacks.impl.HeartbleedAttacker;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.config.delegate.GeneralDelegate;
import io.tomahawkd.tlstester.data.TargetInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HeartBleedTester extends VulnerabilityTester {

	private static final Logger logger = LogManager.getLogger(HeartBleedTester.class);

	public boolean test(TargetInfo host) {

		logger.info("Starting test heartbleed on " + host.getHost());
		GeneralDelegate generalDelegate = new GeneralAttackDelegate();
		generalDelegate.setQuiet(true);

		HeartbleedCommandConfig heartbleed = new HeartbleedCommandConfig(generalDelegate);
		Config config = initConfig(host, heartbleed);

		try {
			return new HeartbleedAttacker(heartbleed, config).isVulnerable();
		} catch (NullPointerException e) {
			logger.error("Heart bleed test did not receive finish message, assuming false");
			return false;
		}
	}
}
