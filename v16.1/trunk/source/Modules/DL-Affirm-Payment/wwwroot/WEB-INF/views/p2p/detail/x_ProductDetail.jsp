<%@ include file="/WEB-INF/views/common/TagLibs.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/ml-social.tld" prefix="mlsocial" %>

<%----------------------------------------------------------------------------------------------------------------------
  -    Description: The Product Detail page shows descriptive and pricing information about a Product or Product Family.
  -  Storyboard ID: P2P0040, P2P0045
  - Pre-conditions:
  -     Model/Form: com.marketlive.app.b2c.p2p.detail.DetailModel, com.marketlive.app.b2c.p2p.basket.AddToBasketForm
  - URLs Posted To: /addToBasket
  -   Current Tile: .p2p.detail.tile
  - Tile Variables:
  -  Child Tile(s): productsetbacknext, itemtable, crosssells
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

<tilesx:useAttribute id="configurationPath" name="configurationPath"/>

<%-- For client side validation --%>
<c:set var="noQtyMsg"><fmt:message key="err.common.noQuantityEntered" /></c:set>
<c:set var="noOptionsMsg"><fmt:message key="err.common.noOptionsSelected" /></c:set>

    <%-- Previous / Next Product Navigation Tile --%>
        <tiles:insertAttribute name="productnavigation" flush="false">
            <tiles:putAttribute name="feature" value="DETAIL_PRODUCT_NAVIGATION" />
        </tiles:insertAttribute>

<div class="ml-product-wrapper">

<span itemscope itemtype="http://schema.org/Product">

     <form:form method="post" modelAttribute="addToBasketForm" id="mainForm"
               action="${mlnav:pathRef('/addToBasket.do', pageContext)}" onsubmit="document.getElementById('mainForm').pageCategory.value=MarketLive.Base.getVariableValue('eVar4');return MarketLive.P2P.validateProductSelection2('${mlnav:escapeJavaScript(noQtyMsg)}', '${mlnav:escapeJavaScript(noOptionsMsg)}', this, 1, true);">
        <spring:hasBindErrors name="addToBasketForm">
            <div class="alert alert-warning alert-dismissable">
                <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
                <c:forEach var="error" items="${errors.allErrors}">
                    <spring:message code="${error.objectName}" arguments="${error.defaultMessage}" />
                </c:forEach>
            </div>
        </spring:hasBindErrors>

        <input type="hidden" name="from" value="detail" />

        <%-- Hidden field that will be set to the value of the "page category" variable,
             for Omniture reporting, when the "Add to Basket" button is clicked. --%>
        <input type="hidden" name="pageCategory" value="" />

        <%-- Product Detail --%>
        <div class="ml-product-detail">
            <div class="ml-product-detail-wrapper">
                <%-- Product Image and Swatches --%>
                <c:choose>
                    <c:when test="${ProductDetailModel.intelligentImagingEnabled}">
                        <c:choose>
                            <c:when test="${ProductDetailModel.hasAltImages}">
                                <div class="ml-product-alt-detail-image">
                            </c:when>
                            <c:otherwise>
                                <div class="ml-product-detail-image">
                            </c:otherwise>
                        </c:choose>
                    </c:when>
                    <c:otherwise>
                        <c:choose>
                            <c:when test="${ProductDetailModel.hasAltImages}">
                                <div class="ml-product-alt-detail-image">
                            </c:when>
                            <c:otherwise>
                                <div class="ml-product-detail-image">
                            </c:otherwise>
                </c:choose>
                    </c:otherwise>
                </c:choose>

                    <div>
                        <c:choose>
                            <c:when test="${ProductDetailModel.intelligentImagingEnabled}">
                                    <tiles:insertAttribute name="detailImageSwatchViews">
                                        <tiles:putAttribute name="configurationPath" value="${configurationPath}" />
                                    </tiles:insertAttribute>

                            </c:when>
                            <c:otherwise>
                                     <tiles:insertAttribute name="detailImageSwatches">
                                      <tiles:putAttribute name="configurationPath" value="${configurationPath}" />
                                  </tiles:insertAttribute>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <%-- Social Links --%>
                    <div class="ml-product-social-links">
                        <tiles:insertAttribute name="socialButtons" />
                    </div>
                </div>

                <%-- Product Detail Information --%>
                <c:choose>
                    <c:when test="${ProductDetailModel.intelligentImagingEnabled}">
                        <c:choose>
                            <c:when test="${ProductDetailModel.hasAltImages}">
                                <div class="ml-product-alt-detail-info">
                            </c:when>
                            <c:otherwise>
                                <div class="ml-product-detail-info">
                            </c:otherwise>
                        </c:choose>
                    </c:when>
                    <c:otherwise>
                        <c:choose>
                            <c:when test="${ProductDetailModel.hasAltImages}">
                                <div class="ml-product-alt-detail-info">
                            </c:when>
                            <c:otherwise>
                                <div class="ml-product-detail-info">
                            </c:otherwise>
                </c:choose>
                    </c:otherwise>
                </c:choose>


                    <%-- Product Name --%>
                    <c:if test="${ProductDetailModel.productNameShown}">
                        <div class="ml-product-name"><div itemprop="name"><mlabtest:element uniqueIdentifier="detail_productName" description="Product Name"><c:out value="${ProductDetailModel.categoryItem.name}" escapeXml="false" /></mlabtest:element></div></div>
                    </c:if>

                    <%-- Product Code --%>
                    <div class="ml-product-code"><span class="ml-product-code-header"><fmt:message key="hdr.itemTable.itemNumber" /></span>  ${ProductDetailModel.product.code} </div>



                    <div class="ml-product-reviews">
                    <%-- Product Rating --%>
                    <c:if test="${ProductDetailModel.productRatingShown}">
                        <div class="ml-product-rating">
                            <tiles:insertAttribute name="rating">
                                <tiles:putAttribute name="product" value="${ProductDetailModel.product}" />
                                <tiles:putAttribute name="ratingStarsSize" value="large" />
                                <tiles:putAttribute name="showRatingValue" value="${ProductDetailModel.showRatingValue}" />
                                <tiles:putAttribute name="showRatingText" value="${ProductDetailModel.showRatingText}" />
                            </tiles:insertAttribute>
                        </div>
                    </c:if>

                    <%-- Customer Reviews Link --%>
                    <c:if test="${ProductDetailModel.productReviewShown}">
                        <div class="ml-product-rating-links">
                            <c:choose>
                                <c:when test="${ProductDetailModel.customerReviewCount > 0}">
                                    <a href="#customerReviews" id="reviews"><fmt:message key="lnk.customerReviews.readNumReviews"><fmt:param value="${ProductDetailModel.customerReviewCount}"/></fmt:message></a>
                                </c:when>
                                <c:otherwise>
                                    <a href="#customerReviews" id="reviews"><fmt:message key="lnk.customerReviews.readReviews"/></a>
                                </c:otherwise>
                            </c:choose>
                            <span>|</span>
                            <a href="${mlnav:entityRefAddPath(ProductDetailModel.product, 'ReviewSubmit', pageContext)}"><fmt:message key="lnk.customerReviews.writeAReview" /></a>
                        </div>
                    </c:if>
                    </div>

                    <%-- Product Short Description --%>
                    <c:if test="${ProductDetailModel.productShortDescShown}">
                        <div class="ml-product-desc-short"><mlabtest:element uniqueIdentifier="detail_productShortDesc" description="Product Short Description"><c:out value="${ProductDetailModel.categoryItem.description.short}" escapeXml="false" /></mlabtest:element></div>
                    </c:if>

                    <%-- Product Long Description --%>
                    <c:if test="${ProductDetailModel.productLongDescShown}">
                        <div class="ml-product-desc-long"><div itemprop="description"><mlabtest:element uniqueIdentifier="detail_productLongDesc" description="Product Long Description"><c:out value="${ProductDetailModel.categoryItem.description.long}" escapeXml="false" /></mlabtest:element></div></div>
                    </c:if>

                    <%-- More Info --%>
                    <c:if test="${!empty ProductDetailModel.product.description.moreInfo}">
                        <div class="ml-product-desc-more">
                            <c:url var="modalURL" value="${mlnav:entityRefAddPath(ProductDetailModel.product, 'MoreInfo', pageContext)}" />
                            <fmt:message var="modalLinkText"  key="img.altTxt.moreInfo" />
                            <ml:modal id="moreInfoModal" url="${modalURL}" text='${modalLinkText}' />
                        </div>
                    </c:if>

                    <%-- Item Table Tile --%>
                    <c:if test="${!ProductDetailModel.family}">
                    <div class="ml-product-item"><tiles:insertAttribute name="itemtable" /></div>
                    </c:if>

					<%-- Affirm Monthly Payment Price --%>
					<tiles:insertAttribute name="affirmMonthlyPaymentPriceMessage">
						<tiles:putAttribute name="categoryItem" value="${ProductDetailModel.categoryItem}"/>
					</tiles:insertAttribute>

					
                    <%-- Border Free Restriction check tile --%>
                    <c:set var="borderFreeEnable" value="${PricingModel.borderFreeIsEnable}" />
                    <c:if test="${borderFreeEnable}">
                        <ct:call object="${PricingModel}" method="isProductRestricted" param1="${ProductDetailModel.product.code}" return="restricted"/>
                        <tiles:insertAttribute name="borderfreerestrictioncheck">
                            <tiles:putAttribute name="restricted" value="${restricted}"/>
                        </tiles:insertAttribute>
                    </c:if>

                    <%-- Button(s) --%>
                        <%-- Add To Basket --%>
                        <c:if test="${ProductDetailModel.hasSkus}" >
						<c:set var="addToBasket" value="/addToBasket.do"/>
                            <div class="ml-product-btn-primary">
                                <mlabtest:element uniqueIdentifier="detail_addToBasketButton" description="Add To Basket Button (Page Level)" uriSpecific="false">
                                    <input type="submit" name="addToBasket" class="ml-button-add-basket" onclick="document.getElementById('mainForm').action='${mlnav:pathRefAmpEscape(addToBasket, pageContext)}'" value="<fmt:message key='btn.altTxt.addToBasket' />">
                                </mlabtest:element>
                            </div>
                        </c:if>

                        <div class="ml-product-btn-secondary">
                            <%-- Add To WishList --%>
                            <c:if test="${ProductDetailModel.wishListShown && ProductDetailModel.hasSkus}" >
                                <c:set var="wishlistSelect" value="/wishlist/select/additem.do?method=execute&id=${ProductDetailModel.product.pk.asString}"/>
                                <span class="ml-product-btn-wish-list">
                                    <span class="ml-icon-lib ml-icon-heart ml-tell-friend-icon"></span>
                                    <mlabtest:element uniqueIdentifier="detail_addToWishListButton" description="Add To WishList Button (Page Level)" uriSpecific="false">
                                        <button type="submit" name="addtoWishlist" class="ml-product-wish-list" onclick="document.getElementById('mainForm').action='${mlnav:pathRefAmpEscape(wishlistSelect, pageContext)}'"> <fmt:message key="btn.altTxt.addToWishlist"/></button>
                                    </mlabtest:element>
                                </span>
                            </c:if>

                            <%-- Tell A Friend --%>
                            <c:if test="${ProductDetailModel.tellAFriendShown}">
                                <span class="ml-product-btn-tell-friend">
                                    <span class="ml-icon-lib ml-icon-star ml-tell-friend-icon"></span>
                                    <a id="taf" href="<c:url value="${mlnav:entityRefAddPath(ProductDetailModel.product, 'TellAFriendSubmit', pageContext)}" />"><span class="ml-product-tell-friend"><fmt:message key="img.altTxt.tellAFriend" /></span></a>
                                </span>
                            </c:if>
                        </div>


                </div>
            </div>

        <%-- Item Table Tile --%>
        <c:if test="${ProductDetailModel.family}">
            <div class="ml-product-item-family"><tiles:insertAttribute name="itemtable" /></div>
        </c:if>


        <%-- Fillslot --%>
        <div class="ml-product-fillslot-container"><ml:fillslot index="1" /></div>

</form:form>

        <%-- Expanded Lower Content (Recently Viewed, Info Tabs, and Cross Sells) --%>
        <c:choose>
            <c:when test="${ProductDetailModel.expandLowerContent}">
                <%-- Info Tabs --%>
                <div class="ml-product-info-tabs"><tiles:insertAttribute name="infoTabs"/></div>

                <%-- Recently Viewed --%>
                    <c:if test="${ProductDetailModel.displayRecentlyViewed}">
                        <div class="ml-product-recent-view">
                            <tiles:insertAttribute name="recentlyViewedCarousel"/>
                        </div>
                    </c:if>

                    <%-- Begin My Buys, MySite Recommendation Carousel --%>
                    <c:if test="${ProductDetailModel.showRecommendation}">
                        <ml:recommendation recommendationPageType="PRODUCT_DETAILS" recommendationProvider="MyBuys"/>
                    </c:if>
                    <%-- End My Buys, MySite Recommendation Carousel --%>

                    <%-- Cross Sells --%>
                    <c:if test="${ProductDetailModel.hasCrossSells}">
                        <div class="ml-product-cross-sells">
                            <tiles:insertAttribute name="crossSellsCarousel"/>
                        </div>
                    </c:if>

            </c:when>
            <c:otherwise>
                <%-- Info Tabs --%>
                <div class="ml-product-info-tabs"><tiles:insertAttribute name="infoTabs"/></div>

                <%-- Begin My Buys, MySite Recommendation Carousel --%>
                <c:if test="${ProductDetailModel.showRecommendation}">
                        <ml:recommendation recommendationPageType="PRODUCT_DETAILS" recommendationProvider="MyBuys"/>
                </c:if>
                <%-- End My Buys, MySite Recommendation Carousel --%>

                <%-- Cross Sells --%>
                <c:if test="${ProductDetailModel.hasCrossSells}">
                    <div class="ml-product-cross-sells">
                        <tiles:insertAttribute name="crossSellsCarousel"/>
                    </div>
                </c:if>
            </c:otherwise>
        </c:choose>

        <%-- Customer Reviews Detail --%>
        <c:if test="${ProductDetailModel.productReviewShown}">
            <div id="customerReviews" name="customerReviews" class="ml-product-customer-reviews-wrapper">
                <tiles:insertAttribute name="customerReviews"/>
            </div>
        </c:if>

</span>
</div>
<%-- Back to Top --%>
<div class="ml-product-backto-top"><a href="#top"><span class="ml-icon-lib ml-icon-up"></span> <fmt:message key="lnk.backToTop"/></a></div>

<div id="productData" style="display:none;" data-product-id="${ProductDetailModel.categoryItem.pk}" data-product-name="${fn:escapeXml(ProductDetailModel.categoryItem.name)}"></div>

<%-- Affirm Monthly Payment Price --%>
<tiles:insertAttribute name="affirmMonthlyPaymentPrice"/>

<mlperf:script defer="true">MarketLive.P2P.initializeProductDetailTabs(); MarketLive.P2P.onDetailReady();</mlperf:script>
<mlperf:script defer="true">MarketLive.P2P.onEwbisReady(${jsEwbisAlert});</mlperf:script>
<%-- BlockUI when submitting form --%>
<mlperf:script defer="true" type="text/javascript">MarketLive.Base.blockUIWhen('#mainForm', 'submit');</mlperf:script>
<mlperf:script defer="true" type="text/javascript">

     <jsp:useBean id="pdpJsonObject" class="org.json.simple.JSONObject" />
     <c:set var="pdpDetailImgMap" value="" />
     <c:set var="pdpZoomImgMap" value="" />
     <c:set var="pdpSwatchImgMap" value="" />
     <c:set var="pdpAdditionalViewImgMap" value="" />

     <fmt:message key="lbl.detailImageSwatches.shownIn" var="pdpTempShownIn"/>

     <ct:call object="${pdpJsonObject}" method="toJSONString" param="${DetailImageSwatchViewModel.detailImgMap}" return="pdpDetailImgMap"/>
     <ct:call object="${pdpJsonObject}" method="toJSONString" param="${DetailImageSwatchViewModel.zoomImgMap}" return="pdpZoomImgMap"/>
     <ct:call object="${pdpJsonObject}" method="toJSONString" param="${DetailImageSwatchViewModel.swatchImgMap}" return="pdpSwatchImgMap"/>
     <ct:call object="${pdpJsonObject}" method="toJSONString" param="${DetailImageSwatchViewModel.additionalViewImgMap}" return="pdpAdditionalViewImgMap"/>

     <c:if test="${ProductDetailModel.intelligentImagingEnabled}">
          var objPDPDetailImageSwatchView = new MarketLive.IntelligentImaging.DetailImageSwatchView(
            '${DetailImageSwatchViewModel.productURL}'  // iiServerPath
            , '${DetailImageSwatchViewModel.imageBase}' // imageBase
            , '${DetailImageSwatchViewModel.imageExt}' // imageExt
            , '${DetailImageSwatchViewModel.widTag}' // widTag
            , '${DetailImageSwatchViewModel.imageEnd}' // imageEnd
            , '${DetailImageSwatchViewModel.currentOptionCode}' // currentOptionCode
            , '${DetailImageSwatchViewModel.currentView}' // currentView
            , '${DetailImageSwatchViewModel.detailWid}' // detailWid
            , '${DetailImageSwatchViewModel.zoomWid}' // zoomWid
            , '${DetailImageSwatchViewModel.viewWid}' // viewWid
            , '${DetailImageSwatchViewModel.swatchWid}'
            , '${DetailImageSwatchViewModel.product.pk.asString}'
            , '${DetailImageSwatchViewModel.optionTypeVO.optionType.pk.asString}'
            , '${pdpTempShownIn}'
            , "${DetailImageSwatchViewModel.product.name}"
            , ${pdpDetailImgMap}
            , ${pdpZoomImgMap}
            , ${pdpSwatchImgMap}
            , ${pdpAdditionalViewImgMap});

            var objPDPMainImgPath = jQuery('#mainimage').attr('src');
            var objPDPZoomImgPath =  jQuery('#zoom1').attr('href');

     </c:if>
</mlperf:script>
