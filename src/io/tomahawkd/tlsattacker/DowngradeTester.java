package io.tomahawkd.tlsattacker;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.config.delegate.CiphersuiteDelegate;
import de.rub.nds.tlsattacker.core.config.delegate.ClientDelegate;
import de.rub.nds.tlsattacker.core.constants.CipherSuite;
import de.rub.nds.tlsattacker.core.protocol.message.*;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.DefaultWorkflowExecutor;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTraceUtil;
import de.rub.nds.tlsattacker.core.workflow.action.ReceiveAction;
import de.rub.nds.tlsattacker.core.workflow.action.SendAction;
import io.tomahawkd.common.log.Logger;
import org.jetbrains.annotations.Contract;

import java.util.List;

public class DowngradeTester {

	private Config config;
	private WorkflowTrace trace;

	private static final String DEFAULT_PORT = "443";

	private static final Logger logger = Logger.getLogger(DowngradeTester.class);

	public DowngradeTester(String host) {

		if (host.split(":").length == 1) host = host + ":" + DEFAULT_PORT;

		logger.info("Starting test key exchange on " + host);
		config = Config.createConfig();
		ClientDelegate delegate = new ClientDelegate();
		delegate.setHost(host);
		delegate.applyDelegate(config);
		trace = new WorkflowTrace();
		trace.addTlsAction(new SendAction(new ClientHelloMessage(config)));
		trace.addTlsAction(new ReceiveAction(
				new ServerHelloMessage(),
				new CertificateMessage(),
				new ServerHelloDoneMessage()));
	}

	@Contract("_ -> this")
	public DowngradeTester setCipherSuite(CipherSuite cipherSuite) {

		logger.info("Set cipher suite " + cipherSuite.getValue());
		CiphersuiteDelegate ciphersuiteDelegate = new CiphersuiteDelegate();
		ciphersuiteDelegate.setCipherSuites(cipherSuite);
		ciphersuiteDelegate.applyDelegate(config);
		return this;
	}

	public List<HandshakeMessage> execute() {

		logger.debug("Executing...");
		State state = new State(config, trace);
		DefaultWorkflowExecutor executor = new DefaultWorkflowExecutor(state);
		executor.executeWorkflow();

		logger.debug("Complete");
		return WorkflowTraceUtil.getAllSendHandshakeMessages(trace);
	}
}
