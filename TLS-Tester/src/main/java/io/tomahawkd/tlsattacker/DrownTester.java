package io.tomahawkd.tlsattacker;

import de.rub.nds.tlsattacker.attacks.config.DrownCommandConfig;
import de.rub.nds.tlsattacker.attacks.config.delegate.GeneralAttackDelegate;
import de.rub.nds.tlsattacker.attacks.impl.DrownAttacker;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.config.delegate.GeneralDelegate;
import io.tomahawkd.common.log.Logger;

public class DrownTester extends VulnerabilityTester {

	private static final Logger logger = Logger.getLogger(DrownTester.class);

	@Override
	public boolean test(String host) {

		if (host.split(":").length == 1) host = host + ":" + DEFAULT_PORT;

		logger.info("Starting test DROWN on " + host);
		GeneralDelegate generalDelegate = new GeneralAttackDelegate();
		generalDelegate.setQuiet(true);

		DrownCommandConfig drown = new DrownCommandConfig(generalDelegate);
		Config config = initConfig(host, drown);

		return new DrownAttacker(drown, config).isVulnerable();
	}
}
