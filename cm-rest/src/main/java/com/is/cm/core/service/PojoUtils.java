package com.is.cm.core.service;

import java.util.Iterator;
import java.util.Set;

import com.is.cm.core.domain.Channel;
import com.is.cm.core.domain.ChannelAccessDetail;

public class PojoUtils {
	public static String getChannelAccessDetailValue(Channel channel,
			Integer field) {
		Set<ChannelAccessDetail> allDetails = channel
				.getOimChannelAccessDetailses();
		if (allDetails == null || allDetails.size() == 0)
			return null;

		Iterator<ChannelAccessDetail> it = allDetails.iterator();
		while (it.hasNext()) {
			ChannelAccessDetail oca = it.next();
			if (oca.getDeleteTm() != null) {
				continue; // Ignoring access detail as deleted
			}
			if (oca.getOimChannelAccessFields().getFieldId().equals(field))
				return oca.getDetailFieldValue();
		}
		return null;
	}
}
