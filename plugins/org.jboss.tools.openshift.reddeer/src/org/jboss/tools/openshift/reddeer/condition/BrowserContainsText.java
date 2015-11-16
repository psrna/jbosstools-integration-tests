package org.jboss.tools.openshift.reddeer.condition;

import org.jboss.reddeer.common.condition.AbstractWaitCondition;
import org.jboss.reddeer.swt.impl.browser.InternalBrowser;

public class BrowserContainsText extends AbstractWaitCondition {

	private InternalBrowser browser;
	private String text;
	
	public BrowserContainsText(String text) {
		browser = new InternalBrowser();
		this.text = text;
	}
	
	@Override
	public boolean test() {
		browser.refresh();
		return browser.getText().contains(text);
	}

	@Override
	public String description() {
		return "browser contains text";
	}

}
