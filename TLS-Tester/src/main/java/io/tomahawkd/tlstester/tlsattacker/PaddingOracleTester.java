package io.tomahawkd.tlstester.tlsattacker;

import de.rub.nds.tlsattacker.attacks.config.PaddingOracleCommandConfig;
import de.rub.nds.tlsattacker.attacks.config.delegate.GeneralAttackDelegate;
import de.rub.nds.tlsattacker.attacks.impl.PaddingOracleAttacker;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.config.delegate.GeneralDelegate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PaddingOracleTester extends VulnerabilityTester {

	private static final Logger logger = LogManager.getLogger(PaddingOracleTester.class);

	public boolean test(String host) {

		if (host.split(":").length == 1) host = host + ":" + DEFAULT_PORT;

		logger.info("Starting test OpenSSL's Padding Oracle on " + host);
		GeneralDelegate generalDelegate = new GeneralAttackDelegate();
		generalDelegate.setQuiet(true);

		PaddingOracleCommandConfig pd = new PaddingOracleCommandConfig(generalDelegate);
		Config config = initConfig(host, pd);

		return new PaddingOracleAttacker(pd, config).isVulnerable();
	}
}
