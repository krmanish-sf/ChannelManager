package salesmachine.oim.stores.impl;

import org.hibernate.Session;

import salesmachine.hibernatedb.OimOrderBatches;
import salesmachine.hibernatedb.OimOrderBatchesTypes;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.oim.stores.api.ChannelBase;
import salesmachine.oim.stores.api.IOrderImport;
import salesmachine.oim.stores.exception.ChannelCommunicationException;
import salesmachine.oim.stores.exception.ChannelConfigurationException;
import salesmachine.oim.stores.exception.ChannelOrderFormatException;
import salesmachine.oim.suppliers.modal.OrderStatus;

/***
 * Psudo code to explain Channel Integration.
 * 
 * @author amit-yadav
 *
 */

public class EbayOrderImport extends ChannelBase implements IOrderImport {

	@Override
	public boolean init(int channelID, Session dbSession)
			throws ChannelConfigurationException {
		super.init(channelID, dbSession);
		// Ebay Specific parameters init.
		return false;
	}

	@Override
	public void getVendorOrders(OimOrderBatchesTypes batchesTypes,
			OimOrderBatches batch) throws ChannelCommunicationException,
			ChannelOrderFormatException, ChannelConfigurationException {
		// Provide the Order pulling logic form Ebay API.
	}

	@Override
	public boolean updateStoreOrder(OimOrderDetails oimOrderDetails,
			OrderStatus orderStatus) throws ChannelCommunicationException,
			ChannelOrderFormatException {
		// Provide logic to update the Ebay store with Order status.
		return false;
	}

}
