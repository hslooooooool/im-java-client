
package com.farsunset.cim.sdk.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.farsunset.cim.sdk.client.model.Message;
import com.farsunset.cim.sdk.client.model.ReplyBody;

/**
 * CIM 消息监听器管理
 */
public class CIMListenerManager {

	private static ArrayList<CIMEventListener> cimListeners = new ArrayList<CIMEventListener>();
	private static CIMMessageReceiveComparator comparator = new CIMMessageReceiveComparator();
    private static final Logger LOGGER = LoggerFactory.getLogger(CIMListenerManager.class);

	public static void registerMessageListener(CIMEventListener listener) {

		if (!cimListeners.contains(listener)) {
			cimListeners.add(listener);
			Collections.sort(cimListeners, comparator);
		}
	}

	public static void removeMessageListener(CIMEventListener listener) {
		for (int i = 0; i < cimListeners.size(); i++) {
			if (listener.getClass() == cimListeners.get(i).getClass()) {
				cimListeners.remove(i);
			}
		}
	}

	public static void notifyOnConnectionSuccessed(boolean antoBind) {
		for (CIMEventListener listener : cimListeners) {
			listener.onConnectionSuccessed(antoBind);
		}
	}

	public static void notifyOnMessageReceived(Message message) {
		for (CIMEventListener listener : cimListeners) {
			listener.onMessageReceived(message);
		}
	}

	public static void notifyOnConnectionClosed() {
		for (CIMEventListener listener : cimListeners) {
			listener.onConnectionClosed();
		}
	}

	public static void notifyOnReplyReceived(ReplyBody body) {
		for (CIMEventListener listener : cimListeners) {
			listener.onReplyReceived(body);
		}
	}

	public static void notifyOnConnectionFailed() {
		for (CIMEventListener listener : cimListeners) {
			listener.onConnectionFailed();
		}
	}

	public static void destory() {
		cimListeners.clear();
	}

	public static void logListenersName() {
		for (CIMEventListener listener : cimListeners) {
			LOGGER.debug("#######" + listener.getClass().getName() + "#######");
		}
	}

	/**
	 * 消息接收activity的接收顺序排序，CIM_RECEIVE_ORDER倒序
	 */
	private static class CIMMessageReceiveComparator implements Comparator<CIMEventListener> {

		@Override
		public int compare(CIMEventListener arg1, CIMEventListener arg2) {

			int order1 = arg1.getEventDispatchOrder();
			int order2 = arg2.getEventDispatchOrder();
			return order2 - order1;
		}

	}

}
