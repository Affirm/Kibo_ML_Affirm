<%@ page import="org.marketlive.entity.sourcecode.ISourceCode"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
// File Name	:  orderdetail.jsp
// Created By	:  05/18/03, Uday Devata
%>

<%--
    //(C) Copyright MarketLive. 2006. All rights reserved.
    //MarketLive is a trademark of MarketLive, Inc.
    //Warning: This computer program is protected by copyright law and international treaties.
    //Unauthorized reproduction or distribution of this program, or any portion of it, may result
    //in severe civil and criminal penalties, and will be prosecuted to the maximum extent
    //possible under the law.
--%>
<%@ include file="/admin/include/TagLibs.jsp"%>
<%@ include file="orderdetaillogic.jsp"%>
<c:set var="title" scope="request" value="" />
<c:set var="suppressCommonJS" value="false" scope="request" />
<c:set var="form" value="true" scope="request" />
<c:set var="flyopen" value="true" scope="request" />
<c:set var="adminreports" value="true" scope="request" />
<jsp:include page="/admin/include/HeaderInclude.jsp" />

<script language="javascript">
    jQuery(document).ready(function() {
        if (typeof(top.frameset) != 'undefined' && typeof(top.frameset.menutree) != 'undefined'){
            top.frameset.menutree.menuTree.resizeFrame(top.frameset.menutree.menuTree.orderDetailDiv);
            if (top.frameset.menutree.menuTree.printOrderDetailButtonBar != null){
                top.frameset.menutree.menuTree.printOrderDetailButtonBar.show();
            }
        }
    });
</script>
<style type="text/css">
.detailOrder{width:860px; padding:10px; background:#fff;}
	@media print
    {
		.detailOrder{width:97% !important;}
    }
    <!--
    @page { size:8.5in 11in; margin: 2cm; }
	-->
  </style>
</head>
<body class="popupBody" topmargin="0" rightmargin="0" leftmargin="0" style=""	BGCOLOR="#888888">
	<center>
		<form name="address" action="addresstab.jsp" method="post">
			<input type="hidden" name="iCustomerID" value="<%=customerID%>">
			<div class="detailOrder">
			<table BORDER="0" CELLPADDING="5" CELLSPACING="0" WIDTH="100%">
				<tr style="background-color:#DDDDDD">
					<td  class="text-normal" nowrap colspan="2" valign="top"><b>Order Info</b></td>
					<td  class="text-normal" nowrap colspan="2" valign="top"><b>Shipment Tracking</b></td>
				</tr>
				<tr>
					<td class="text-normal" nowrap colspan="2" valign="top">
						<div class="text-normal" nowrap>
							Order #:
							<%=orderReview.getPk().getAsString()%></div>
						<div class="text-normal" nowrap>
							Order Code:
							<%=orderReview.getCode()%></div>	
							
						<div class="text-normal">
							Date:
							<%=sDateOrdered%></div>
						<div class="text-normal"><%=sStatus%></div>
					</td>
					<td class="text-normal" nowrap colspan=2 valign="top"><%=sShipmentTracking%></td>
				</tr>
                <tr>
                    <td class="text-normal" colspan="2" valign="top">Comments:<br/><%=orderComments%></td>
                </tr>
				<tr style="background-color:#DDDDDD">
					<td class="text-normal" nowrap colspan=2 valign="top"><b>Billing Info</b></td>
					<td class="text-normal" nowrap colspan=2 valign="top"><b>Payment Method</b></td>
				</tr>
				<tr>
					<td class="text-normal" nowrap colspan=2 valign="top"><%=sBillInfo%></td>
					<td class="text-normal" nowrap colspan=2 valign="top"><%=sPaymentMethod%>
					<%if(sPaymentRequestId !=null && sAffirmPaymentMerchantUrl !=null && sAffirmPaymentPublicKey != null){%>
						<a target="_blank" href="<%=sAffirmPaymentMerchantUrl%><%=sPaymentRequestId%>?trk=<%=sAffirmPaymentPublicKey%>"><%=sPaymentRequestId%></a>
					<%
					}
					%>
					</td>
				</tr>
				<%
					for (int i = 0; i < shipmentCount; i++) {
				%>
				<tr>
					<td colspan=3><hr></td>
				</tr>
				<tr>
					<td class="text-normal" nowrap><b>Shipment<b>&nbsp;</b><%=(i + 1)%></b></td>
					<td class="text-normal">&nbsp;</td>
					<td class="text-normal"><b>Date Shipped:</b>&nbsp;<%=sDateShipped[i]%></td>
				</tr>
				<tr style="background-color:#DDDDDD">
					<td class="text-normal" nowrap colspan=2 valign="top"><b>Shipping Info</b></td>
					<td class="text-normal" nowrap colspan=2 valign="top"><b>Shipping Method</b></td>
				</tr>
				<tr>
					<td class="text-normal" nowrap colspan=2 valign="top"><br><%=sShipInfo[i]%></td>
					<td class="text-normal" wrap colspan=2 valign="top" width='45%'><br><%=sShipMethod[i]%></td>
				</tr>
				<% if (sShipGiftMessage[i] !=null){%>
				<tr>
					<td class="text-normal" nowrap colspan=4 valign="top">
						<b>Gift Message</b> : <%=sShipGiftMessage[i]%>
					</td>
				<tr>
				<%
					}
				%>
				</tr>
				<tr>
					<td class="text-normal" nowrap colspan=4 valign="top">
						<table border="0" width='100%' cellpadding="5" cellspacing="0">
							<%=sLineItems[i].toString()%>
						</table>
					</td>
				</tr>
				<%
					}
				%>
				<tr>
					<td colspan=3><hr></td>
				</tr>
				<tr>
					<%--
    <% if(showItemizedDiscounts) {%>
        <%=sOrderDiscountMsg.toString()%>
    <%}%>
    --%>
				<td class="text-normal" nowrap colspan=6 valign="top" align="right">
					<table border="0" cellpadding="5" cellspacing="0">
						<%=sOrderTotals.toString()%>
					</table>
				</td>
				</tr>
			</table>
			</div>
		</form>
    </center>
</body>
<jsp:include page="/admin/include/FooterInclude.jsp" />
</html>
