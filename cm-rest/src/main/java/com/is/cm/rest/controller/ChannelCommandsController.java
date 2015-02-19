package com.is.cm.rest.controller;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import salesmachine.hibernatedb.OimChannelFiles;
import salesmachine.hibernatedb.OimChannelSupplierMap;
import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimFields;
import salesmachine.hibernatedb.OimFileFieldMap;
import salesmachine.hibernatedb.OimFileformatParams;
import salesmachine.hibernatedb.OimFileformats;
import salesmachine.hibernatedb.OimFiletypes;
import salesmachine.hibernatedb.OimOrderBatches;
import salesmachine.hibernatedb.OimOrderBatchesTypes;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimSuppliers;
import salesmachine.hibernatedb.OimUploadedFiles;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.api.OimConstants;
import salesmachine.util.CsvHelper;
import salesmachine.util.StringHandle;

import com.is.cm.core.domain.Channel;
import com.is.cm.core.domain.Filetype;
import com.is.cm.core.domain.Order;
import com.is.cm.core.domain.OrderBatch;
import com.is.cm.core.domain.OrderDetail;
import com.is.cm.core.domain.UploadedFile;
import com.is.cm.core.event.CreateEvent;
import com.is.cm.core.event.CreatedEvent;
import com.is.cm.core.event.ReadEvent;
import com.is.cm.core.event.RequestReadEvent;
import com.is.cm.core.event.UpdateEvent;
import com.is.cm.core.event.UpdatedEvent;
import com.is.cm.core.event.channels.ChannelDeletedEvent;
import com.is.cm.core.event.channels.DeleteChannelEvent;
import com.is.cm.core.event.orders.CreateOrderEvent;
import com.is.cm.core.event.orders.OrderCreatedEvent;
import com.is.cm.core.service.ChannelService;
import com.is.cm.core.service.OrderService;

@Controller
@RequestMapping("/aggregators/channels")
public class ChannelCommandsController extends BaseController {
	private static Logger LOG = LoggerFactory
			.getLogger(ChannelCommandsController.class);

	@Autowired
	private ChannelService channelService;
	@Autowired
	OrderService orderService;

	@RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
	public ResponseEntity<Channel> deleteChannel(@PathVariable String id) {
		LOG.debug("Recieved request to delete Channel:{}", id);
		ChannelDeletedEvent deletedEvent = channelService
				.deleteChannel(new DeleteChannelEvent(Integer.parseInt(id)));
		return createResponseBody(deletedEvent);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/{id}")
	public ResponseEntity<Channel> updateChannel(
			@RequestBody Map<String, String> channel, @PathVariable int id) {
		LOG.debug("Recieved request to update Channel:{}", id);
		UpdatedEvent<Channel> updatedEvent = channelService
				.update(new UpdateEvent<Map<String, String>>(id, channel));
		return createResponseBody(updatedEvent);
	}

	@RequestMapping(method = RequestMethod.PUT)
	public ResponseEntity<Channel> createChannel(
			@RequestBody Map<String, String> channel) {
		LOG.debug("Recieved request to create Channel");
		CreatedEvent<Channel> createdEvent = channelService
				.create(new CreateEvent<Map<String, String>>(channel));
		if (createdEvent.isAlradyExists()) {
			return new ResponseEntity<Channel>(createdEvent.getEntity(),
					HttpStatus.CONFLICT);
		}
		return new ResponseEntity<Channel>(createdEvent.getEntity(),
				HttpStatus.CREATED);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/{channelId}/orders")
	public ResponseEntity<Order> createSingleOrder(
			@RequestBody Map<String, String> orderData,
			@PathVariable int channelId, UriComponentsBuilder builder) {
		orderData.put("channelId", String.valueOf(channelId));
		OrderCreatedEvent orderCreated = orderService
				.createOrder(new CreateOrderEvent(orderData, null));
		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(builder.path("/aggregators/orders/{id}")
				.buildAndExpand(orderCreated.getNewId()).toUri());
		return new ResponseEntity<Order>(orderCreated.getEntity(), headers,
				HttpStatus.CREATED);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{channelId}/pull")
	public ResponseEntity<String> pullOrders(@PathVariable int channelId) {
		try {
			ReadEvent<String> readEvent = channelService
					.pullOrders(new ReadEvent<Channel>(channelId));
			return new ResponseEntity<String>(readEvent.getEntity(),
					HttpStatus.OK);
		} catch (RuntimeException e) {
			return new ResponseEntity<String>(
					"Channel not configured properly", HttpStatus.OK);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{channelId}/filetypes")
	public ResponseEntity<List<Filetype>> getFileTypes(
			@PathVariable int channelId) {
		ReadEvent<List<Filetype>> readEvent = channelService
				.getFileTypes(new ReadEvent<Integer>(channelId));
		return new ResponseEntity<List<Filetype>>(readEvent.getEntity(),
				HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{channelId}/uploadedfiles")
	public ResponseEntity<List<UploadedFile>> getUploadedFiles(
			@PathVariable int channelId) {
		ReadEvent<List<UploadedFile>> readEvent = channelService
				.getUploadedFiles(new ReadEvent<Integer>(channelId));
		return new ResponseEntity<List<UploadedFile>>(readEvent.getEntity(),
				HttpStatus.OK);
	}

	int m_channelId;

	public class UploadResponse {
		private final String status;
		private final List<String> header;
		private final int channelId;
		private final String uploadedfilename;
		private final int filetypeId;

		public UploadResponse(final String status, final List<String> header,
				final int channelId, final String filename, final int filetypeId) {
			this.status = status;
			this.header = header;
			this.channelId = channelId;
			this.uploadedfilename = filename;
			this.filetypeId = filetypeId;
		}

		public String getStatus() {
			return status;
		}

		public List<String> getHeader() {
			return header;
		}

		public int getChannelId() {
			return channelId;
		}

		public String getUploadedfilename() {
			return uploadedfilename;
		}

		public int getFiletypeId() {
			return filetypeId;
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/{channelId}/uploadfile")
	public ResponseEntity<UploadResponse> UploadFile(
			@PathVariable int channelId, @RequestParam MultipartFile upload,
			@RequestParam int filetype, @RequestParam String filename,
			@RequestParam String hasheader,
			@RequestParam String fieldDelimiter,
			@RequestParam String textDelimiter, @RequestHeader int vid) {

		LOG.info("Going to process uploaded file");
		String fullFileName = vid + upload.getName() + ".txt";
		String report = "";
		File theFile = new File(fullFileName);
		try {
			upload.transferTo(theFile);
		} catch (IllegalStateException e1) {
			LOG.error(e1.getMessage(), e1);
		} catch (IOException e1) {
			LOG.error(e1.getMessage(), e1);
		}// FileUtils.copyFile(m_upload, theFile);
		LOG.info("Copied the file to local-zone");
		if (filetype == -1) {
			// Show the column mapping interface

			if ("tab".equalsIgnoreCase(fieldDelimiter))
				fieldDelimiter = "\t";
			else if ("comma".equalsIgnoreCase(fieldDelimiter))
				fieldDelimiter = ",";

			if ("quotes".equalsIgnoreCase(textDelimiter))
				textDelimiter = "\"";
			LOG.info("Computing headers");
			List<String> header = CsvHelper.getColumnHeaders(theFile,
					fieldDelimiter, textDelimiter,
					hasheader.equalsIgnoreCase("yes"));

			// Check if the headers of the file are readable or not
			Iterator iter = header.iterator();
			while (iter.hasNext()) {
				String headerCol = (String) iter.next();
				if (!(isPureAscii(headerCol))) {
					report = "The header contains some unwanted characters. Please check the uploaded file";
				}
			}

			// Minimum 6 headers are required for successful order upload
			if (header.size() < 6) {
				report = "The file doesnt contain the minimum required headers. Please check the uploaded file";
			}
			if ("".equals(report)) {
				report = "Orders file uploaded and parsed successfully";
			}
			LOG.info("Determined headers");
			return new ResponseEntity<UploadResponse>(new UploadResponse(
					report, header, channelId, fullFileName, filetype),
					HttpStatus.OK);
		} else {
			// Simply import the orders from the file
			Session dbSession = SessionManager.currentSession();
			try {
				// processImportOrders(dbSession, theFile, filetype, filename);
				return new ResponseEntity<UploadResponse>(new UploadResponse(
						"Orders file uploaded successfully.", null, channelId,
						fullFileName, filetype), HttpStatus.OK);
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
			// SessionManager.closeSession();
		}
		return null;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{channelId}/newfile")
	public ResponseEntity<OrderBatch> saveNewFile(@PathVariable int channelId,
			@RequestHeader int vid, @RequestBody Map<String, String> fieldMap) {
		this.m_channelId = channelId;
		String fieldDelimiter = fieldMap.get("fieldDelimiter");
		String textDelimiter = fieldMap.get("textDelimiter");
		String hasheader = fieldMap.get("hasheader");
		String uploadedfilename = fieldMap.get("uploadedfilename");
		int filetype = Integer.parseInt(fieldMap.get("filetype"));
		Session dbSession = SessionManager.currentSession();
		Transaction tx = null;
		try {
			if (filetype == -1) {
				tx = dbSession.beginTransaction();

				// Here is your db code
				LOG.info("Creating File Type Instance");
				OimFileformats fmt = new OimFileformats();
				fmt.setFileformatId(new Integer(1));
				OimFiletypes oft = new OimFiletypes();
				oft.setOimFileformats(fmt);
				oft.setInsertionTm(new Date());
				oft.setFileTypeName(fieldMap.get("filename"));
				dbSession.save(oft);

				LOG.info("Associating file with channel " + channelId);
				OimChannels c = new OimChannels();
				c.setChannelId(channelId);
				OimChannelFiles ocf = new OimChannelFiles();
				ocf.setOimFiletypes(oft);
				ocf.setOimChannels(c);
				ocf.setInsertionTm(new Date());
				dbSession.save(ocf);

				LOG.info("Saving file format parameters");
				saveFileFormatParam(dbSession, oft, "USE_HEADER",
						hasheader.equalsIgnoreCase("yes") ? "1" : "0");
				String delim = fieldDelimiter;
				if ("COMMA".equalsIgnoreCase(fieldDelimiter))
					delim = ",";
				saveFileFormatParam(dbSession, oft, "FIELD_DELIMITER", delim);
				delim = textDelimiter;
				if ("COMMA".equalsIgnoreCase(textDelimiter))
					delim = ",";
				else if ("QUOTES".equalsIgnoreCase(textDelimiter))
					delim = "\"";
				saveFileFormatParam(dbSession, oft, "TEXT_DELIMITER", delim);

				int columnCount = Integer.parseInt(fieldMap.get("colcount"));
				LOG.info("Saving file field map");
				for (int i = 0; i < columnCount; i++) {
					OimFileFieldMap ffm = new OimFileFieldMap();

					OimFields ff = new OimFields();
					String fieldName = "colindex_fieldid_" + i;
					Integer fieldId = Integer.valueOf(fieldMap.get(fieldName));
					ff.setFieldId(fieldId);
					ffm.setOimFields(ff);
					LOG.info("Mapping field: " + fieldId);

					if (hasheader.equalsIgnoreCase("yes")) {
						fieldName = "colindex_mapped_header_" + i;
						String mappedHeaderName = fieldMap.get(fieldName);
						ffm.setMappedFieldName(mappedHeaderName);
						LOG.info(" with " + mappedHeaderName);
					} else {
						LOG.info(" at index " + i);
					}
					ffm.setInsertionTm(new Date());
					ffm.setOimFiletypes(oft);
					dbSession.save(ffm);
				}
				tx.commit();
				filetype = oft.getFileTypeId();
				dbSession.evict(oft);
				dbSession.evict(ocf);
			}

			LOG.info("Importing orders from data file now");
			OrderBatch orderBatch = processImportOrders(dbSession, new File(
					uploadedfilename), filetype, uploadedfilename);
			return new ResponseEntity<OrderBatch>(orderBatch, HttpStatus.OK);
		} catch (RuntimeException e) {
			if (tx != null && tx.isActive())
				tx.rollback();
			e.printStackTrace();
		} catch (Exception e) {
			if (tx != null && tx.isActive())
				tx.rollback();
			e.printStackTrace();
		}
		return new ResponseEntity<OrderBatch>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{channelId}/confirmupload")
	public ResponseEntity<Integer> confirmOrder(@PathVariable int channelId,
			@RequestHeader int vid, @RequestBody OrderBatch orderBatch) {

		int batchId = orderBatch.getBatchId();
		Integer[] orderIdArr = new Integer[orderBatch.getOimOrderses().size()];
		String orderIds = "";
		String orderDetailIds = "";
		for (Iterator it = orderBatch.getOimOrderses().iterator(); it.hasNext();) {
			Order o = (Order) it.next();
			if (orderIds.length() > 0)
				orderIds += ",";
			orderIds += o.getOrderId();

			for (Iterator dit = o.getOimOrderDetailses().iterator(); dit
					.hasNext();) {
				OrderDetail d = (OrderDetail) dit.next();
				if (orderDetailIds.length() > 0)
					orderDetailIds += ",";
				orderDetailIds += d.getDetailId();
			}
		}
		String uploadedfileid = "";
		if (orderBatch.getOimUploadedFileses() != null) {
			for (Iterator dit = orderBatch.getOimUploadedFileses().iterator(); dit
					.hasNext();) {
				UploadedFile d = (UploadedFile) dit.next();
				if (uploadedfileid.length() > 0)
					uploadedfileid += ",";
				uploadedfileid += d.getFileId();
			}
		}

		Session dbSession = SessionManager.currentSession();
		Transaction tx = null;
		try {
			tx = dbSession.beginTransaction();

			// Here is your db code
			Query q = dbSession
					.createQuery("update salesmachine.hibernatedb.OimUploadedFiles f set f.deleteTm=null where f.fileId in ("
							+ uploadedfileid + ")");
			int rows = q.executeUpdate();
			LOG.info("Updated uploadedfile. Rows changed: " + rows);

			q = dbSession
					.createQuery("update salesmachine.hibernatedb.OimOrderBatches b set b.deleteTm=null where b.batchId="
							+ batchId);
			rows = q.executeUpdate();
			LOG.info("Updated batch. Rows changed: " + rows);

			q = dbSession
					.createQuery("update salesmachine.hibernatedb.OimOrders o set o.deleteTm=null where o.orderId in ("
							+ orderIds + ")");
			rows = q.executeUpdate();
			LOG.info("Updated orders. Rows changed: " + rows);

			q = dbSession
					.createQuery("update salesmachine.hibernatedb.OimOrderDetails o set o.deleteTm=null where o.detailId in ("
							+ orderDetailIds + ")");
			rows = q.executeUpdate();
			LOG.info("Updated order details. Rows changed: " + rows);

			tx.commit();
			return new ResponseEntity<Integer>(rows, HttpStatus.OK);
		} catch (RuntimeException e) {
			if (tx != null && tx.isActive())
				tx.rollback();
			e.printStackTrace();
		}

		return new ResponseEntity<Integer>(0, HttpStatus.OK);
	}

	private OrderBatch processImportOrders(Session dbSession, File file,
			Integer filetypeid, String uploadFileName) throws Exception {
		LOG.info("Processing orders for filetypeid: " + filetypeid);

		Transaction tx = null;
		try {
			tx = dbSession.beginTransaction();
			OimOrderBatches b = CsvHelper.processOrdersFromFile(dbSession,
					file, filetypeid);
			saveOrderBatchAsDeleted(dbSession, b);
			OimUploadedFiles ouf = saveUploadedFileAsDeleted(dbSession, b,
					filetypeid, file, uploadFileName);
			b.getOimUploadedFileses().add(ouf);
			LOG.debug("File :" + uploadFileName
					+ " added successfully with id# {}", ouf.getFileId());
			/*
			 * String orderIds = ""; String orderDetailIds = ""; for (Iterator
			 * it = b.getOimOrderses().iterator(); it.hasNext();) { OimOrders o
			 * = (OimOrders) it.next(); if (orderIds.length() > 0) orderIds +=
			 * ","; orderIds += o.getOrderId();
			 * 
			 * for (Iterator dit = o.getOimOrderDetailses().iterator(); dit
			 * .hasNext();) { OimOrderDetails d = (OimOrderDetails) dit.next();
			 * if (orderDetailIds.length() > 0) orderDetailIds += ",";
			 * orderDetailIds += d.getDetailId(); } }
			 */
			/*
			 * request.setAttribute("confirm", "1");
			 * request.setAttribute("orderbatch", b);
			 * request.setAttribute("batchid", b.getBatchId());
			 * request.setAttribute("orderids", orderIds);
			 * request.setAttribute("detailids", orderDetailIds);
			 * request.setAttribute("uploadedfileid", ouf.getFileId());
			 */

			tx.commit();
			OrderBatch orderBatch = OrderBatch.from(b);
			Set<Order> orders = new HashSet<Order>();
			for (OimOrders oimOrders : (Set<OimOrders>) b.getOimOrderses()) {
				orders.add(Order.from(oimOrders));
			}
			orderBatch.setOimOrderses(orders);
			return orderBatch;
		} catch (RuntimeException e) {
			if (tx != null && tx.isActive())
				tx.rollback();
			e.printStackTrace();
		}
		return null;
	}

	private void saveOrderBatchAsDeleted(Session dbSession, OimOrderBatches b) {
		b.setInsertionTm(new Date());
		b.setDeleteTm(new Date());
		b.setCreationTm(new Date());

		OimOrderBatchesTypes obt = new OimOrderBatchesTypes();
		obt.setBatchTypeId(new Integer(OimConstants.ORDERBATCH_TYPE_ID_MANUAL));
		b.setOimOrderBatchesTypes(obt);

		OimChannels c = new OimChannels();
		c.setChannelId(m_channelId);
		b.setOimChannels(c);
		dbSession.save(b);
		System.out.println("Saved batch id: " + b.getBatchId());

		Query q = dbSession
				.createQuery("from salesmachine.hibernatedb.OimChannelSupplierMap m where m.deleteTm is null and m.oimChannels=:channel");
		Map supplierMap = new HashMap();
		for (Iterator it = q.setEntity("channel", c).list().iterator(); it
				.hasNext();) {
			OimChannelSupplierMap map = (OimChannelSupplierMap) it.next();

			String prefix = map.getSupplierPrefix();
			OimSuppliers supplier = map.getOimSuppliers();
			LOG.info("prefix# {} supplierID# {} ", prefix,
					supplier.getSupplierId());
			supplierMap.put(prefix, supplier);
		}

		for (Iterator oit = b.getOimOrderses().iterator(); oit.hasNext();) {
			OimOrders order = (OimOrders) oit.next();
			order.setOimOrderBatches(b);
			order.setOrderFetchTm(new Date());
			order.setInsertionTm(new Date());
			order.setDeleteTm(new Date());
			dbSession.save(order);
			LOG.info("Saved order id: {}", order.getOrderId());

			for (Iterator dit = order.getOimOrderDetailses().iterator(); dit
					.hasNext();) {
				OimOrderDetails detail = (OimOrderDetails) dit.next();
				detail.setOimOrders(order);
				detail.setInsertionTm(new Date());
				detail.setDeleteTm(new Date());
				LOG.info("Processing order detail with SKU:{}", detail.getSku());
				if (detail.getSku() != null && detail.getSku().length() > 2) {
					String prefix = StringHandle.removeNull(detail.getSku())
							.substring(0, 2);
					if (supplierMap.containsKey(prefix)) {
						OimSuppliers supplier = (OimSuppliers) supplierMap
								.get(prefix);
						detail.setOimSuppliers(supplier);
					}
				}
				dbSession.save(detail);
				LOG.info("Saved detail id:{}", detail.getDetailId());
			}
		}
	}

	private OimUploadedFiles saveUploadedFileAsDeleted(Session dbSession,
			OimOrderBatches b, Integer filetypeId, File file,
			String uploadFileName) {
		OimUploadedFiles ouf = new OimUploadedFiles();
		ouf.setOimOrderBatches(b);

		OimFiletypes oft = new OimFiletypes();
		oft.setFileTypeId(filetypeId);
		ouf.setOimFiletypes(oft);

		OimChannels c = new OimChannels();
		c.setChannelId(m_channelId);
		ouf.setOimChannels(c);

		ouf.setInsertionTm(new Date());
		ouf.setDeleteTm(new Date());
		ouf.setFileName(uploadFileName);
		ouf.setFileSz(new Double(file.length()));

		dbSession.save(ouf);
		return ouf;
	}

	private void saveFileFormatParam(Session dbSession, OimFiletypes oft,
			String paramName, String paramValue) {
		OimFileformatParams params = new OimFileformatParams();
		params.setInsertionTm(new Date());
		params.setOimFiletypes(oft);
		params.setParamName(paramName);
		params.setParamValue(paramValue);
		dbSession.save(params);
	}

	public static boolean isPureAscii(String data) {
		byte bytearray[] = data.getBytes();
		CharsetDecoder d = Charset.forName("US-ASCII").newDecoder();
		try {
			CharBuffer r = d.decode(ByteBuffer.wrap(bytearray));
			r.toString();
		} catch (CharacterCodingException e) {
			return false;
		}
		return true;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/pull")
	public ResponseEntity<String> pullOrders() {
		try {
			ReadEvent<String> readEvent = channelService
					.pullOrders(new RequestReadEvent<Channel>());
			return new ResponseEntity<String>(readEvent.getEntity(),
					HttpStatus.OK);
		} catch (RuntimeException e) {
			return new ResponseEntity<String>(
					"Channel not configured properly", HttpStatus.OK);
		}
	}
}
