package io.tomahawkd.tlstester.tlsattacker;

import de.rub.nds.tlsattacker.attacks.config.GeneralDrownCommandConfig;
import de.rub.nds.tlsattacker.attacks.config.delegate.GeneralAttackDelegate;
import de.rub.nds.tlsattacker.attacks.impl.drown.GeneralDrownAttacker;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.config.delegate.GeneralDelegate;
import io.tomahawkd.tlstester.data.TargetInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DrownTester extends VulnerabilityTester {

	private static final Logger logger = LogManager.getLogger(DrownTester.class);

	@Override
	public boolean test(TargetInfo host) {

		logger.info("Starting test DROWN on " + host.getHost());
		GeneralDelegate generalDelegate = new GeneralAttackDelegate();
		generalDelegate.setQuiet(true);

		GeneralDrownCommandConfig drown = new GeneralDrownCommandConfig(generalDelegate);
		Config config = initConfig(host, drown);

		return new GeneralDrownAttacker(drown, config).isVulnerable();
	}
}
