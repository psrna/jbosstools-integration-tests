/*******************************************************************************
 * Copyright (c) 2007-2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v 1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.openshift.ui.bot.test.application.v3.adapter;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.function.Predicate;

import org.jboss.reddeer.common.condition.AbstractWaitCondition;
import org.jboss.reddeer.common.exception.WaitTimeoutExpiredException;
import org.jboss.reddeer.common.logging.Logger;
import org.jboss.reddeer.common.wait.TimePeriod;
import org.jboss.reddeer.common.wait.WaitUntil;
import org.jboss.reddeer.common.wait.WaitWhile;
import org.jboss.reddeer.core.condition.JobIsRunning;
import org.jboss.reddeer.core.exception.CoreLayerException;
import org.jboss.reddeer.eclipse.condition.ServerHasPublishState;
import org.jboss.reddeer.eclipse.condition.ServerHasState;
import org.jboss.reddeer.eclipse.core.resources.Project;
import org.jboss.reddeer.eclipse.core.resources.ProjectItem;
import org.jboss.reddeer.eclipse.debug.core.BreakpointsView;
import org.jboss.reddeer.eclipse.debug.core.DebugView;
import org.jboss.reddeer.eclipse.debug.core.VariablesView;
import org.jboss.reddeer.eclipse.jdt.ui.ProjectExplorer;
import org.jboss.reddeer.eclipse.ui.browser.BrowserEditor;
import org.jboss.reddeer.eclipse.ui.console.ConsoleView;
import org.jboss.reddeer.eclipse.ui.perspectives.DebugPerspective;
import org.jboss.reddeer.eclipse.wst.server.ui.view.ServersView;
import org.jboss.reddeer.eclipse.wst.server.ui.view.ServersViewEnums.ServerPublishState;
import org.jboss.reddeer.eclipse.wst.server.ui.view.ServersViewEnums.ServerState;
import org.jboss.reddeer.junit.requirement.inject.InjectRequirement;
import org.jboss.reddeer.junit.runner.RedDeerSuite;
import org.jboss.reddeer.junit.screenshot.CaptureScreenshotException;
import org.jboss.reddeer.requirements.openperspective.OpenPerspectiveRequirement.OpenPerspective;
import org.jboss.reddeer.swt.api.TreeItem;
import org.jboss.reddeer.swt.impl.button.OkButton;
import org.jboss.reddeer.swt.impl.menu.ContextMenu;
import org.jboss.reddeer.swt.impl.menu.ShellMenu;
import org.jboss.reddeer.swt.impl.shell.DefaultShell;
import org.jboss.reddeer.swt.impl.styledtext.DefaultStyledText;
import org.jboss.reddeer.swt.impl.toolbar.DefaultToolItem;
import org.jboss.reddeer.swt.impl.tree.DefaultTree;
import org.jboss.reddeer.swt.impl.tree.DefaultTreeItem;
import org.jboss.reddeer.workbench.exception.WorkbenchLayerException;
import org.jboss.reddeer.workbench.impl.editor.TextEditor;
import org.jboss.reddeer.workbench.impl.shell.WorkbenchShell;
import org.jboss.reddeer.workbench.ui.dialogs.WorkbenchPreferenceDialog;
import org.jboss.tools.openshift.reddeer.preference.page.JavaDebugPreferencePage;
import org.jboss.tools.openshift.reddeer.requirement.OpenShiftCommandLineToolsRequirement.OCBinary;
import org.jboss.tools.openshift.reddeer.requirement.OpenShiftConnectionRequirement;
import org.jboss.tools.openshift.reddeer.requirement.OpenShiftConnectionRequirement.RequiredBasicConnection;
import org.jboss.tools.openshift.reddeer.view.OpenShiftExplorerView;
import org.jboss.tools.openshift.reddeer.view.resources.ServerAdapter;
import org.jboss.tools.openshift.reddeer.view.resources.ServerAdapter.Version;
import org.jboss.tools.openshift.ui.bot.test.application.v3.adapter.condition.BrowserIsReadyElseReloadCondition;
import org.jboss.tools.openshift.ui.bot.test.application.v3.adapter.condition.SuspendedTreeItemIsReady;
import org.jboss.tools.openshift.ui.bot.test.application.v3.create.AbstractCreateApplicationTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@OpenPerspective(DebugPerspective.class)
@RunWith(RedDeerSuite.class)
@OCBinary
@RequiredBasicConnection
public class DebuggingEAPAppTest extends AbstractCreateApplicationTest {

	private static Logger LOGGER = new Logger(DebuggingEAPAppTest.class);

	@InjectRequirement
	private OpenShiftConnectionRequirement requiredConnection;
	private static ServerAdapter serverAdapter;

	@BeforeClass
	public static void setupClass() {
		doNotSuspendOnUncaughtExceptions();

		toggleAutoBuild(false);

		createServerAdapter();

		disableShowConsoleWhenOutputChanges();

		serverAdapter = new ServerAdapter(Version.OPENSHIFT3, "eap-app", "Service");
		serverAdapter.select();
		new ContextMenu("Restart in Debug").select();
		new WaitWhile(new JobIsRunning(), TimePeriod.VERY_LONG);

		waitForserverAdapterToBeInRightState();

		cleanAndBuildWorkspace();
	}

	@AfterClass
	public static void tearDownClass() {
		toggleAutoBuild(true);
	}

	@Before
	public void setup() {
		setupBreakpoint();

		cleanAndBuildWorkspace();

		triggerDebugSession();
	}

	@Test
	public void debuggerStopsAtBreakpointTest() {

		// now it shoud be stopped in debug mode.

		checkDebugView();

		checkVariablesView();
	}

	@Test
	public void changeVariableValueTest() throws CaptureScreenshotException {

		setNewVariableValue("NewWorld", "name");

		checkNewVariableValueIsPropagatedToBrowser();
	}

	@After
	public void teardown() {
		try {
			new ShellMenu("Run", "Terminate").select();
		} catch (CoreLayerException ex) {
			if (ex.getMessage().contains("Menu item is not enabled")) {
				// no big deal, there is no execution running
			} else {
				throw ex;
			}
		}

		// remove all breakpoints
		BreakpointsView breakpointsView = new BreakpointsView();
		breakpointsView.open();
		breakpointsView.removeAllBreakpoints();

	}

	private static void doNotSuspendOnUncaughtExceptions() {
		WorkbenchPreferenceDialog workbenchPreferenceDialog = new WorkbenchPreferenceDialog();
		workbenchPreferenceDialog.open();
		JavaDebugPreferencePage javaDebugPreferencePage = new JavaDebugPreferencePage();
		workbenchPreferenceDialog.select(javaDebugPreferencePage);

		javaDebugPreferencePage.setSuspendOnUncaughtExceptions(false);

		workbenchPreferenceDialog.ok();
	}

	private static void toggleAutoBuild(boolean autoBuild) {
		ShellMenu autoBuildMenuItem = new ShellMenu("Project", "Build Automatically");
		boolean isSelected = autoBuildMenuItem.isSelected();
		if (autoBuild && !isSelected) {
			autoBuildMenuItem.select();
		}
		if (!autoBuild && isSelected) {
			autoBuildMenuItem.select();
		}
	}

	private static void cleanAndBuildWorkspace() {
		new ShellMenu("Project", "Clean...").select();
		new DefaultShell("Clean");
		new OkButton().click();
		new WaitWhile(new JobIsRunning());
	}

	private static void waitForserverAdapterToBeInRightState() {
		new WaitUntil(new ServerHasState(new ServersView().getServer(serverAdapter.getLabel()), ServerState.DEBUGGING));
		new WaitUntil(new ServerHasPublishState(new ServersView().getServer(serverAdapter.getLabel()),
				ServerPublishState.SYNCHRONIZED));
	}

	private void setupBreakpoint() {
		// set breakpoint where we need it.
		ProjectItem helloServiceFile = getHelloServiceFile();
		setBreakpointToLineWithText(helloServiceFile, "return \"Hello");
	}

	private void checkNewVariableValueIsPropagatedToBrowser() {

		clickResume();

		BrowserEditor browserEditor = new BrowserEditor("helloworld");
		browserEditor.activate();
		String text = browserEditor.getText();
		assertTrue(text.contains("NewWorld"));

	}

	private void clickResume() {
		new WaitWhile(new AbstractWaitCondition() {

			@Override
			public boolean test() {
				ShellMenu resumeMenu = new ShellMenu("Run", "Resume");
				if (resumeMenu.isEnabled()) {
					resumeMenu.select();
					return true;
				} else {
					return false;
				}
			}
		});
	}

	private void checkDebugView() {
		DebugView debugView = new DebugView();
		debugView.open();

		TreeItem createHelloMessageDebugItem = ensureCorrectFrameIsSelected(debugView);

		assertTrue(createHelloMessageDebugItem.getText().contains("createHelloMessage"));
	}

	private TreeItem ensureCorrectFrameIsSelected(DebugView debugView) {
		List<TreeItem> items;
		TreeItem createHelloMessageDebugItem;

		// get frames of suspended thread. If the desired frame is not present,
		// try reopening Debug view
		items = getSuspendedThreadTreeItem(debugView).getItems();
		if (items.size() < 2) {
			// no stack trace available. Try to close&reopen Debug view (dirty
			// hack)
			debugView.close();
			debugView = new DebugView();
			debugView.open();
			items = getSuspendedThreadTreeItem(debugView).getItems();
		}

		final List<TreeItem> tIList = items;

		// wait for frame texts to populate.
		new WaitUntil(new AbstractWaitCondition() {

			@Override
			public boolean test() {
				return tIList.stream()
						.peek(ti -> LOGGER.debug(ti.getText()))
						.filter(ti -> ti.getText().contains("createHelloMessage"))
						.findFirst()
						.isPresent();
			}
		});

		createHelloMessageDebugItem = tIList.stream()
				.peek(ti -> LOGGER.debug(ti.getText()))
				.filter(ti -> ti.getText().contains("createHelloMessage"))
				.findFirst()
				.get();
		// select the item and return it
		createHelloMessageDebugItem.select();
		return createHelloMessageDebugItem;

	}

	private TreeItem getSuspendedThreadTreeItem(DebugView debugView) {
		// get top item
		debugView.activate();
		DefaultTree parent = new DefaultTree();
		TreeItem remoteDebuggerTreeItem = parent.getItems().stream().filter(containsStringPredicate("Remote debugger"))
				.findFirst().get();

		List<TreeItem> items = remoteDebuggerTreeItem.getItems();
		TreeItem openJDKTreeItem = items.get(0);

		// this could (and will) change when run with another JDK - need
		// investigation
		assertTrue(openJDKTreeItem.getText().contains("OpenJDK"));

		// wait until we can see the suspended thread
		SuspendedTreeItemIsReady suspendedTreeItemIsReady = new SuspendedTreeItemIsReady(openJDKTreeItem);
		new WaitUntil(suspendedTreeItemIsReady);
		return suspendedTreeItemIsReady.getSuspendedTreeItem();
	}

	private Predicate<TreeItem> containsStringPredicate(String string) {
		return treeItem -> treeItem.getText().contains(string);
	}

	private void checkVariablesView() {
		VariablesView variablesView = new VariablesView();
		variablesView.open();
		// wait for variables to have correct value
		new WaitUntil(new AbstractWaitCondition() {

			@Override
			public boolean test() {
				return variablesView.getValue("name").equals("World");
			}
		});
		new WaitUntil(new AbstractWaitCondition() {

			@Override
			public boolean test() {
				return variablesView.getValue("this").contains("HelloService");
			}
		});
	}

	private void triggerDebugSession() {
		serverAdapter.select();
		new ContextMenu("Show In", "Web Browser").select();
		try {
			new WaitUntil(new BrowserIsReadyElseReloadCondition(serverAdapter));
		} catch (WaitTimeoutExpiredException e) {
			throw e;
		}
		new WaitUntil(new AbstractWaitCondition() {

			@Override
			public boolean test() {
				TextEditor currentEditor = null;
				try {
					currentEditor = new TextEditor();
				} catch (WorkbenchLayerException ex) {
					// current editor is not TextEditor
					return false;
				}
				if (currentEditor.getTitle().contains("HelloService.java")) {
					// textEditor is active.
					return true;
				}
				// try to reload again
				serverAdapter.select();
				new ContextMenu("Show In", "Web Browser").select();
				return false;
			}
		}, TimePeriod.NORMAL, false);
	}

	private ProjectItem getHelloServiceFile() {
		ProjectExplorer projectExplorer = new ProjectExplorer();
		projectExplorer.open();
		Project project = projectExplorer.getProject(PROJECT_NAME);
		ProjectItem helloServiceFile = project.getProjectItem("Java Resources", "src/main/java",
				"org.jboss.as.quickstarts.helloworld", "HelloService.java");
		return helloServiceFile;
	}

	// Sets breakpoint to first appearance of given text.
	private void setBreakpointToLineWithText(ProjectItem file, String text) {
		file.open();
		TextEditor textEditor = new TextEditor("HelloService.java");
		textEditor.setCursorPosition(textEditor.getPositionOfText(text));
		new ShellMenu("Run", "Toggle Breakpoint").select();
	}

	private static void createServerAdapter() {
		OpenShiftExplorerView explorer = new OpenShiftExplorerView();
		explorer.getOpenShift3Connection().getProject().getService("eap-app").createServerAdapter();
	}

	private static void disableShowConsoleWhenOutputChanges() {
		ConsoleView consoleView = new ConsoleView();
		consoleView.open();

		new WaitUntil(new ShowConsoleOutputToolItemIsAvailable());

		DefaultToolItem showConsoleOnChange = new DefaultToolItem(new WorkbenchShell(),
				"Show Console Output When Standard Out Changes");
		showConsoleOnChange.click();
	}

	// TODO this should be replaced once
	// https://github.com/jboss-reddeer/reddeer/issues/1668 is fixed.
	private void setNewVariableValue(String newValue, final String... variablePath) {
		new WaitWhile(new JobIsRunning());
		DebugView debugView = new DebugView();
		debugView.open();

		ensureCorrectFrameIsSelected(debugView);

		VariablesView variablesView = new VariablesView();
		variablesView.open();

		new WaitUntil(new AbstractWaitCondition() {

			@Override
			public boolean test() {
				try {
					TreeItem variable = new DefaultTreeItem(variablePath);
					variable.select();
					return variable.isSelected();
				} catch (Exception e) {
					return false;
				}
			}

			@Override
			public String description() {
				return "Variable is not selected";
			}
		});

		try {
			new ContextMenu("Change Value...").select();
		} catch (CoreLayerException e) {
			throw e;
		}
		new DefaultShell("Change Object Value");
		new DefaultStyledText().setText(newValue);
		new OkButton().click();

		new WaitWhile(new JobIsRunning());
	}

	private static class ShowConsoleOutputToolItemIsAvailable extends AbstractWaitCondition {
		@Override
		public boolean test() {
			try {
				new DefaultToolItem(new WorkbenchShell(), "Show Console Output When Standard Out Changes");
				return true;
			} catch (CoreLayerException ex) {
				return false;
			}
		}
	}
}