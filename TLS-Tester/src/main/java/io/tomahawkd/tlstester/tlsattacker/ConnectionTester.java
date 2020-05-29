package io.tomahawkd.tlstester.tlsattacker;

import de.rub.nds.modifiablevariable.bytearray.ModifiableByteArray;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.config.delegate.CiphersuiteDelegate;
import de.rub.nds.tlsattacker.core.config.delegate.ClientDelegate;
import de.rub.nds.tlsattacker.core.constants.CipherSuite;
import de.rub.nds.tlsattacker.core.constants.HandshakeMessageType;
import de.rub.nds.tlsattacker.core.constants.ProtocolMessageType;
import de.rub.nds.tlsattacker.core.protocol.message.ClientHelloMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.DefaultWorkflowExecutor;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTraceUtil;
import de.rub.nds.tlsattacker.core.workflow.action.SendAction;
import de.rub.nds.tlsattacker.core.workflow.factory.WorkflowTraceType;
import io.tomahawkd.tlstester.data.testssl.parser.CipherInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;

import java.util.List;

public class ConnectionTester {

	private static final String DEFAULT_PORT = "443";
	private static final Logger logger = LogManager.getLogger(ConnectionTester.class);

	private Config config;
	private WorkflowTrace trace;

	public ConnectionTester(String host) {

		if (host.split(":").length == 1) host = host + ":" + DEFAULT_PORT;

		logger.info("Starting test connection on " + host);
		config = Config.createConfig();
		ClientDelegate delegate = new ClientDelegate();
		delegate.setHost(host);
		delegate.applyDelegate(config);
		config.setWorkflowTraceType(WorkflowTraceType.HELLO);
	}

	@Contract("_ -> this")
	public ConnectionTester setNegotiateVersion(CipherInfo.SSLVersion version) {
		logger.info("Set ssl version " + version.getLevel());
		config.setHighestProtocolVersion(TesterHelper.getVersionForTest(version));
		return this;
	}

	public void setStarttlsProtocol(String protocol) {
		TesterHelper.setStarttlsProtocol(config, protocol);
	}

	@Contract("_ -> this")
	public ConnectionTester setCipherSuite(CipherSuite cipherSuite) {

		logger.info("Set cipher suite " + cipherSuite.getValue());
		CiphersuiteDelegate ciphersuiteDelegate = new CiphersuiteDelegate();
		ciphersuiteDelegate.setCipherSuites(cipherSuite);
		ciphersuiteDelegate.applyDelegate(config);
		return this;
	}

	public ConnectionTester execute(ModifiableByteArray id) {
		logger.debug("Executing...");
		State state = new State(config);
		((ClientHelloMessage) (
				(SendAction)
						state.getWorkflowTrace().getTlsActions().get(0))
				.getSendMessages().get(0))
				.setSessionId(id);

		DefaultWorkflowExecutor executor = new DefaultWorkflowExecutor(state);
		executor.executeWorkflow();
		logger.debug("Complete");

		trace = state.getWorkflowTrace();
		return this;
	}

	@Contract("-> this")
	public ConnectionTester execute() {
		logger.debug("Executing...");
		State state = new State(config);
		DefaultWorkflowExecutor executor = new DefaultWorkflowExecutor(state);
		executor.executeWorkflow();
		logger.debug("Complete");

		trace = state.getWorkflowTrace();
		return this;
	}

	public boolean isServerHelloReceived() {
		return WorkflowTraceUtil
				.didReceiveMessage(HandshakeMessageType.SERVER_HELLO, trace);
	}

	public List<ProtocolMessage> getHandShakeMessages() {
		return WorkflowTraceUtil.getAllReceivedMessages(trace, ProtocolMessageType.HANDSHAKE);
	}
}
