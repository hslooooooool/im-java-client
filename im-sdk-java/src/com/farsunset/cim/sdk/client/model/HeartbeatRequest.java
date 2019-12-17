
package com.farsunset.cim.sdk.client.model;

import java.io.Serializable;

import com.farsunset.cim.sdk.client.constant.CIMConstant;

/**
 * 服务端心跳请求
 *
 */
public class HeartbeatRequest implements Serializable, Protobufable {

	private static final long serialVersionUID = 1L;
	private static final String TAG = "SERVER_HEARTBEAT_REQUEST";
	private static final String CMD_HEARTBEAT_RESPONSE = "SR";

	private static HeartbeatRequest object = new HeartbeatRequest();

	private HeartbeatRequest() {

	}

	public static HeartbeatRequest getInstance() {
		return object;
	}

	@Override
	public byte[] getByteArray() {
		return CMD_HEARTBEAT_RESPONSE.getBytes();
	}

	public String toString() {
		return TAG;
	}

	@Override
	public byte getType() {
		return CIMConstant.ProtobufType.S_H_RQ;
	}

}
