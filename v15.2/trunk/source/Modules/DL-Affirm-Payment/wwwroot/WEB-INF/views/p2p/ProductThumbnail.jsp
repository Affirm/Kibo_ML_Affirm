<%@ include file="/WEB-INF/views/common/TagLibs.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/ml-social.tld" prefix="mlsocial" %>

<%----------------------------------------------------------------------------------------------------------------------
-    Description: Displays a categoryitem (i.e. product/family/topic) thumbnail along with its name/shortDescription/price, based on the config.
-  Storyboard ID: P2P0033, P2P0034
- Pre-conditions: needs to be passed a categoryItem from the parent tile.
-     Model/Form: com.marketlive.app.b2c.p2p.ThumbnailModel
- URLs Posted To:
-   Current Tile: .tile.p2p.thumbnail
- Tile Variables: categoryItem (ICategoryItem)
-  Child Tile(s): pricing
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
<tiles:importAttribute name="categoryItem" />
<tiles:importAttribute name="useMiniThumb" ignore="true" />
<tiles:importAttribute name="showName" ignore="true" />
<tiles:importAttribute name="wrapShowName" ignore="true" />
<tiles:importAttribute name="truncateShowName"  ignore="true" />
<tiles:importAttribute name="showShortDescription" ignore="true" />
<tiles:importAttribute name="wrapShortDescription" ignore="true" />
<tiles:importAttribute name="truncateShortDescription" ignore="true" />
<tiles:importAttribute name="showLongDescriptionForDirectoryListView" ignore="true" />
<tiles:importAttribute name="showPrices" ignore="true" />
<tiles:importAttribute name="showSwatches" ignore="true" />
<tiles:importAttribute name="showQuickView" ignore="true" />
<tiles:importAttribute name="showRating" ignore="true" />
<tiles:importAttribute name="showFBLike" ignore="true" />
<tiles:importAttribute name="showFacePile" ignore="true" />
<tiles:importAttribute name="deferLoadingThumbnail" ignore="true" />
<tiles:importAttribute name="showBadge" ignore="true" />
<tiles:importAttribute name="imageWidth" ignore="true" />
<tiles:importAttribute name="imageHeight" ignore="true" />
<tiles:importAttribute name="onselect" ignore="true" />
<tiles:importAttribute name="carouselParam" ignore="true" />
<tiles:importAttribute name="fromSearchCarouselParam" ignore="true" />
<tiles:importAttribute name="fromParam" ignore="true" />
<tiles:importAttribute name="itemIndex" ignore="true" />

<jsp:useBean id="affirmWebUtil" class="com.deplabs.affirm.app.b2c.AffirmWebUtil"/>
<ct:call object="${affirmWebUtil}" method="isBorderFreeEnabledAndNotUSSelected"  fieldNameParam="${pageContext.request}" return="borderFreeEnabledAndNotUSSelected" />


<c:set var="categoryItemURL" scope="request"><c:url value="${mlnav:entityRef(categoryItem, pageContext)}"/></c:set>

<c:if test="${not empty refTypeQueryString}">
    <ct:call object="${site.reportingModel}" method="addTrackingToURL" originalURLParam="${categoryItemURL}" queryStringParams="${refTypeQueryString}" return="categoryItemURL"/>
</c:if>

<c:if test="${empty showName}" >
    <c:set var="showName" value="${ThumbnailModel.showName}" scope="page"  />
</c:if>
<c:if test="${empty showShortDescription}" >
    <c:set var="showShortDescription" value="${ThumbnailModel.showShortDescription}" scope="page"  />
</c:if>
<c:if test="${empty showLongDescriptionForDirectoryListView}" >
    <c:set var="showLongDescriptionForDirectoryListView" value="${ThumbnailModel.showLongDescriptionForDirectoryListView}" scope="page"  />
</c:if>
<c:if test="${empty showPrices}" >
    <c:set var="showPrices" value="${ThumbnailModel.showPrices}" scope="page"  />
</c:if>
<c:if test="${empty showSwatches}" >
    <c:set var="showSwatches" value="${ThumbnailModel.showSwatches}" scope="page"  />
</c:if>
<c:if test="${empty showRating}" >
    <c:set var="showRating" value="${ThumbnailModel.showRating}" scope="page"  />
</c:if>
<c:if test="${empty showFBLike}" >
    <c:set var="showFBLike" value="${ThumbnailModel.showFBLike}" scope="page"  />
</c:if>
<c:if test="${empty showFacePile}" >
    <c:set var="showFacePile" value="${ThumbnailModel.showFacePile}" scope="page"  />
</c:if>
<c:if test="${empty deferLoadingThumbnail}" >
    <c:set var="deferLoadingThumbnail" value="${ThumbnailModel.deferLoadingThumbnail}" scope="page"  />
</c:if>
<c:if test="${empty showBadge}" >
    <c:set var="showBadge" value="${ThumbnailModel.showBadge}" scope="page"  />
</c:if>
<c:if test="${empty onselect}" >
    <c:set var="onselect" value="false" scope="page"  />
</c:if>
<c:if test="${!empty truncateShowName}">
    <c:set var="truncateCategoryItemName" value="${truncateShowName}" scope="page"  />
</c:if>
<c:if test="${!empty truncateShortDescription}">
    <c:set var="truncateShortDescription" value="${truncateShortDescription}" scope="page"  />
</c:if>
<c:if test="${!empty carouselParam}">
    <c:set var="carouselParam" value="${carouselParam}" scope="page"  />
</c:if>
<c:if test="${!empty fromSearchCarouselParam}">
    <c:set var="fromParam" value="${fromSearchCarouselParam}" scope="page"  />
</c:if>
<%-- <c:out value="productThumbnail" /> --%>
<%-- <c:out value="${fromParam}" /> --%>
<c:if test="${!empty fromParam}">
    <c:choose>
        <c:when test="${fn:contains(categoryItemURL, '?')}"><c:set var="categoryItemURL" value="${categoryItemURL}&amp;" /></c:when>
        <c:otherwise><c:set var="categoryItemURL" value="${categoryItemURL}?" /></c:otherwise>
    </c:choose>
    <c:set var="categoryItemURL" value="${categoryItemURL}${fromParam}" />
</c:if>

<c:if test="${!empty carouselParam}">
    <c:choose>
        <c:when test="${fn:contains(categoryItemURL, '?')}"><c:set var="categoryItemURL" value="${categoryItemURL}&amp;" /></c:when>
        <c:otherwise><c:set var="categoryItemURL" value="${categoryItemURL}?" /></c:otherwise>
    </c:choose>
    <c:set var="categoryItemURL" value="${categoryItemURL}${carouselParam}" />
</c:if>
<%-- <c:out value="${categoryItemURL}" /> --%>

<%-- Early executing swatches tile BEFORE displaying thumbnail image to use its model properties when determining thumbnail image src --%>
<c:if test="${showSwatches}">
    <c:set var="swatchesHtml">
        <tiles:insertAttribute name="swatches" flush="false">
            <tiles:putAttribute name="categoryItem" value="${categoryItem}"/>
            <tiles:putAttribute name="useMiniThumb" value="${useMiniThumb}" />
            <c:set var="pageContext" value="${pageContext}" scope="request" />
            <tiles:putAttribute name="pageContext" value="${pageContext}" />
        </tiles:insertAttribute>
    </c:set>
</c:if>
<div class="ml-grid-item-image ml-thumb-item <c:if test='${onselect}'> imgdiv-carousel-selected</c:if>">
    <div class="ml-thumb-image-container">
    <c:choose>
        <c:when test="${useMiniThumb}">
            <div class="ml-thumb-image-mini"><a href="${mlurl:replaceAllUriAmpersands(categoryItemURL)}"><mlimg:prodimg src="${showSwatches and not empty ThumbnailSwatchesModel.thumbImages and not ThumbnailSwatchesModel.useDefaultThumb ? ThumbnailSwatchesModel.thumbImages[0] : ''}" imageRoot="${catalogImages.imagePath}" productID="${categoryItem.pk.asString}" imageType="mini" entityType="${categoryItem.entityType}" deferLoading="${deferLoadingThumbnail}" deferredPlaceholder="${siteImages.imagePath}global/globalgraphics/deferred_placeholder.gif" otherAttrs='alt="${fn:escapeXml(categoryItem.name)}" '/></a></div>
        </c:when>
        <c:otherwise>
            <div class="ml-thumb-image"><a href="${mlurl:replaceAllUriAmpersands(categoryItemURL)}"><mlimg:prodimg src="${showSwatches and not empty ThumbnailSwatchesModel.thumbImages and not ThumbnailSwatchesModel.useDefaultThumb ? ThumbnailSwatchesModel.thumbImages[0] : ''}" imageRoot="${catalogImages.imagePath}" productID="${categoryItem.pk.asString}" imageType="thumb" entityType="${categoryItem.entityType}" deferLoading="${deferLoadingThumbnail}" deferredPlaceholder="${siteImages.imagePath}global/globalgraphics/deferred_placeholder.gif" otherAttrs='alt="${fn:escapeXml(categoryItem.name)}" '/></a></div>
        </c:otherwise>
    </c:choose>
    </div>
    <c:if test="${showSwatches}">
        <%-- Use generated swatches HTML which was generated by calling swatches tile above --%>
        ${swatchesHtml}
    </c:if>
    <c:if test="${!empty categoryItem.image.thumbBadge && !useMiniThumb && showBadge}"><div class="ml-thumb-badge"><a href="<c:url value='${mlurl:replaceAllUriAmpersands(categoryItemURL)}' />"><img src="${catalogImages.imagePath}${mlurl:replaceAllUriAmpersands(categoryItem.image.thumbBadge)}" /></a></div></c:if>
</div>

<c:if test="${showName || showShortDescription || showPrices || showRating || showFBLike}">
    <div class="ml-grid-item-info ml-thumb-info">
         <%-- Category item name --%>
        <c:if test='${onselect}'><c:set var="carouselSelected" value="name-carousel-selected" /></c:if>
        <c:if test="${showName}">
            <div class="ml-thumb-name ${carouselSelected} ${carouselWordwrap}">
                <a href="<c:url value='${mlurl:replaceAllUriAmpersands(categoryItemURL)}' />">
                    <c:choose>
                        <c:when test="${truncateCategoryItemName > 0 && fn:length(categoryItem.name) >= truncateCategoryItemName}">
                            <c:out value="${fn:substring(categoryItem.name,0,truncateCategoryItemName)}" escapeXml="false"/>...
                        </c:when>
                        <c:otherwise>
                            <c:out value="${categoryItem.name}" escapeXml="false" />
                        </c:otherwise>
                    </c:choose>
                </a>
            </div>
        </c:if>
        <%-- Short Description --%>
        <c:if test="${showShortDescription}">
            <c:set var="carouselWordwrap" value=""/>

            <div class="ml-thumb-desc-short ${carouselSelected} ${carouselWordwrap}">
                 <a href="<c:url value='${mlurl:replaceAllUriAmpersands(categoryItemURL)}' />">
                      <c:choose>
                           <c:when test="${truncateShortDescription > 0 && fn:length(categoryItem.description.short) >= truncateShortDescription}">
                               <!--Check for html open tag < -->
                               <c:choose>
                                    <c:when test="${fn:indexOf(categoryItem.description.short, '<') != -1}">
                                         <c:set var="countShortDescriptionLength" value="0" scope="page"/>
                                         <!--stringArrayShortDescription1 split open tag character < -->
                                         <c:set var="stringArrayShortDescription1"
                                                value="${fn:split(categoryItem.description.short,'<')}" scope="page"/>
                                         <c:forEach var="str1" items="${stringArrayShortDescription1}">
                                             <!--Check for html end tag > -->
                                             <c:choose>
                                                 <c:when test="${fn:indexOf(str1, '>') != -1}">
                                                     <!--stringArrayShortDescription2 split end tag character > -->
                                                     <c:set var="stringArrayShortDescription2"
                                                            value="${fn:split(str1,'>')}" scope="page"/>
                                                     <c:forEach var="str2" items="${stringArrayShortDescription2}"
                                                                varStatus="status">
                                                         <!--first part should be tag -->
                                                         <c:if test="${status.index==0}">
                                                             <c:out value="<${str2}>" escapeXml="false"/>
                                                         </c:if>
                                                         <!--second part should be data -->
                                                         <c:if test="${status.index==1}">
                                                             <!--check: data is over truncated required yet -->
                                                             <c:choose>
                                                                 <c:when test="${fn:length(str2) + countShortDescriptionLength <= truncateShortDescription}">
                                                                     <c:out value="${str2}" escapeXml="false"/>
                                                                     <c:set var="countShortDescriptionLength"
                                                                            value="${countShortDescriptionLength + fn:length(str2)}"
                                                                            scope="page"/>
                                                                 </c:when>
                                                                 <c:otherwise>
                                                                     <c:if test="${truncateShortDescription > countShortDescriptionLength}">
                                                                         <c:out value="${fn:substring(str2,0,truncateShortDescription - countShortDescriptionLength)}"
                                                                                escapeXml="false"/>...
                                                                         <c:set var="countShortDescriptionLength"
                                                                                value="${truncateShortDescription}"
                                                                                scope="page"/>
                                                                     </c:if>
                                                                 </c:otherwise>
                                                             </c:choose>
                                                         </c:if>
                                                     </c:forEach>
                                                     <c:remove var="stringArrayShortDescription2" scope="page"/>
                                                 </c:when>
                                                 <c:otherwise>
                                                     <!--check: data is over truncated required yet -->
                                                     <c:choose>
                                                         <c:when test="${fn:length(str1) + countShortDescriptionLength <= truncateShortDescription}">
                                                             <c:out value="${str1}" escapeXml="false"/>
                                                             <c:set var="countShortDescriptionLength"
                                                                    value="${countShortDescriptionLength + fn:length(str1)}"
                                                                    scope="page"/>
                                                         </c:when>
                                                         <c:otherwise>
                                                             <c:if test="${truncateShortDescription > countShortDescriptionLength}">
                                                                 <c:out value="${fn:substring(str1,0,truncateShortDescription - countShortDescriptionLength)}"
                                                                        escapeXml="false"/>...
                                                                 <c:set var="countShortDescriptionLength"
                                                                        value="${truncateShortDescription}"
                                                                        scope="page"/>
                                                             </c:if>
                                                         </c:otherwise>
                                                     </c:choose>
                                                 </c:otherwise>
                                             </c:choose>
                                         </c:forEach>
                                         <c:remove var="countShortDescriptionLength" scope="page"/>
                                         <c:remove var="stringArrayShortDescription1" scope="page"/>
                                    </c:when>
                                    <c:otherwise>
                                         <c:out value="${fn:substring(categoryItem.description.short,0,truncateShortDescription)}"
                                                escapeXml="false"/>...
                                    </c:otherwise>
                               </c:choose>
                            </c:when>
                           <c:otherwise>
                               <c:out value="${categoryItem.description.short}" escapeXml="false"/>
                           </c:otherwise>
                      </c:choose>
                 </a>
            </div>
        </c:if>
        <%-- Prices --%>
        <c:if test="${showPrices}">
            <c:if test='${onselect}'><c:set var="carouselPriceSelected">price-carousel-selected</c:set></c:if>
            <div class="ml-thumb-price ${carouselPriceSelected}">
                <c:set var="thumbnail" value="true" scope="request"/>
                <tiles:insertAttribute name="pricing">
                    <tiles:putAttribute name="categoryItem" value="${categoryItem}" type="org.marketlive.entity.product.ICategoryItem" />
                    <tiles:putAttribute name="itemIndex" value="${categoryItem.pk.asString}" />
                </tiles:insertAttribute>
            </div>
        </c:if>
        
        <%-- Affirm Monthly Payment Price --%>
        <c:if test="${MonthlyPaymentPriceModel.attributeMap['isAffirmMonthlyPaymentDirectoryEnabled'] && !borderFreeEnabledAndNotUSSelected}">
		<tiles:insertDefinition name=".tile.p2p.AffirmMonthlyPaymentPriceMessage">
			<tiles:putAttribute name="categoryItem" value="${categoryItem}"/>
		</tiles:insertDefinition>
		</c:if>
        
        <%-- Rating --%>
        <c:if test="${showRating}">
            <tiles:insertAttribute name="rating">
                <tiles:putAttribute name="categoryItem" value="${categoryItem}" type="org.marketlive.entity.product.ICategoryItem" />
                <tiles:putAttribute name="showRatingValue" value="${ThumbnailModel.showRatingValue}"/>
                <tiles:putAttribute name="showRatingText" value="${ThumbnailModel.showRatingText}"/>
            </tiles:insertAttribute>
        </c:if>
        <%-- FB Likes --%>
        <c:if test="${showFBLike}">
            <div class="ml-fbl" data-href="${mlsocial:generateFacebookProductURL(categoryItem, pageContext, false)}" data-show-faces="${showFacePile}">
                <div class="ml-fbl-placeholder">&nbsp;</div>
            </div>
        </c:if>

        <%-- Long Description--%>
        <c:if test="${showLongDescriptionForDirectoryListView}">
            <div class="ml-thumb-desc-long"><c:out value="${categoryItem.description.long}" escapeXml="false" /></div>
        </c:if>
    </div>
</c:if>

