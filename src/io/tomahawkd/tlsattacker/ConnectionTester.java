package io.tomahawkd.tlsattacker;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.config.delegate.CiphersuiteDelegate;
import de.rub.nds.tlsattacker.core.config.delegate.ClientDelegate;
import de.rub.nds.tlsattacker.core.constants.CipherSuite;
import de.rub.nds.tlsattacker.core.constants.HandshakeMessageType;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.DefaultWorkflowExecutor;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTraceUtil;
import de.rub.nds.tlsattacker.core.workflow.factory.WorkflowTraceType;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.testssl.data.parser.CipherInfo;
import org.jetbrains.annotations.Contract;

public class ConnectionTester {

	private Config config;

	private static final String DEFAULT_PORT = "443";

	private static final Logger logger = Logger.getLogger(ConnectionTester.class);

	public ConnectionTester(String host) {

		if (host.split(":").length == 1) host = host + ":" + DEFAULT_PORT;

		logger.info("Starting test key exchange on " + host);
		config = Config.createConfig();
		ClientDelegate delegate = new ClientDelegate();
		delegate.setHost(host);
		delegate.applyDelegate(config);
		config.setWorkflowTraceType(WorkflowTraceType.HELLO);
	}

	@Contract("_ -> this")
	public ConnectionTester setNegotiateVersion(CipherInfo.SSLVersion version) {
		logger.info("Set ssl version " + version.getLevel());
		config.setHighestProtocolVersion(version.getVersionForTest());
		return this;
	}

	@Contract("_ -> this")
	public ConnectionTester setCipherSuite(CipherSuite cipherSuite) {

		logger.info("Set cipher suite " + cipherSuite.getValue());
		CiphersuiteDelegate ciphersuiteDelegate = new CiphersuiteDelegate();
		ciphersuiteDelegate.setCipherSuites(cipherSuite);
		ciphersuiteDelegate.applyDelegate(config);
		return this;
	}

	public boolean isServerHelloReceived() {

		logger.debug("Executing...");
		State state = new State(config);
		DefaultWorkflowExecutor executor = new DefaultWorkflowExecutor(state);
		executor.executeWorkflow();

		logger.debug("Complete");
		return WorkflowTraceUtil
				.didReceiveMessage(HandshakeMessageType.SERVER_HELLO, state.getWorkflowTrace());
	}
}
