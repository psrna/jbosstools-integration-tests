/******************************************************************************* 
 * Copyright (c) 2016 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.central.reddeer.preferences;

import org.jboss.reddeer.jface.preference.PreferencePage;
import org.jboss.reddeer.swt.impl.group.DefaultGroup;
import org.jboss.reddeer.swt.impl.styledtext.DefaultStyledText;

public class OfflineSupportPreferencePage extends PreferencePage {

	public OfflineSupportPreferencePage() {
		super(new String[] {"JBoss Tools", "Project Examples", "Offline Support"});
	}
	
	public String getCommand(){
		DefaultGroup defaultGroup = new DefaultGroup("Prepare offline data");
		DefaultStyledText defaultStyledText = new DefaultStyledText(defaultGroup);
		return defaultStyledText.getText();
	}
	
}
