<%@ include file="/WEB-INF/views/common/TagLibs.jsp" %>

<tiles:importAttribute name="categoryItem"/>

<ct:call object="${PricingModel}" method="getPriceInfo" itemParam="${categoryItem}" sessionParam="${sessionScope.COMMERCE_SESSION}" return="priceInfo"/>

<c:choose>
   <%-- Was / Is --%>
  <c:when test="${priceInfo.wasIs || priceInfo.onSale}">
   	<c:set var="productPrice" value="${priceInfo.minSalePrice.getAsString()}" />
  </c:when>
  <c:otherwise>
   	<c:set var="productPrice" value="${priceInfo.minPrice.getAsString()}" />
   </c:otherwise>
   </c:choose>
    
<div class="dl-affirm-monthly-payment-price">
		<a class="learn-more" style="visibility:hidden" href="#" data-affirmprice="${productPrice}"></a>
</div>
	