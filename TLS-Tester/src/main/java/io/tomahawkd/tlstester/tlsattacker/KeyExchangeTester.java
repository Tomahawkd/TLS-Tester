package io.tomahawkd.tlstester.tlsattacker;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.config.delegate.CiphersuiteDelegate;
import de.rub.nds.tlsattacker.core.config.delegate.ClientDelegate;
import de.rub.nds.tlsattacker.core.constants.CipherSuite;
import de.rub.nds.tlsattacker.core.protocol.message.*;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.DefaultWorkflowExecutor;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import de.rub.nds.tlsattacker.core.workflow.action.MessageAction;
import de.rub.nds.tlsattacker.core.workflow.action.ReceiveAction;
import de.rub.nds.tlsattacker.core.workflow.action.SendAction;
import io.tomahawkd.tlstester.InternalNamespaces;
import io.tomahawkd.tlstester.data.TargetInfo;
import io.tomahawkd.tlstester.data.testssl.parser.CipherInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;

import java.util.List;

public class KeyExchangeTester {

	private Config config;
	private WorkflowTrace trace;

	private static final Logger logger = LogManager.getLogger(KeyExchangeTester.class);

	public KeyExchangeTester(TargetInfo host) {

		logger.debug("Starting test key exchange on " + host.getHost());
		config = Config.createConfig();
		ClientDelegate delegate = new ClientDelegate();
		delegate.setHost(host.getHost());
		delegate.applyDelegate(config);
		String protocol = (String)
				host.getCollectedData().get(InternalNamespaces.Data.STARTTLS);
		if (protocol != null) {
			TesterHelper.setStarttlsProtocol(config, protocol);
		}

		trace = new WorkflowTrace();
	}

	@Contract("_ -> this")
	public KeyExchangeTester setCipherSuite(CipherSuite cipherSuite) {

		logger.debug("Set cipher suite " + cipherSuite.getValue());
		CiphersuiteDelegate ciphersuiteDelegate = new CiphersuiteDelegate();
		ciphersuiteDelegate.setCipherSuites(cipherSuite);
		ciphersuiteDelegate.applyDelegate(config);
		return this;
	}

	@Contract("_ -> this")
	public KeyExchangeTester setNegotiateVersion(CipherInfo.SSLVersion version) {

		logger.debug("Set ssl version " + version.getLevel());
		config.setHighestProtocolVersion(TesterHelper.getVersionForTest(version));
		return this;
	}

	public List<MessageAction> execute() {

		logger.debug("Executing...");
		State state = new State(config, trace);
		DefaultWorkflowExecutor executor = new DefaultWorkflowExecutor(state);
		executor.executeWorkflow();

		logger.debug("Complete");
		return trace.getMessageActions();
	}

	@Contract("-> this")
	public KeyExchangeTester initRSA() {

		logger.debug("Initializing RSA handshake");

		trace.reset();

		ClientHelloMessage clientHelloMessage = new ClientHelloMessage(config);

		trace.addTlsAction(new SendAction(clientHelloMessage));
		trace.addTlsAction(new ReceiveAction(
				new ServerHelloMessage(),
				new CertificateMessage(),
				new ServerHelloDoneMessage()));
		trace.addTlsAction(new SendAction(
				new RSAClientKeyExchangeMessage(),
				new ChangeCipherSpecMessage(),
				new FinishedMessage()));
		trace.addTlsAction(new ReceiveAction(
				new ChangeCipherSpecMessage(),
				new FinishedMessage()));
		return this;
	}

	@Contract("-> this")
	public KeyExchangeTester initECDHE() {

		logger.debug("Initializing ECDHE handshake");

		trace.reset();

		ClientHelloMessage clientHelloMessage = new ClientHelloMessage(config);

		trace.addTlsAction(new SendAction(clientHelloMessage));
		trace.addTlsAction(new ReceiveAction(
				new ServerHelloMessage(),
				new CertificateMessage(),
				new ECDHEServerKeyExchangeMessage(),
				new ServerHelloDoneMessage()));
		trace.addTlsAction(new SendAction(
				new ECDHClientKeyExchangeMessage(),
				new ChangeCipherSpecMessage(),
				new FinishedMessage()));
		trace.addTlsAction(new ReceiveAction(
				new ChangeCipherSpecMessage(),
				new FinishedMessage()));
		return this;
	}
}
