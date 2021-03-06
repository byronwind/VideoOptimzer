/*
 *  Copyright 2017 AT&T
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package com.att.aro.ui.commonui;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JFrame;
import javax.swing.SwingWorker;

import com.att.aro.core.ApplicationConfig;
import com.att.aro.core.ILogger;
import com.att.aro.core.util.CrashHandler;
import com.att.aro.ui.utils.ResourceBundleHelper;
import com.att.aro.ui.view.bestpracticestab.BestPracticesTab;

public class AROSwingWorker<T, V> extends SwingWorker<T, V> {

	private ILogger log = ContextAware.getAROConfigContext().getBean(ILogger.class);

	private JFrame parent;
	private AROProgressDialog progress;
	private String property;
	private Object oldValue, newValue;
	List<PropertyChangeListener> changeListeners = null;
	private int id;
	private String command;
	private String msg;
	List<ActionListener> actionListeners = null;

	private long startTime;

	private long endTime;

	public AROSwingWorker(JFrame frmApplicationResourceOptimizer, List<PropertyChangeListener> changeListeners, String property, Object oldValue, Object newValue, String msg) {
		Thread.setDefaultUncaughtExceptionHandler(new CrashHandler());
		this.progress = new AROProgressDialog(frmApplicationResourceOptimizer, ResourceBundleHelper.getMessageString("progress.loadingTraceResults"));
		progress.setVisible(true);
		this.parent = frmApplicationResourceOptimizer;
		this.msg = msg;
		this.changeListeners = changeListeners;
		this.property = property;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public AROSwingWorker(JFrame frmApplicationResourceOptimizer, List<ActionListener> actionListeners, int id, String command, String msg) {
		Thread.setDefaultUncaughtExceptionHandler(new CrashHandler());
		this.progress = new AROProgressDialog(frmApplicationResourceOptimizer, ResourceBundleHelper.getMessageString("progress.loadingTraceResults"));
		progress.setVisible(true);
		this.parent = frmApplicationResourceOptimizer;
		this.msg = msg;
		this.actionListeners = actionListeners;
		this.id = id;
		this.command = command;
	}

	@Override
	protected T doInBackground() throws Exception {
		startTime = System.currentTimeMillis();
		if (changeListeners != null)
			for (PropertyChangeListener name : changeListeners) {
				name.propertyChange(new PropertyChangeEvent(this, property, oldValue, newValue));
			}
		if (actionListeners != null)
			for (ActionListener name : actionListeners) {
				name.actionPerformed(new ActionEvent(this, id, command));
			}
		
		refreshLocationMap();
		
		return null;
	}

	@Override
	protected void done() {

		changeListeners = null;
		actionListeners = null;
		progress.dispose();

		if (msg != null) {
			MessageDialogFactory.showMessageDialog(parent, msg);
		}

		try {
			get();
			endTime = System.currentTimeMillis();
			log.debug("deltaTime :" + (endTime - startTime));
		} catch (InterruptedException e) {
			log.error("Interrupted error: " + e.getLocalizedMessage());
			new MessageDialogFactory().showErrorDialog(parent, e.getLocalizedMessage(), "Interrupted");
		} catch (ExecutionException e) {
			log.error("Processing error: " + e.getLocalizedMessage(), e);
			if (e.getMessage().contains("GC overhead limit exceeded")) {
				String errorMsg = MessageFormat.format(ResourceBundleHelper.getMessageString("Error.gc_overhead_limit_exceeded"), 
														ApplicationConfig.getInstance().getAppShortName());
				new MessageDialogFactory().showErrorDialog(parent, errorMsg, "Problem encountered");
			} else if (e.getMessage().contains("jpcap64.dll") || e.getMessage().contains("PCapAdapter.loopPacket")) {
				String errorMsg = MessageFormat.format(ResourceBundleHelper.getMessageString("aro.winpcap_error"), 
														ApplicationConfig.getInstance().getAppShortName());
				new MessageDialogFactory().showErrorDialog(parent, errorMsg, "Problem encountered");
			} else if (e.getMessage().contains("ffmpeg")) {
				String errorMsg = MessageFormat.format(ResourceBundleHelper.getMessageString("aro.ffmpeg_error"), 
														ApplicationConfig.getInstance().getAppShortName());
				new MessageDialogFactory().showErrorDialog(parent, errorMsg, "Problem encountered");
			} else if (e.getMessage().contains("mp4Player")) {
				new MessageDialogFactory().showErrorDialog(parent, ResourceBundleHelper.getMessageString("aro.mp4Player_error"), "Problem encountered");
			} else {
				String errorMsg = MessageFormat.format(ResourceBundleHelper.getMessageString("aro.analyse_error"), 
														ApplicationConfig.getInstance().getAppShortName());
				new MessageDialogFactory().showErrorDialog(parent, errorMsg, "Problem encountered");
			}
		}
	}
	
	/**
	 * refresh bpItem location in best practices tab after loading the trace
	 */
	private void refreshLocationMap() {
		Container container = parent.getContentPane();
		Component[] components = container.getComponents();
		
		if (components != null && components.length > 0) {
			Component bpComponent = components[0];
			
			try {
				BestPracticesTab bpTab = (BestPracticesTab) bpComponent;
				bpTab.setScrollLocationMap();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
