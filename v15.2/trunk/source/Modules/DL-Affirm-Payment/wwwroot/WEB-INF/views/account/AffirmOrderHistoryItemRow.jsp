<%@ include file="/WEB-INF/views/common/TagLibs.jsp" %>
<%@ page import="com.marketlive.entity.currency.Amount" %>

<%----------------------------------------------------------------------------------------------------------------------
  -    Description:
  -  Storyboard ID:
  - Pre-conditions:
  -     Model/Form:
  - URLs Posted To:
  -   Current Tile: orderHistoryItemRow
  - Tile Variables: orderInfo (IOrder)
  -  Child Tile(s):
  -      Copyright: (c) 2014 MarketLive, Inc. All Rights Reserved.
  --------------------------------------------------------------------------------------------------------------------%>

<%--
    //(C) Copyright MarketLive. 2014. All rights reserved.
    //MarketLive is a trademark of MarketLive, Inc.
    //Warning: This computer program is protected by copyright law and international treaties.
    //Unauthorized reproduction or distribution of this program, or any portion of it, may result
    //in severe civil and criminal penalties, and will be prosecuted to the maximum extent
    //possible under the law.
--%>

<tilesx:useAttribute id="orderInfo" name="orderInfo"/>
<c:set var="zeroAmountCheck" value="true" />

<c:if test="${!empty orderInfo.code}">
    <c:set var="orderNumber" value="${accountRegisterForm.orderNumberPrefix}${orderInfo.code}" scope="page"/>
</c:if>
<c:if test="${empty orderInfo.code }">
    <c:set var="orderNumber" value="${accountRegisterForm.orderNumberPrefix}${orderInfo.pk.asString}" scope="page"/>
</c:if>
<div class="row">

<%--  Order Number--%>
	<div class="col-sm-6 col-md-2">
		<fmt:message key="hdr.myAccount.orderHistory.table.orderNumber" />
		<span><a href='<c:url value="${mlnav:urlWrapper('/account/orderdetail.do', pageContext)}?orderNumber=${orderInfo.pk.asString}" />'>${orderNumber} </a></span>
	</div>
	<div class="col-sm-6 col-md-3">
<%--  Order Date--%>
		<fmt:message key="hdr.myAccount.orderHistory.table.orderDate" />
		<span><fmt:formatDate dateStyle="long" pattern= "MMMM dd, YYYY" value="${orderInfo.dateOrdered}" /></span>
	</div>

<%--  Order Status--%>
<c:choose>
	<c:when test="${accountRegisterForm.showOrderStatus && orderInfo.status == null}">
		<td data-title="<fmt:message key='hdr.myAccount.orderHistory.table.orderStatus'/>"><fmt:message key="msg.account.order.noStatus"/></td>
	</c:when>
	<c:when test="${accountRegisterForm.showOrderStatus}">
		<div class="col-sm-6 col-md-2">
			<fmt:message key="hdr.myAccount.orderHistory.table.orderStatus"/>
			<span>${orderInfo.status}</span>
		</div>
	</c:when>
	<c:otherwise>
	</c:otherwise>
</c:choose>



<%--  Order Total --%>
<c:forEach var="payment" items="${orderInfo.payments}" >
	<c:choose>
		<c:when test="${payment.description == 'CREDIT CARD' or payment.description == 'PAYPAL' or payment.description == 'AMAZON'  or payment.description == 'AFFIRM'}">
				<c:set var="zeroAmountCheck" value="false" />
		</c:when>
	</c:choose>
</c:forEach>
<div class="col-sm-6 col-md-2">
	<fmt:message key="hdr.myAccount.orderHistory.table.orderTotalCharge" /> 
	<c:choose>
		<c:when  test = "${zeroAmountCheck == true}">
			<span><mlamt:formatAmount value="<%=new Amount()%>" /></span>
		</c:when>
		<c:otherwise>
			<c:forEach var="payment" items="${orderInfo.payments}" >
				<c:choose>
					<c:when test="${payment.description == 'CREDIT CARD' or payment.description == 'PAYPAL' or payment.description == 'AMAZON' or payment.description == 'AFFIRM'}">
						<span><mlamt:formatAmount value="${payment.amount}" /></span>
					</c:when>
				</c:choose>
			</c:forEach>
		</c:otherwise>
	</c:choose>
</div>

<%--  Shipment Tracking --%>
<c:choose>
  <c:when test="${empty orderInfo.trackings && accountRegisterForm.showOrderTracking}">
	<div class="col-xs-12 col-sm-6 col-md-3">
		<fmt:message key="hdr.myAccount.orderHistory.table.shipmentTracking" />
		<span><fmt:message key="msg.account.order.noTracking"/></span>
	</div>
  </c:when>
  <c:when test="${!accountRegisterForm.showOrderTracking}"> </c:when>
  <c:otherwise>
  <div class="col-xs-12 col-sm-6 col-md-3">
    <fmt:message key="hdr.myAccount.orderHistory.table.shipmentTracking"/>
    <c:forEach var="tracking" varStatus="status" items="${orderInfo.trackings}">
	<c:if test="${!status.first}"> </c:if>
	<div class="shipment-tracking">${tracking.carrierName}<c:choose><c:when test="${tracking.trackingNumber != null && fn:length(tracking.trackingNumber) > 0}"><fmt:message key="msg.common.optionSeparator" /><c:choose>
                <c:when test="${tracking.carrierURL != null && fn:length(tracking.carrierURL) > 0}"><a href="#" onClick="return MarketLive.Base.flyopen(${accountRegisterForm.shipmentCarrierWxH}, '<c:url value="${tracking.carrierURL}" />', 'tracking')">${tracking.trackingNumber}</a></c:when>
              </c:choose>
            </c:when>
          </c:choose>
	</div>	  
    </c:forEach>
  </div>
  </c:otherwise>
</c:choose>
</div>
