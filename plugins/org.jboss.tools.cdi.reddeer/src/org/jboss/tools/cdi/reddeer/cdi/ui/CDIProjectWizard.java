/*******************************************************************************
 * Copyright (c) 2010-2018 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.cdi.reddeer.cdi.ui;

import org.eclipse.reddeer.eclipse.selectionwizard.NewMenuWizard;
import org.jboss.tools.cdi.reddeer.CDIConstants;

public class CDIProjectWizard extends NewMenuWizard {

	public static final String NAME = "CDI Web Project";
	public static final String SHELL_TEXT = "New CDI Web Project";

	public CDIProjectWizard() {
		super(SHELL_TEXT, CDIConstants.CDI_GROUP, NAME);
	}

}
