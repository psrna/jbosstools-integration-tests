package org.jboss.ide.eclipse.as.ui.bot.test.eap7;

import org.jboss.ide.eclipse.as.reddeer.server.requirement.ServerReqVersion;
import org.jboss.ide.eclipse.as.reddeer.server.requirement.ServerReqType;
import org.jboss.ide.eclipse.as.reddeer.server.requirement.ServerRequirement.JBossServer;
import org.jboss.ide.eclipse.as.ui.bot.test.template.DeployJSPProjectTemplate;
import org.jboss.ide.eclipse.as.ui.bot.test.template.UndeployJSPProjectTemplate;
import org.jboss.reddeer.requirements.server.ServerReqState;

@JBossServer(state=ServerReqState.RUNNING, type=ServerReqType.EAP7x, version=ServerReqVersion.EQUAL)
public class UndeployJSPProjectEAP7Server extends UndeployJSPProjectTemplate {

	@Override
	protected String getConsoleMessage() {
		return "Undeployed \"" + DeployJSPProjectTemplate.PROJECT_NAME + ".war\"";
	}
}