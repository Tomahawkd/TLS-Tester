package io.tomahawkd.tlstester.tlsattacker;

import de.rub.nds.tlsattacker.attacks.config.TLSPoodleCommandConfig;
import de.rub.nds.tlsattacker.attacks.config.delegate.GeneralAttackDelegate;
import de.rub.nds.tlsattacker.attacks.impl.TLSPoodleAttacker;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.config.delegate.GeneralDelegate;
import io.tomahawkd.tlstester.data.TargetInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TLSPoodleTester extends VulnerabilityTester {

	private static final Logger logger = LogManager.getLogger(TLSPoodleTester.class);

	public boolean test(TargetInfo host) {

		logger.info("Starting test tls-poodle on " + host.getHost());
		GeneralDelegate generalDelegate = new GeneralAttackDelegate();
		generalDelegate.setQuiet(true);

		TLSPoodleCommandConfig poodle = new TLSPoodleCommandConfig(generalDelegate);
		Config config = initConfig(host, poodle);

		return new TLSPoodleAttacker(poodle, config).isVulnerable();
	}
}
