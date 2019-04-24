package io.tomahawkd.tlsattacker;

import de.rub.nds.tlsattacker.attacks.config.TLSPoodleCommandConfig;
import de.rub.nds.tlsattacker.attacks.config.delegate.GeneralAttackDelegate;
import de.rub.nds.tlsattacker.attacks.impl.TLSPoodleAttacker;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.config.delegate.GeneralDelegate;
import io.tomahawkd.common.log.Logger;

public class TLSPoodleTester extends VulnerabilityTester {

	private static final Logger logger = Logger.getLogger(TLSPoodleTester.class);

	public boolean test(String host) {

		if (host.split(":").length == 1) host = host + ":" + DEFAULT_PORT;

		logger.info("Starting test tls-poodle on " + host);
		GeneralDelegate generalDelegate = new GeneralAttackDelegate();
		generalDelegate.setQuiet(true);

		TLSPoodleCommandConfig poodle = new TLSPoodleCommandConfig(generalDelegate);
		Config config = initConfig(host, poodle);

		return new TLSPoodleAttacker(poodle, config).isVulnerable();
	}
}
