package salesmachine.oim.suppliers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Date;

import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import salesmachine.email.EmailUtil;
import salesmachine.hibernatedb.OimChannelAccessDetails;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimVendorSuppliers;
import salesmachine.hibernatedb.Reps;
import salesmachine.hibernatedb.Vendors;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.stores.exception.ChannelCommunicationException;
import salesmachine.oim.stores.exception.ChannelConfigurationException;
import salesmachine.oim.stores.exception.ChannelOrderFormatException;
import salesmachine.oim.suppliers.exception.SupplierCommunicationException;
import salesmachine.oim.suppliers.exception.SupplierConfigurationException;
import salesmachine.oim.suppliers.exception.SupplierOrderException;
import salesmachine.oim.suppliers.exception.SupplierOrderTrackingException;
import salesmachine.oim.suppliers.modal.OrderDetailResponse;
import salesmachine.oim.suppliers.modal.OrderStatus;

public class Europa extends Supplier implements HasTracking {
  private static final Logger log = LoggerFactory.getLogger(Europa.class);

  @Override
  public void sendOrders(Integer vendorId, OimVendorSuppliers ovs, OimOrders order)
      throws SupplierConfigurationException, SupplierCommunicationException, SupplierOrderException,
      ChannelConfigurationException, ChannelCommunicationException, ChannelOrderFormatException {

    log.info("Sending orders of Account: {}", ovs.getAccountNumber());
    if (ovs.getTestMode().equals(1))
      return;
    orderSkuPrefixMap = setSkuPrefixForOrders(ovs);
    Session session = SessionManager.currentSession();
    Reps r = (Reps) session.createCriteria(Reps.class).add(Restrictions.eq("vendorId", vendorId))
        .uniqueResult();
    Vendors v = new Vendors();
    v.setVendorId(r.getVendorId());

    String poNum;
    Query query = session.createSQLQuery(
        "select  distinct SUPPLIER_ORDER_NUMBER from kdyer.OIM_ORDER_DETAILS where ORDER_ID=:orderId and SUPPLIER_ID=:supplierId");
    query.setInteger("orderId", order.getOrderId());
    query.setInteger("supplierId", ovs.getOimSuppliers().getSupplierId());
    Object q = null;
    try {
      q = query.uniqueResult();
    } catch (NonUniqueResultException e) {
      log.error(
          "This order has more than one product having different PO number. Please make them unique. store order id is - {}",
          order.getStoreOrderId());
      throw new SupplierConfigurationException(
          "This order has more than one product having different PO number. Please make them unique.");
    }
    if (q != null) {
      poNum = (String) q;
      log.info("Reprocessing po - {}", poNum);
    } else {
      poNum = ovs.getVendors().getVendorId() + "-" + order.getStoreOrderId();
    }

    String orderFileName = null;
    try {
      orderFileName = createPdfFile(order, ovs);
    } catch (FileNotFoundException | DocumentException e) {
      log.error("Error occured while creating order file for Europa. ", e);
      throw new ChannelOrderFormatException("Error occured while creating order file for Europa.",
          e);
    }

    if (orderFileName != null) {
      sendEmail(orderFileName, ovs, poNum, "weborders@europasports.com");
      sendEmail(orderFileName, ovs, poNum, "orders@inventorysource.com");
      log.info("order sent successfully to supplier.");
    }
    for (OimOrderDetails od : order.getOimOrderDetailses()) {

      successfulOrders.put(od.getDetailId(), new OrderDetailResponse(poNum,
          OimConstants.OIM_SUPPLER_ORDER_STATUS_SENT_TO_SUPPLIER, null));
    }

  }

  private static void sendEmail(String orderFileName, OimVendorSuppliers ovs, String poNumber,
      String TO) {
    String emailSubject = "Business Name: " + ovs.getLogin() + ", Europa Customer Number : "
        + ovs.getAccountNumber() + ", PO Number : " + poNumber;
    EmailUtil.sendEmailWithAttachment(TO, "support@inventorysource.com", "", emailSubject, "",
        orderFileName);
  }

  public static void main(String[] args) throws FileNotFoundException, DocumentException {
    Europa e = new Europa();
    Session session = SessionManager.currentSession();
    OimOrders order = (OimOrders) session.get(OimOrders.class, 417901);
    OimVendorSuppliers ovs = (OimVendorSuppliers) session.get(OimVendorSuppliers.class, 10181);
    String orderFileName = e.createPdfFile(order, ovs);
    String poNum = ovs.getVendors().getVendorId() + "-" + order.getStoreOrderId();
    sendEmail(orderFileName, ovs, poNum, "manish@inventorysource.com");
    System.out.println("Done!!!");
  }

  private static final Font HEADER_BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA, 12,
      Font.BOLD);
  private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 8,
      Font.NORMAL);
  private static final Font BODY_BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA, 8,
      Font.BOLD);
  private static final Font BODY_HEADER_BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA, 8,
      Font.BOLD, BaseColor.BLUE);

  public String createPdfFile(OimOrders order, OimVendorSuppliers ovs)
      throws FileNotFoundException, DocumentException {
    Document document = new Document();
    String poNum = ovs.getVendors().getVendorId() + "-" + order.getStoreOrderId();
    String orderFileName = "/tmp/Europa_PO-" + poNum + ".pdf";
    File file = new File(orderFileName);
    PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
    document.open();
    String strUrl = null;
    for (OimChannelAccessDetails accessDetail : order.getOimOrderBatches().getOimChannels()
        .getOimChannelAccessDetailses()) {
      if (accessDetail.getOimChannelAccessFields().getFieldId() == 1)
        strUrl = accessDetail.getDetailFieldValue();
    }
    document.add(new Paragraph(order.getOimOrderBatches().getOimChannels().getChannelName(),
        HEADER_BOLD_FONT));
    document.add(
        new Paragraph("Thank you for your order! Please visit us again at " + strUrl, NORMAL_FONT));

    PdfPTable mainTable = new PdfPTable(2);
    mainTable.setWidthPercentage(100); // Width
    mainTable.setSpacingBefore(10f); // Space before table
    mainTable.setSpacingAfter(10f); // Space after table
    // mainTable.setHorizontalAlignment(mainTable.ALIGN_CENTER);

    float[] columnWidths = { 1f, 1f };
    mainTable.setWidths(columnWidths);

    PdfPTable shipToTable = createShipToTable(order);
    PdfPTable orderDetailsTable = createOrderDetailTable(order);
    PdfPCell cell1 = new PdfPCell(shipToTable);
    PdfPCell cell2 = new PdfPCell(orderDetailsTable);
    mainTable.addCell(cell1);
    mainTable.addCell(cell2);
    document.add(mainTable);
    PdfPTable itemsTable = createItemsTable(order, ovs);
    document.add(itemsTable);
    document.close();
    writer.close();

    return orderFileName;
  }

  private PdfPTable createItemsTable(OimOrders order, OimVendorSuppliers ovs)
      throws DocumentException {
    PdfPTable itemTable = new PdfPTable(5);

    itemTable.setWidthPercentage(100); // Width
    itemTable.setSpacingBefore(10f); // Space before table
    itemTable.setSpacingAfter(10f); // Space after table
    itemTable.setHorizontalAlignment(PdfPTable.ALIGN_JUSTIFIED);

    float[] columnWidths = { 1f, 1f, 1f, 1f, 1f };
    itemTable.setWidths(columnWidths);

    PdfPCell cell1 = new PdfPCell(new Paragraph("Ordered Items", BODY_HEADER_BOLD_FONT));
    cell1.setPaddingLeft(10);
    cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
    cell1.setVerticalAlignment(Element.ALIGN_LEFT);
    cell1.setColspan(5);
    cell1.setBackgroundColor(BaseColor.GRAY);

    PdfPCell skuHeaderCell = new PdfPCell(new Paragraph("SKU", BODY_BOLD_FONT));
    skuHeaderCell.setBorderColorRight(BaseColor.WHITE);
    skuHeaderCell.setPaddingLeft(10);
    skuHeaderCell.setHorizontalAlignment(Element.ALIGN_LEFT);
    skuHeaderCell.setVerticalAlignment(Element.ALIGN_LEFT);

    PdfPCell qtyHeaderCell = new PdfPCell(new Paragraph("Quantity", BODY_BOLD_FONT));
    qtyHeaderCell.setBorderColorRight(BaseColor.WHITE);
    qtyHeaderCell.setPaddingLeft(10);
    qtyHeaderCell.setHorizontalAlignment(Element.ALIGN_LEFT);
    qtyHeaderCell.setVerticalAlignment(Element.ALIGN_LEFT);

    PdfPCell descHeaderCell = new PdfPCell(new Paragraph("Description", BODY_BOLD_FONT));
    descHeaderCell.setBorderColorRight(BaseColor.WHITE);
    descHeaderCell.setPaddingLeft(10);
    descHeaderCell.setHorizontalAlignment(Element.ALIGN_LEFT);
    descHeaderCell.setVerticalAlignment(Element.ALIGN_LEFT);

    PdfPCell costHeaderCell = new PdfPCell(new Paragraph("Unit Cost", BODY_BOLD_FONT));
    costHeaderCell.setBorderColorRight(BaseColor.WHITE);
    costHeaderCell.setPaddingLeft(10);
    costHeaderCell.setHorizontalAlignment(Element.ALIGN_LEFT);
    costHeaderCell.setVerticalAlignment(Element.ALIGN_LEFT);

    PdfPCell totalHeaderCell = new PdfPCell(new Paragraph("Total", BODY_BOLD_FONT));
    totalHeaderCell.setBorderColorRight(BaseColor.WHITE);
    totalHeaderCell.setPaddingLeft(10);
    totalHeaderCell.setHorizontalAlignment(Element.ALIGN_LEFT);
    totalHeaderCell.setVerticalAlignment(Element.ALIGN_LEFT);

    itemTable.addCell(cell1);
    itemTable.addCell(skuHeaderCell);
    itemTable.addCell(qtyHeaderCell);
    itemTable.addCell(descHeaderCell);
    itemTable.addCell(costHeaderCell);
    itemTable.addCell(totalHeaderCell);

    for (OimOrderDetails detail : order.getOimOrderDetailses()) {
      // if
      // (!detail.getOimSuppliers().getSupplierId().equals(ovs.getOimSuppliers().getSupplierId()))
      // continue;
      String sku = detail.getSku();
      // String skuPrefix = null,
      // if (!orderSkuPrefixMap.isEmpty()) {
      // skuPrefix = orderSkuPrefixMap.values().toArray()[0].toString();
      // }
      // skuPrefix = StringHandle.removeNull(skuPrefix);
      // if (sku.startsWith(skuPrefix)) {
      // sku = sku.substring(skuPrefix.length());
      // }
      PdfPCell skuValueCell = new PdfPCell(new Paragraph(sku, NORMAL_FONT));
      skuValueCell.setPaddingLeft(10);
      skuValueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
      skuValueCell.setVerticalAlignment(Element.ALIGN_LEFT);
      itemTable.addCell(skuValueCell);

      PdfPCell qtyValueCell = new PdfPCell(new Paragraph(detail.getQuantity() + "", NORMAL_FONT));
      qtyValueCell.setPaddingLeft(10);
      qtyValueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
      qtyValueCell.setVerticalAlignment(Element.ALIGN_LEFT);
      itemTable.addCell(qtyValueCell);

      PdfPCell descValueCell = new PdfPCell(new Paragraph(detail.getProductName(), NORMAL_FONT));
      descValueCell.setPaddingLeft(10);
      descValueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
      descValueCell.setVerticalAlignment(Element.ALIGN_LEFT);
      itemTable.addCell(descValueCell);

      PdfPCell costValueCell = new PdfPCell(new Paragraph(detail.getCostPrice() + "", NORMAL_FONT));
      costValueCell.setPaddingLeft(10);
      costValueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
      costValueCell.setVerticalAlignment(Element.ALIGN_LEFT);
      itemTable.addCell(costValueCell);

      double totalCost = detail.getQuantity() * detail.getSalePrice();
      PdfPCell totalCostValueCell = new PdfPCell(new Paragraph(totalCost + "", NORMAL_FONT));
      totalCostValueCell.setPaddingLeft(10);
      totalCostValueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
      totalCostValueCell.setVerticalAlignment(Element.ALIGN_LEFT);
      itemTable.addCell(totalCostValueCell);
    }
    PdfPCell blankCell = new PdfPCell(new Paragraph("", NORMAL_FONT));
    blankCell.setColspan(3);
    blankCell.setBorder(0);
    itemTable.addCell(blankCell);

    PdfPCell totalCell = new PdfPCell(new Paragraph("Total", BODY_BOLD_FONT));
    totalCell.setPaddingLeft(10);
    totalCell.setHorizontalAlignment(Element.ALIGN_LEFT);
    totalCell.setVerticalAlignment(Element.ALIGN_LEFT);
    itemTable.addCell(totalCell);

    PdfPCell totalValueCell = new PdfPCell(
        new Paragraph(order.getOrderTotalAmount() + "", NORMAL_FONT));
    totalValueCell.setPaddingLeft(10);
    totalValueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
    totalValueCell.setVerticalAlignment(Element.ALIGN_LEFT);
    itemTable.addCell(totalValueCell);

    return itemTable;
  }

  private static PdfPTable createOrderDetailTable(OimOrders order) throws DocumentException {
    PdfPTable orderDetailTable = new PdfPTable(2);
    orderDetailTable.setWidthPercentage(40); // Width
    orderDetailTable.setSpacingBefore(10f); // Space before table
    orderDetailTable.setSpacingAfter(10f); // Space after table
    orderDetailTable.setHorizontalAlignment(PdfPTable.ALIGN_RIGHT);

    float[] columnWidths = { 1f, 1f };
    orderDetailTable.setWidths(columnWidths);

    PdfPCell cell1 = new PdfPCell(new Paragraph("Order Details", BODY_HEADER_BOLD_FONT));
    cell1.setPaddingLeft(10);
    cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
    cell1.setVerticalAlignment(Element.ALIGN_LEFT);
    cell1.setColspan(2);
    cell1.setBackgroundColor(BaseColor.GRAY);
    // cell1.setHorizontalAlignment(1);

    PdfPCell dateHeaderCell = new PdfPCell(new Paragraph("Date", BODY_BOLD_FONT));
    dateHeaderCell.setBorderColorRight(BaseColor.WHITE);
    dateHeaderCell.setPaddingLeft(10);
    dateHeaderCell.setHorizontalAlignment(Element.ALIGN_LEFT);
    dateHeaderCell.setVerticalAlignment(Element.ALIGN_LEFT);
    dateHeaderCell.setBorderWidthRight(0);
    dateHeaderCell.setBorderWidthBottom(0);

    PdfPCell dateValueCell = new PdfPCell(new Paragraph(new Date().toString(), NORMAL_FONT));
    dateValueCell.setPaddingLeft(10);
    dateValueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
    dateValueCell.setVerticalAlignment(Element.ALIGN_LEFT);
    dateValueCell.setBorderWidthLeft(0);
    dateValueCell.setBorderWidthBottom(0);

    PdfPCell orderNoHeaderCell = new PdfPCell(new Paragraph("Order No.", BODY_BOLD_FONT));
    orderNoHeaderCell.setBorderColorRight(BaseColor.WHITE);
    orderNoHeaderCell.setPaddingLeft(10);
    orderNoHeaderCell.setHorizontalAlignment(Element.ALIGN_LEFT);
    orderNoHeaderCell.setVerticalAlignment(Element.ALIGN_LEFT);
    orderNoHeaderCell.setBorderWidthRight(0);
    orderNoHeaderCell.setBorderWidthBottom(0);
    orderNoHeaderCell.setBorderWidthTop(0);

    PdfPCell orderNoValueCell = new PdfPCell(new Paragraph(order.getStoreOrderId(), NORMAL_FONT));
    orderNoValueCell.setPaddingLeft(10);
    orderNoValueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
    orderNoValueCell.setVerticalAlignment(Element.ALIGN_LEFT);
    orderNoValueCell.setBorderWidthLeft(0);
    orderNoValueCell.setBorderWidthBottom(0);
    orderNoValueCell.setBorderWidthTop(0);
    // Shipping Method

    PdfPCell shippingHeaderCell = new PdfPCell(new Paragraph("Shipping Method", BODY_BOLD_FONT));
    shippingHeaderCell.setBorderColorRight(BaseColor.WHITE);
    shippingHeaderCell.setPaddingLeft(10);
    shippingHeaderCell.setHorizontalAlignment(Element.ALIGN_LEFT);
    shippingHeaderCell.setVerticalAlignment(Element.ALIGN_LEFT);
    shippingHeaderCell.setBorderWidthRight(0);
    shippingHeaderCell.setBorderWidthBottom(0);
    shippingHeaderCell.setBorderWidthTop(0);

    PdfPCell shippingValueCell = new PdfPCell(
        new Paragraph(order.getOimShippingMethod().getFullName(), NORMAL_FONT));
    shippingValueCell.setPaddingLeft(10);
    shippingValueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
    shippingValueCell.setVerticalAlignment(Element.ALIGN_LEFT);
    shippingValueCell.setBorderWidthLeft(0);
    shippingValueCell.setBorderWidthBottom(0);
    shippingValueCell.setBorderWidthTop(0);

    orderDetailTable.addCell(cell1);
    orderDetailTable.addCell(dateHeaderCell);
    orderDetailTable.addCell(dateValueCell);
    orderDetailTable.addCell(orderNoHeaderCell);
    orderDetailTable.addCell(orderNoValueCell);
    orderDetailTable.addCell(shippingHeaderCell);
    orderDetailTable.addCell(shippingValueCell);
    return orderDetailTable;
  }

  private static PdfPTable createShipToTable(OimOrders order) throws DocumentException {
    PdfPTable shipToTable = new PdfPTable(2); // 2 columns.
    shipToTable.setWidthPercentage(60); // Width
    shipToTable.setSpacingBefore(10f); // Space before table
    shipToTable.setSpacingAfter(10f); // Space after table
    shipToTable.setHorizontalAlignment(PdfPTable.ALIGN_LEFT);

    // Set Column widths
    float[] columnWidths = { 1f, 1f };
    shipToTable.setWidths(columnWidths);

    PdfPCell cell1 = new PdfPCell(new Paragraph("Ship To", BODY_HEADER_BOLD_FONT));
    cell1.setPaddingLeft(10);
    cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
    cell1.setVerticalAlignment(Element.ALIGN_LEFT);
    cell1.setColspan(2);
    cell1.setBackgroundColor(BaseColor.GRAY);
    shipToTable.addCell(cell1);

    PdfPCell nameHeaderCell = new PdfPCell(new Paragraph("Name", BODY_BOLD_FONT));
    nameHeaderCell.setBorderColorRight(BaseColor.WHITE);
    nameHeaderCell.setPaddingLeft(10);
    nameHeaderCell.setHorizontalAlignment(Element.ALIGN_LEFT);
    nameHeaderCell.setVerticalAlignment(Element.ALIGN_LEFT);
    nameHeaderCell.setBorderWidthRight(0);
    nameHeaderCell.setBorderWidthBottom(0);

    PdfPCell nameValueCell = new PdfPCell(new Paragraph(order.getDeliveryName(), NORMAL_FONT));
    nameValueCell.setPaddingLeft(10);
    nameValueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
    nameValueCell.setVerticalAlignment(Element.ALIGN_LEFT);
    nameValueCell.setBorderWidthLeft(0);
    nameValueCell.setBorderWidthBottom(0);

    shipToTable.addCell(nameHeaderCell);
    shipToTable.addCell(nameValueCell);

    PdfPCell addressHederCell = new PdfPCell(new Paragraph("Address", BODY_BOLD_FONT));
    addressHederCell.setPaddingLeft(10);
    addressHederCell.setHorizontalAlignment(Element.ALIGN_LEFT);
    addressHederCell.setVerticalAlignment(Element.ALIGN_LEFT);
    addressHederCell.setBorderWidthRight(0);
    addressHederCell.setBorderWidthBottom(0);
    addressHederCell.setBorderWidthTop(0);

    PdfPCell addressValueCell = new PdfPCell(
        new Paragraph(order.getDeliveryStreetAddress(), NORMAL_FONT));
    addressValueCell.setPaddingLeft(10);
    addressValueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
    addressValueCell.setVerticalAlignment(Element.ALIGN_LEFT);
    addressValueCell.setBorderWidthLeft(0);
    addressValueCell.setBorderWidthBottom(0);
    addressValueCell.setBorderWidthTop(0);

    shipToTable.addCell(addressHederCell);
    shipToTable.addCell(addressValueCell);

    PdfPCell cityHederCell = new PdfPCell(new Paragraph("City", BODY_BOLD_FONT));
    cityHederCell.setPaddingLeft(10);
    cityHederCell.setHorizontalAlignment(Element.ALIGN_LEFT);
    cityHederCell.setVerticalAlignment(Element.ALIGN_LEFT);
    cityHederCell.setBorderWidthRight(0);
    cityHederCell.setBorderWidthBottom(0);
    cityHederCell.setBorderWidthTop(0);

    PdfPCell cityValueCell = new PdfPCell(new Paragraph(order.getDeliveryCity(), NORMAL_FONT));
    cityValueCell.setPaddingLeft(10);
    cityValueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
    cityValueCell.setVerticalAlignment(Element.ALIGN_LEFT);
    cityValueCell.setBorderWidthLeft(0);
    cityValueCell.setBorderWidthBottom(0);
    cityValueCell.setBorderWidthTop(0);

    shipToTable.addCell(cityHederCell);
    shipToTable.addCell(cityValueCell);

    PdfPCell stateHederCell = new PdfPCell(new Paragraph("State", BODY_BOLD_FONT));
    stateHederCell.setPaddingLeft(10);
    stateHederCell.setHorizontalAlignment(Element.ALIGN_LEFT);
    stateHederCell.setVerticalAlignment(Element.ALIGN_LEFT);
    stateHederCell.setBorderWidthRight(0);
    stateHederCell.setBorderWidthBottom(0);
    stateHederCell.setBorderWidthTop(0);

    PdfPCell stateValueCell = new PdfPCell(new Paragraph(order.getDeliveryStateCode() != null
        ? order.getDeliveryStateCode() : order.getDeliveryState(), NORMAL_FONT));
    stateValueCell.setPaddingLeft(10);
    stateValueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
    stateValueCell.setVerticalAlignment(Element.ALIGN_LEFT);
    stateValueCell.setBorderWidthLeft(0);
    stateValueCell.setBorderWidthBottom(0);
    stateValueCell.setBorderWidthTop(0);

    shipToTable.addCell(stateHederCell);
    shipToTable.addCell(stateValueCell);

    if (order.getDeliveryZip() != null) {
      PdfPCell zipHederCell = new PdfPCell(new Paragraph("Zip", BODY_BOLD_FONT));
      zipHederCell.setPaddingLeft(10);
      zipHederCell.setHorizontalAlignment(Element.ALIGN_LEFT);
      zipHederCell.setVerticalAlignment(Element.ALIGN_LEFT);
      zipHederCell.setBorderWidthRight(0);
      zipHederCell.setBorderWidthBottom(0);
      zipHederCell.setBorderWidthTop(0);

      PdfPCell zipValueCell = new PdfPCell(new Paragraph(order.getDeliveryZip(), NORMAL_FONT));
      zipValueCell.setPaddingLeft(10);
      zipValueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
      zipValueCell.setVerticalAlignment(Element.ALIGN_LEFT);
      zipValueCell.setBorderWidthLeft(0);
      zipValueCell.setBorderWidthBottom(0);
      zipValueCell.setBorderWidthTop(0);

      shipToTable.addCell(zipHederCell);
      shipToTable.addCell(zipValueCell);
    }

    if (order.getDeliveryPhone() != null) {
      PdfPCell phoneHederCell = new PdfPCell(new Paragraph("Phone", BODY_BOLD_FONT));
      phoneHederCell.setPaddingLeft(10);
      phoneHederCell.setHorizontalAlignment(Element.ALIGN_LEFT);
      phoneHederCell.setVerticalAlignment(Element.ALIGN_LEFT);
      phoneHederCell.setBorderWidthRight(0);
      phoneHederCell.setBorderWidthTop(0);

      PdfPCell phoneValueCell = new PdfPCell(new Paragraph("1234567890", NORMAL_FONT));
      phoneValueCell.setPaddingLeft(10);
      phoneValueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
      phoneValueCell.setVerticalAlignment(Element.ALIGN_LEFT);
      phoneValueCell.setBorderWidthLeft(0);
      phoneValueCell.setBorderWidthTop(0);

      shipToTable.addCell(phoneHederCell);
      shipToTable.addCell(phoneValueCell);
    }
    return shipToTable;
  }

  @Override
  public OrderStatus getOrderStatus(OimVendorSuppliers oimVendorSuppliers, Object trackingMeta,
      OimOrderDetails oimOrderDetails) throws SupplierOrderTrackingException {
    // TODO Auto-generated method stub
    return null;
  }

}
