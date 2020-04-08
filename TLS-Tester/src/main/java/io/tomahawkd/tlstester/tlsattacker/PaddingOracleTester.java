package io.tomahawkd.tlstester.tlsattacker;

import de.rub.nds.tlsattacker.attacks.config.Cve20162107CommandConfig;
import de.rub.nds.tlsattacker.attacks.config.delegate.GeneralAttackDelegate;
import de.rub.nds.tlsattacker.attacks.impl.Cve20162107Attacker;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.config.delegate.GeneralDelegate;
import io.tomahawkd.tlstester.data.TargetInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PaddingOracleTester extends VulnerabilityTester {

	private static final Logger logger = LogManager.getLogger(PaddingOracleTester.class);

	@SuppressWarnings("deprecated")
	public boolean test(TargetInfo host) {

		logger.info("Starting test OpenSSL's Padding Oracle on " + host.getHost());
		GeneralDelegate generalDelegate = new GeneralAttackDelegate();
		generalDelegate.setQuiet(true);

		Cve20162107CommandConfig pd = new Cve20162107CommandConfig(generalDelegate);
		Config config = initConfig(host, pd);

		//noinspection deprecation
		return new Cve20162107Attacker(pd, config).isVulnerable();
	}
}
