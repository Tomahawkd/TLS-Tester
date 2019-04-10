package io.tomahawkd.tlsattacker;

import de.rub.nds.modifiablevariable.bytearray.ModifiableByteArray;
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
import org.jetbrains.annotations.Contract;

import java.util.List;

public class KeyExchangeTester {

	private Config config;
	private WorkflowTrace trace;

	private static final String DEFAULT_PORT = "443";

	public KeyExchangeTester(String host) {

		if (host.split(":").length == 1) host = host + ":" + DEFAULT_PORT;

		config = Config.createConfig();
		ClientDelegate delegate = new ClientDelegate();
		delegate.setHost(host);
		delegate.applyDelegate(config);
		trace = new WorkflowTrace();
	}

	@Contract("_ -> this")
	public KeyExchangeTester setCipherSuite(CipherSuite cipherSuite) {
		CiphersuiteDelegate ciphersuiteDelegate = new CiphersuiteDelegate();
		ciphersuiteDelegate.setCipherSuites(cipherSuite);
		ciphersuiteDelegate.applyDelegate(config);
		return this;
	}

	public List<MessageAction> execute() {
		State state = new State(config, trace);
		DefaultWorkflowExecutor executor = new DefaultWorkflowExecutor(state);
		executor.executeWorkflow();
		return trace.getMessageActions();
	}

	@Contract("_ -> this")
	public KeyExchangeTester initRSA(ModifiableByteArray session) {
		trace.reset();

		ClientHelloMessage clientHelloMessage = new ClientHelloMessage(config);
		if (session != null) clientHelloMessage.setSessionId(session);

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

	@Contract("_ -> this")
	public KeyExchangeTester initECDHE(ModifiableByteArray session) {
		trace.reset();

		ClientHelloMessage clientHelloMessage = new ClientHelloMessage(config);
		if (session != null) clientHelloMessage.setSessionId(session);

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
