package com.is.cm.core.event.orders;

import com.is.cm.core.domain.OrderDetail;
import com.is.cm.core.event.UpdateEvent;

public class UpdateOrderDetailEvent extends UpdateEvent<OrderDetail> {

	public UpdateOrderDetailEvent(int id, final OrderDetail orderDetail) {
		super(id, orderDetail);
	}
}
