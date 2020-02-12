package io.tomahawkd.tlsattacker;

import de.rub.nds.tlsattacker.attacks.config.Cve20162107CommandConfig;
import de.rub.nds.tlsattacker.attacks.config.delegate.GeneralAttackDelegate;
import de.rub.nds.tlsattacker.attacks.impl.Cve20162107Attacker;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.config.delegate.GeneralDelegate;
import io.tomahawkd.common.log.Logger;

public class CveTester extends VulnerabilityTester {

	private static final Logger logger = Logger.getLogger(CveTester.class);

	public boolean test(String host) {

		if (host.split(":").length == 1) host = host + ":" + DEFAULT_PORT;

		logger.info("Starting test cve-2016-2107 on " + host);
		GeneralDelegate generalDelegate = new GeneralAttackDelegate();
		generalDelegate.setQuiet(true);

		Cve20162107CommandConfig cve20162107 = new Cve20162107CommandConfig(generalDelegate);
		Config config = initConfig(host, cve20162107);

		return new Cve20162107Attacker(cve20162107, config).isVulnerable();
	}
}
