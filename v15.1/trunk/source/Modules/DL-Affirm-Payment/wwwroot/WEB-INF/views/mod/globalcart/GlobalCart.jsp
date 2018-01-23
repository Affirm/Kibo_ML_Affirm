<%@ include file="/WEB-INF/views/common/TagLibs.jsp" %>
<%@ page import="com.marketlive.app.b2c.common.constants.RequestParams, com.marketlive.biz.borderfree.IBorderFreeManager"%>

<%----------------------------------------------------------------------------------------------------------------------
  -    Description: Global Cart Table, contains the table headers, and is a wrapper for looping over the rows.
  -  Storyboard ID:
  - Pre-conditions:
  -     Model/Form: com.marketlive.mod.globalcart.GlobalCartPreparer
  - URLs Posted To:
  -   Current Tile: .tile.nav.GlobalCart
  - Tile Variables:
  -  Child Tile(s): itemtablerow
  -      Copyright: (c) 2014 MarketLive, Inc. All Rights Reserved.
  --------------------------------------------------------------------------------------------------------------------%>
  
<jsp:useBean id="affirmWebUtil" class="com.deplabs.affirm.app.b2c.AffirmWebUtil"/>
<ct:call object="${affirmWebUtil}" method="isBorderFreeEnabledAndNotUSSelected"  fieldNameParam="${pageContext.request}" return="borderFreeEnabledAndNotUSSelected" />

<tilesx:useAttribute id="includedFrom" name="includedFrom" ignore="true" />

<%-- Affirm Monthly Payment Price --%>
<c:if test="${!borderFreeEnabledAndNotUSSelected}">
	<tiles:insertAttribute name="affirmMonthlyPaymentPrice"/>
</c:if>

<c:set var="gcQueryString"><%=RequestParams.GLOBAL_CART%>=<%=RequestParams.GLOBAL_CART_USED%></c:set>

<c:url var="basketURL" value="${mlnav:pathRef('/basket.do', pageContext)}" />

<ct:call object="${site.reportingModel}" method="addTrackingToURL" originalURLParam="${basketURL}" queryStringParams="${gcQueryString}"	return="basketURL" />

<c:set var="borderFreeEnable" value="${PricingModel.borderFreeIsEnable}" />
<c:set var="currency" value="${PricingModel.currentCurrency}" />
<c:set var="exchangeRate" value="${PricingModel.exchangeRate}" />
<c:set var="itemDataMap" value="${PricingModel.skuPriceQuoteMap}" />
<c:set var="basketTotalMap" value="${PricingModel.basketTotalMap}" />
<c:set var="selectedCountryIsUS" value="${PricingModel.selectedCountryIsUS}" />

<c:choose>
    <c:when test ="${borderFreeEnable && !selectedCountryIsUS && !empty basketTotalMap}">
        <c:set var="exchangeRate" value="${PricingModel.noExchangeRate}" />
        <c:set var="merchandiseTotal" value="${basketTotalMap['totalSalePrice']}" />
        <c:if test ="${empty merchandiseTotal}">
            <c:set var="merchandiseTotal" value="${GlobalCartModel.basket.merchandiseTotal}" />
        </c:if>

        <c:set var="basketSubTotal" value="${basketTotalMap['basketPageTotalPrice']}" />
        <c:if test ="${empty basketSubTotal}">
            <c:set var="basketSubTotal" value="${GlobalCartModel.basketSubTotal}" />
        </c:if>
        <c:set var="discountTotal" value="${basketTotalMap['orderDiscount']}" />
        <c:if test ="${empty discountTotal}">
            <c:set var="discountTotal" value="${BasketTableModel.discountTotal}" />
        </c:if>

</c:when>
<c:otherwise>
<c:set var="merchandiseTotal" value="${GlobalCartModel.basket.merchandiseTotal}" />
<c:set var="basketSubTotal" value="${GlobalCartModel.basketSubTotal}" />
</c:otherwise>
</c:choose>

<c:set var="countSummary">
    <c:choose>
        <c:when test="${!GlobalCartModel.showCartItemCountSummary}"></c:when>
        <c:when test="${empty GlobalCartModel.basket.items || GlobalCartModel.totalQty == 0 || GlobalCartModel.totalQty ==null}">
            <fmt:message key="msg.globalCart.itemsSummary">
                <fmt:param value="0" />
            </fmt:message>
        </c:when>
        <c:when test="${GlobalCartModel.totalQty == 1}">
            <fmt:message key="msg.globalCart.itemSummary">
                <fmt:param value="1" />
            </fmt:message>
        </c:when>
        <c:otherwise>
            <fmt:message key="msg.globalCart.itemsSummary">
                <fmt:param value="${GlobalCartModel.totalQty}" />
            </fmt:message>
        </c:otherwise>
    </c:choose>
</c:set>

<c:set var="totalSummary">
    <c:if test="${GlobalCartModel.showCartSubtotal}">
        <fmt:message key="msg.globalCart.totalSummary">
            <fmt:param>
                <mlamt:formatAmount currency ="${currency}" exchangeRate="${exchangeRate}" value="${GlobalCartModel.basketSubTotal}" />
            </fmt:param>
        </fmt:message>
    </c:if>
</c:set>

<c:set var="expandOnSummaryHoverAction" value="${GlobalCartModel.expandOnSummaryHoverAction}" />

<c:set var="expandOnLoadAction" value="${GlobalCartModel.expandOnLoadAction}" />

<c:set var="expandPane" value="${GlobalCartModel.expandPane}" />

<c:set var="a2cExpandPane" value="${GlobalCartModel.a2cExpandPane}" />
<c:if test="${a2cExpandPane == 'cartItemTable'}">
    <c:set var="expandPane" value="${GlobalCartModel.a2cExpandPane}" />
</c:if>

<c:choose>
    <c:when test="${GlobalCartModel.useMiniThumbnail}">
        <c:set var="thumbWidth" value="${site.layout.miniThumbWidth}" />
    </c:when>
    <c:otherwise>
        <c:set var="thumbWidth" value="${site.layout.thumbnailWidth}" />
    </c:otherwise>
</c:choose>
<mlperf:script defer="true" type="text/javascript">
    MarketLive.GlobalCart.onGlobalCartReady();
</mlperf:script>

<mlperf:script defer="true" type="text/javascript">
MarketLive.GlobalCart.initialize(${expandOnSummaryHoverAction}, ${expandOnLoadAction},
        "${basketURL}", ${GlobalCartModel.menuCloseDelay},
        "${GlobalCartModel.cartSummaryOnClass}", ${thumbWidth},
        ${GlobalCartModel.totalQty}, ${GlobalCartModel.displayPriceEach},
        ${GlobalCartModel.displayItemPrice},${GlobalCartModel.displayDiscount}, "${expandPane}");
if ('${includedFrom}' !='QVE') jQuery(document).ready(MarketLive.GlobalCart.adjustGlobalCartLayout(true));
</mlperf:script>

<div id="globalBasket" class="popDownWrapper globalCartWrapper ml-globalcart-container">
    <%-- =========================== Summary =========================== --%>
    <div class="popDownNav ml-header-global-cart">
        <a href="${basketURL}">
            <div class="ml-icon ml-icon-global-cart">
                <span class="ml-header-global-cart-count">${countSummary}</span>
            </div>
            <div class="ml-header-global-cart-text">
                <span class="ml-header-global-cart-label"><fmt:message key="hdr.globalCart.top.shoppingBasket"/></span>
                <c:if test="${!borderFreeEnable || selectedCountryIsUS}">
                    <span class="ml-header-global-cart-price">${totalSummary}</span>
                </c:if>
            </div>
        </a>
    </div>
    <%-- =========================== Detail =========================== --%>
    <div class="popDownLayer globalCartLayer popover bottom">
        <div class="arrow"></div>
        <div class="popover-content"> <%-- Start Bootstrap popover content --%>

        <c:if test="${expandOnSummaryHoverAction or expandOnLoadAction}">
        <c:choose>
            <%-- =========== No item ========== --%>
            <c:when test="${GlobalCartModel.totalQty == 0}">
                <div class="globalCartEmpty">
                    <fmt:message key="msg.globalCart.noItems" />
                </div>
            </c:when>
            <%-- ========== Many items ========== --%>
            <c:otherwise>
                <c:set var="MoreItemCount" scope="request" value="${GlobalCartModel.totalQty}" />
                <c:choose>
                    <%-- ========== Data for Just-Added layout ========== --%>
                    <c:when test="${expandPane == 'lastAddedItemTable'}">
                        <c:set var="basketItems"
                               value="${GlobalCartModel.lastItemAddedRows}" />
                        <c:if test="${GlobalCartModel.lastItemAddedTotalQty > 0}">
                            <div class="globalCartLastItemAddedMessage">
                                <span><fmt:message key="msg.globalCart.lastItemAdded.itemsAdded">
                                        <fmt:param value="${GlobalCartModel.lastItemAddedTotalQty}" />
                                </fmt:message></span>
                            </div>
                        </c:if>
                    </c:when>
                    <c:when test="${a2cExpandPane == 'lastAddedItemTable'}">
                        <c:set var="basketItems"
                               value="${GlobalCartModel.lastItemAddedRows}" />
                        <c:if test="${GlobalCartModel.lastItemAddedTotalQty > 0}">
                            <div class="globalCartLastItemAddedMessage">
                                <span><fmt:message key="msg.globalCart.lastItemAdded.itemsAdded">
                                        <fmt:param value="${GlobalCartModel.lastItemAddedTotalQty}" />
                                </fmt:message></span>
                            </div>
                        </c:if>
                    </c:when>
                    <%-- ========== Data for Default layout ========== --%>
                    <c:otherwise>
                        <c:set var="basketItems" value="${GlobalCartModel.sortedItems}" />
                        <div class="globalCartItemHeaderBlock text-lowercase">
                            <div class="globalCartItemHeaderItem">
                                <fmt:message key="hdr.globalCart.itemTable.item" />
                            </div>
                            <c:if test="${GlobalCartModel.displayPriceEach}">
                                <div class="globalCartItemHeaderPriceEach">
                                    <fmt:message key="hdr.globalCart.itemTable.priceEach" />
                                </div>
                            </c:if>
                            <c:if test="${GlobalCartModel.displayDiscount}">
                                <div id="discountColumnPresent" class="globalCartItemHeaderDiscount">
                                    <fmt:message key="hdr.globalCart.itemTable.discount" />
                                </div>
                            </c:if>
                            <c:if test="${GlobalCartModel.displayItemPrice}">
                                <div class="globalCartItemHeaderPrice">
                                    <fmt:message key="hdr.globalCart.itemTable.price" />
                                    <c:if test="${borderFreeEnable && currency != null}">
                                        <span class="ml-bf-currency-value">
                                            (${currency})
                                        </span>
                                    </c:if>
                                </div>
                            </c:if>
                        </div>
                    </c:otherwise>
                </c:choose>
                <c:choose>
                    <c:when test="${GlobalCartModel.displayMoreItems}">
                        <c:set var="maxCartItemCount" value="${GlobalCartModel.maxCartItemCount}" />
                    </c:when>
                    <c:otherwise>
                        <c:set var="maxCartItemCount" value="${fn:length(basketItems)}" />
                    </c:otherwise>
                </c:choose>
                <%-- ========== Loop in data and show Default/Just-Added layout ========== --%>
                <c:forEach var="basketItem" varStatus="status"
                           end="${maxCartItemCount-1}" items="${basketItems}">
                    <c:set var="itemIndex" value="${status.index}" scope="request" />

                    <%-- add borderfree: item price --%>
                    <c:set var="itemMap" value="${itemDataMap[basketItem.pk.asString]}" />
                    <c:choose>
                          <c:when test="${borderFreeEnable && !selectedCountryIsUS && !empty itemMap}">
                              <c:set var="regularPrice" value="${itemMap['listPrice']}" />
                              <c:if test ="${empty regularPrice}">
                                  <c:set var="regularPrice" value="${basketItem.regularPrice}" />
                              </c:if>
                              <c:set var="skuRegularPrice" value="${itemMap['listPrice']}" />
                              <c:if test ="${empty skuRegularPrice}">
                                  <c:set var="skuRegularPrice" value="${basketItem.skuRegularPrice}" />
                              </c:if>
                              <c:set var="itemTotalDiscount" value="${itemMap['itemDiscount']}" />
                              <c:if test ="${empty itemTotalDiscount}">
                                  <c:set var="itemTotalDiscount" value="${basketItem.totalDiscount}" />
                              </c:if>

                              <c:set var="itemSubTotal" value="${itemMap['itemSubtotal']}" />
                              <c:if test ="${empty itemSubTotal}">
                                  <c:set var="itemSubTotal" value="${basketItem.subTotal}" />
                              </c:if>
                          </c:when>
                          <c:otherwise>
                              <c:set var="regularPrice" value="${basketItem.regularPrice}" />
                              <c:set var="skuRegularPrice" value="${basketItem.skuRegularPrice}" />
                              <c:set var="itemTotalDiscount" value="${basketItem.totalDiscount}" />
                              <c:set var="itemSubTotal" value="${basketItem.subTotal}" />
                          </c:otherwise>
                     </c:choose>

                    <c:choose>
                        <c:when test="${GlobalCartModel.displayProductsAsLinks}">
                            <c:set var="ProductLink" scope="request">
                                <c:url value="${mlnav:entityRef(basketItem.product, pageContext)}" />;</c:set>
                        </c:when>
                        <c:otherwise>
                            <c:set var="ProductLink" value="" scope="request" />
                        </c:otherwise>
                    </c:choose>
                    <c:choose>
                        <c:when test="${GlobalCartModel.displayThumbnailsAsLinks}">
                            <c:set var="ProductThumbnailLink" scope="request">
                                <c:url value="${mlnav:entityRef(basketItem.product, pageContext)}" />;</c:set>
                        </c:when>
                        <c:otherwise>
                            <c:set var="ProductThumbnailLink" value="" scope="request" />
                        </c:otherwise>
                    </c:choose>

                    <c:choose>
                        <c:when test="${GlobalCartModel.displayPriceEach}">
                            <c:choose>
                                <c:when test="${basketItem.freeGift}">
                                    <c:set var="ProductPrice">
                                        <div class=cartmenupriceitem>
                                            <div class=messagefreegift>
                                                <fmt:message key="msg.common.freeGift" />
                                            </div>
                                        </div>
                                    </c:set>
                                </c:when>
                                <c:otherwise>
                                    <c:choose>
                                        <c:when test="${regularPrice < skuRegularPrice}">
                                            <c:set var="ProductPrice">
                                                <div class=cartmenupriceitem>
                                                    <div>
                                                        <mlamt:formatAmount exchangeRate="${exchangeRate}" currency ="${currency}" value="${regularPrice}" />
                                                    </div>
                                                    <span class=pricewas><mlamt:formatAmount exchangeRate="${exchangeRate}" currency ="${currency}" value="${skuRegularPrice}" /></span>
                                                </div>
                                            </c:set>
                                        </c:when>
                                        <c:otherwise>
                                            <c:set var="ProductPrice">
                                                <div class=cartmenupriceitem>
                                                    <mlamt:formatAmount exchangeRate="${exchangeRate}" currency ="${currency}" value="${regularPrice}" />
                                                </div>
                                            </c:set>
                                        </c:otherwise>
                                    </c:choose>
                                </c:otherwise>
                            </c:choose>
                        </c:when>
                        <c:otherwise>
                            <c:set var="ProductPrice" value="" scope="request" />
                        </c:otherwise>
                    </c:choose>

                    <c:choose>
                        <c:when test="${fn:length(fn:trim(basketItem.product.name)) > GlobalCartModel.maxCharForItemName}">
                            <c:set var="ProductName" scope="request">
                                ${fn:substring((fn:trim(basketItem.product.name)),0,(GlobalCartModel.maxCharForItemName-3))}
                                <fmt:message key="msg.globalCart.itemTable.productNameElipsis" />
                            </c:set>
                        </c:when>
                        <c:otherwise>
                            <c:set var="ProductName" scope="request">${fn:trim(basketItem.product.name)}</c:set>
                        </c:otherwise>
                    </c:choose>

                    <c:set var="search" value="\"" />
                    <c:set var="replace" value="\\\"" />
                    <c:set var="globalCartItemInfoCSS" value="globalCartItemInfo globalCartItemInfo1Bg" />
                    <c:if test="${itemIndex % 2 != 0}">
                        <c:set var="globalCartItemInfoCSS" value="globalCartItemInfo globalCartItemInfo2Bg" />
                    </c:if>

                    <div class="${globalCartItemInfoCSS}">
                        <div class="nameQtyAndImage">
                            <div class="itemImage">
                                <c:if test="${!empty ProductThumbnailLink}">
                                    <a href="${ProductThumbnailLink}" />
                                </c:if>
                                <c:if test="${(not GlobalCartModel.iiEnabled && basketItem.cartItemType.defaultProduct.image.thumb!=null && basketItem.cartItemType.defaultProduct.image.thumb!='')
                                    || (GlobalCartModel.iiEnabled && basketItem.cartItemType.defaultProduct.zoomPage.zoom1Image != null && basketItem.cartItemType.defaultProduct.zoomPage.zoom1Image !='')}">
                                    <c:choose>
                                        <c:when test="${GlobalCartModel.useMiniThumbnail}">
                                            <c:set var="thumbnailWidth" value="${site.layout.miniThumbWidth}" />
                                            <c:set var="thumbnailHeight" value="${site.layout.miniThumbHeight}" />
                                        </c:when>
                                        <c:otherwise>
                                            <c:set var="thumbnailWidth" value="${site.layout.thumbnailWidth}" />
                                            <c:set var="thumbnailHeight" value="${site.layout.thumbnailHeight}" />
                                        </c:otherwise>
                                    </c:choose>
                                    <mlimg:prodimg imageRoot="${catalogImages.imagePath}"
                                                   productID="${basketItem.cartItemType.defaultProduct.pk.asString}"
                                                   imageType="mini" entityType="product"
                                                   otherAttrs='width="${thumbnailWidth}"  height="${thumbnailHeight}" alt="${fn:escapeXml(basketItem.cartItemType.defaultProduct.name)}" ' />
                                    <c:if test="${!empty ProductThumbnailLink}">
                                        </a>
                                    </c:if>
                                </c:if>
                            </div>

                            <div class="itemNameAndQty">
                                <div class="name">
                                    <c:if test="${!empty ProductLink}">
                                        <a href="${ProductLink}">
                                    </c:if>
                                    ${ProductName}
                                    <c:if test="${!empty ProductLink}">
                                        </a>
                                    </c:if>
                                </div>

                                <c:if test="${GlobalCartModel.displayOptionName}">
                                    <c:set var="optionSeparator">
                                        <fmt:message key="msg.common.optionSeparator" />
                                    </c:set>
                                    <c:forEach var="option" items="${basketItem.cartItemType.optionsSorted}">
                                        <c:choose>
                                            <c:when test="${GlobalCartModel.displayOptionLabel}">
                                                <c:set var="optionName">${fn:trim(option.optionType.name)}${optionSeparator} ${option.name}</c:set>
                                            </c:when>
                                            <c:otherwise>
                                               <c:set var="optionName">${option.name}</c:set>
                                            </c:otherwise>
                                        </c:choose>
                                        <c:if test="${fn:length(optionName) > GlobalCartModel.maxCharForOptionName}">
                                            <c:set var="optionName">
                                                ${fn:substring(optionName, 0, GlobalCartModel.maxCharForOptionName-3)}
                                                <fmt:message key="msg.globalCart.itemTable.productNameElipsis" />
                                            </c:set>
                                        </c:if>
                                        <div class="option">
                                            ${optionName}<br />
                                        </div>
                                    </c:forEach>
                                </c:if>

                                <div class="qty">
                                    <fmt:message key="msg.globalCart.itemTable.qty" />
                                    ${basketItem.qty}
                                </div>
                                <c:set var="MoreItemCount" scope="request" value="${MoreItemCount - basketItem.qty}" />
                            </div>
                        </div>

                        <c:if test="${expandPane != 'lastAddedItemTable' and GlobalCartModel.displayPriceEach}">
                            <div class="priceEach">${ProductPrice}</div>
                        </c:if>
                        <c:if test="${expandPane != 'lastAddedItemTable' and GlobalCartModel.displayDiscount}">
<!--BorderFree - item discount -->
                            <c:choose>
                                <c:when test="${itemTotalDiscount.toBigDecimal() > 0}">
                                    <div class="discount">
                                        -<mlamt:formatAmount exchangeRate="${exchangeRate}" currency ="${currency}" value="${itemTotalDiscount}" />
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="discount">&nbsp;</div>
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                        <c:if test="${GlobalCartModel.displayItemPrice}">
                            <c:choose>
                                <c:when test="${basketItem.freeGift}">
                                    <div class="messagefreegift price">
                                        <fmt:message key="msg.common.freeGift" />
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="price">
                                        <!--BorderFree - item sub total: price + qty -->
                                        <mlamt:formatAmount exchangeRate="${exchangeRate}" currency ="${currency}" value="${itemSubTotal}" />
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                        <%-- Border Free Restriction check tile --%>
                        <c:if test="${borderFreeEnable}">
                        <ct:call object="${PricingModel}" method="isProductRestricted" param1="${basketItem.product.code}" return="restricted"/>
                            <tiles:insertAttribute name="borderfreerestrictioncheck">
                                <tiles:putAttribute name="restricted" value="${restricted}"/>
                            </tiles:insertAttribute>
                        </c:if>
                    </div>
                </c:forEach>

                <c:if test="${GlobalCartModel.displayCartSubtotalOnItemTable or (GlobalCartModel.displayMoreItems and MoreItemCount > 0)}">
                    <div class="globalCartTotal">

                        <c:if test="${GlobalCartModel.displayCartSubtotalOnItemTable}">
                            <c:if test="${merchandiseTotal.toBigDecimal() != basketSubTotal.toBigDecimal()}">
                                <div class="summary">
                                    <div class="title">
                                        <fmt:message key="msg.globalCart.merchandiseSubtotal" />
                                    </div>
                                    <div class="value">
                                        <!--BorderFree - merchandiseTotal -->
                                        <mlamt:formatAmount exchangeRate="${exchangeRate}"  currency ="${currency}" value="${merchandiseTotal}" />
                                    </div>
                                </div>
                            </c:if>

                            <%-- Iterate over Order Discounts --%>
                            <c:forEach var="orderDiscount" varStatus="status" items="${GlobalCartModel.basket.discounts}">
                                <c:set var="discountName" value="&nbsp;"/>
                                <c:if test="${orderDiscount.showMessage}">
                                    <c:set var="discountName" value="${orderDiscount.message}&#58;" />
                                </c:if>
                                <div class="summary">
                                    <div class="discounttitle">
                                        <c:out value="${discountName}" escapeXml="false" />
                                    </div>
                                    <c:choose>
                                    <c:when test ="${borderFreeEnable && !selectedCountryIsUS}">
                                        <c:if test ="${discountTotal > PricingModel.zero && status.last}">
                                            <div class="discountvalue discount">
                                                -<mlamt:formatAmount exchangeRate="${exchangeRate}" currency ="${currency}" value="${discountTotal}"/>
                                            </div>
                                        </c:if>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="discountvalue discount">
                                            -<mlamt:formatAmount exchangeRate="${exchangeRate}" currency ="${currency}" value="${orderDiscount.amount}"/>
                                        </div>
                                    </c:otherwise>
                                    </c:choose>
                                </div>
                            </c:forEach>

                            <div class="summary">
                                <c:if test="${expandPane != 'lastAddedItemTable'}">
                                    <c:if test="${MoreItemCount == 1}">
                                        <div class="globalCartMoreItems">
                                            <a href="${basketURL}">
                                                <fmt:message key="msg.globalCart.itemTable.moreItem">
                                                    <fmt:param value="${MoreItemCount}" />
                                                </fmt:message></a>
                                        </div>
                                    </c:if>
                                    <c:if test="${MoreItemCount > 1}">
                                        <div class="globalCartMoreItems">
                                            <a href="${basketURL}">
                                            <fmt:message key="msg.globalCart.itemTable.moreItems">
                                                <fmt:param value="${MoreItemCount}" />
                                            </fmt:message></a>
                                        </div>
                                    </c:if>
                                </c:if>

                                <div class="title-subtotal">
                                    <fmt:message key="msg.globalCart.subtotal" />
                                </div>
                                <div class="value">
                                    <mlamt:formatAmount exchangeRate="${exchangeRate}" currency ="${currency}" value="${basketSubTotal}" />
                                </div>
                                
                               <div id="affirmMonthlyPaymentPriceBasketGlobalCartCartSummaryMessage" style="float:right"> 
		                            <c:if test="${MonthlyPaymentPriceModel.attributeMap['isAffirmMonthlyPaymentGlobalCartEnabled'] && !borderFreeEnabledAndNotUSSelected}">
			                           <tiles:insertAttribute name="affirmMonthlyPaymentPriceBasketGlobalCartCartSummaryMessage">
											<tiles:putAttribute name="total" value="${basketSubTotal}"/>
										</tiles:insertAttribute>
		                            </c:if>
	                           </div> 
	                           
                            </div>
                        </c:if>
                    </div>
                </c:if>

                <div class="viewBasketAndCheckout">
                    <div class="globalCartViewBasketBtn">
                        <a href="${basketURL}">
                            <div class="ml-globalcart-button">
                                <fmt:message key="btn.globalCart.viewBasket" />
                            </div>
                        </a>
                    </div>
                    <c:if test="${GlobalCartModel.saveCartEnabled}">
                        <div class="globalCartSaveCart">
                            <div class="ml-globalcart-button">
                                <c:url var="dataUrl" value="/checkout/literegistration.do"><c:param name="type" value="sb"/></c:url>
                                <div class="ml-save-cart" data-from="Shopping Bag Flyout" data-url='${dataUrl}'>
                                    <fmt:message key="btn.globalCart.saveCart" />
                                </div>
                            </div>
                        </div>
                    </c:if>
                    <form id="globalCartCheckoutForm" action="/basket.do?method=checkout" method="post">
                        <c:forEach var="basketItem" varStatus="status" end="${maxCartItemCount-1}" items="${basketItems}">
                            <input type="hidden" name="itemPk" value="${basketItem.pk.asString}" />
                            <input type="hidden" name="qty" id="qty_${basketItem.pk.asString}" value="${basketItem.qty}" />
                            <input type="hidden" name="option" id="optionTypeValues_${basketItem.pk.asString}"
                                   value="<c:forEach var="option" varStatus="status" items="${basketItem.cartItemType.options}">${option.optionType.pk.asString}=${option.pk.asString}<c:if test="${!status.last}">:</c:if></c:forEach><c:if test="${empty basketItem.cartItemType.options}">none</c:if>" />
                        </c:forEach>
                        <div class="globalCartCheckoutBtn">
                            <button type="button" name="Checkout" class="ml-primary-button" onclick="MarketLive.P2P.Basket.basketCheckout('globalCartCheckoutForm');"><fmt:message key="btn.altTxt.checkout"/></button>
                        </div>
                    </form>

                </div>

                <div class="globalcart-carousel-wrapper">
                    <tiles:insertAttribute name="globalcartCarousel"/>
                </div>
            </c:otherwise>
        </c:choose>
        </c:if> <%-- End if (expandOnSummaryHoverAction or expandOnLoadAction) --%>
        <div class="globalcart-carousel-wrapper">
            <%--  (8) Fill Slot --%>
            <div class="globalCartLastItemAddedFillSlot ml-slot-item">
                <c:if test="${GlobalCartModel.totalQty == 0}">
                    <tiles:insertDefinition name=".display.nestedtemplate">
                        <tiles:putAttribute name="itemID" value="13" />
                        <tiles:putAttribute name="itemType" value="KICKERS" />
                        <tiles:putAttribute name="parameters" value="page=EMPTY_GLOBAL_CART" />
                        <c:set var="abTestId" value="basket" scope="request" />
                        <c:set var="uriSpecific" value="false" scope="request" />
                        <c:set var="additionalDescription" value=" (Page Level)" scope="request" />
                    </tiles:insertDefinition>
                </c:if>
                <c:if test="${GlobalCartModel.totalQty > 0}">
                    <tiles:insertDefinition name=".display.nestedtemplate">
                        <tiles:putAttribute name="itemID" value="13" />
                        <tiles:putAttribute name="itemType" value="KICKERS" />
                        <tiles:putAttribute name="parameters" value="page=GLOBAL_CART" />
                        <c:set var="abTestId" value="basket" scope="request" />
                        <c:set var="uriSpecific" value="false" scope="request" />
                        <c:set var="additionalDescription" value=" (Page Level)" scope="request" />
                    </tiles:insertDefinition>
                </c:if>
            </div>
        </div>
        </div> <%-- End Bootstrap popover content --%>
    </div>
</div>