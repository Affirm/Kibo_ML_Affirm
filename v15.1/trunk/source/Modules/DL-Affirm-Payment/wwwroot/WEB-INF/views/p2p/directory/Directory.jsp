<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/views/common/TagLibs.jsp" %>
<%@ page import="com.marketlive.app.b2c.common.constants.RequestParams"%>
<c:set var="refTypeQueryString" scope="request"><%= RequestParams.REFERRAL_TYPE %>=<%=""%></c:set>

<%----------------------------------------------------------------------------------------------------------------------
  -    Description: Directory, displays a listing of the active products and families.
  -  Storyboard ID: P2P0030
  - Pre-conditions:
  -     Model/Form: com.marketlive.app.b2c.p2p.directory.ProductDirectoryModel
  - URLs Posted To:
  -   Current Tile: .tile.p2p.directory
  - Tile Variables:
  -  Child Tile(s): categorydropdown, productsetpaging, productthumbnail, productsetpaging
  --------------------------------------------------------------------------------------------------------------------%>

<%--
    //(C) Copyright MarketLive. 2014. All rights reserved.
    //MarketLive is a trademark of MarketLive, Inc.
    //Warning: This computer program is protected by copyright law and international treaties.
    //Unauthorized reproduction or distribution of this program, or any portion of it, may result
    //in severe civil and criminal penalties, and will be prosecuted to the maximum extent
    //possible under the law.
--%>
<jsp:useBean id="affirmWebUtil" class="com.deplabs.affirm.app.b2c.AffirmWebUtil"/>
<ct:call object="${affirmWebUtil}" method="isBorderFreeEnabledAndNotUSSelected"  fieldNameParam="${pageContext.request}" return="borderFreeEnabledAndNotUSSelected" />

<mlperf:script defer="true" type="text/javascript">
    MarketLive.P2P.onDirectoryReady();
    MarketLive.Events.bindProductClick('.ml-directory',
            MarketLive.Reporting.EnhancedEcommerceListName.DIRECTORY_PAGE,
            '${ProductDirectoryModel.category.pk.asString}',
            MarketLive.Reporting.EnhancedEcommerceBindingPriority.SPECIFIC_PAGE);
</mlperf:script>

<c:set var="isProductsExist">${!empty ProductDirectoryModel.products}</c:set>
<c:set var="classShowLeftNav" value=""/>
<c:if test="${showLeftNav}">
    <c:set var="classShowLeftNav" value="ml-show-left-nav" />
</c:if>

<c:set var="classNoProducts" value=""/>
<c:if test="${!isProductsExist}">
    <c:set var="classNoProducts" value="ml-directory-no-products"/>
</c:if>

<div class="ml-directory ${classShowLeftNav}">
    <%--  Cat Header Image and Product Set Paging --%>
    <c:if test="${ProductDirectoryModel.categoryHeaderShown}">
        <div class="ml-dir-header-category-wrapper ${classNoProducts}">
            <div class="ml-dir-header-category">
                <c:choose>
                    <c:when test="${showLeftNav}">
                        <c:set var="catHeader" value="${ProductDirectoryModel.category.name}" />
                        <c:if test="${!empty ProductDirectoryModel.category.headerText}"><c:set var="catHeader" value="${ProductDirectoryModel.category.headerText}" /></c:if>
                        <c:set var="catHeader"><div class="ml-dir-header-category-head-text"><h1>${catHeader}</h1></div></c:set>
                    </c:when>
                    <c:otherwise>
                        <c:set var="catHeader"><div class="ml-dir-header-category-head-image"><img src="${catalogImages.imagePath}${mlurl:replaceAllUriAmpersands(ProductDirectoryModel.catHeaderImage)}" alt=""></div></c:set>
                        <c:if test="${!empty ProductDirectoryModel.catHeaderText}">
                            <c:set var="catHeader"><div class="ml-dir-header-category-head-text"><h1>${ProductDirectoryModel.catHeaderText}</h1></div></c:set>
                        </c:if>
                    </c:otherwise>
                </c:choose>
                    <c:if test="${!showLeftNav}">
                        <div class="ml-dir-header-category-head">${catHeader}</div>
                        <div class="ml-dir-header-category-dropdown">
                            <c:choose>
                                <c:when test="empty ProductDirectoryModel.catHeaderText">
                                    <c:set var="catHeaderText" value="Category"/>
                                </c:when>
                                <c:otherwise>
                                    <c:set var="catHeaderText" value="${ProductDirectoryModel.catHeaderText}"/>
                                </c:otherwise>
                            </c:choose>
                            <tiles:insertAttribute name="categorydropdown">
                                <tiles:putAttribute name="catHeaderText" value="${catHeaderText}"/>
                            </tiles:insertAttribute>
                        </div>
                    </c:if>
                    <%--  Sorting --%>
                    <c:if test="${ProductDirectoryModel.sortingShown && isProductsExist}">
                        <c:set var="sortAction" value="${ProductDirectoryModel.sortingURL}" scope="request"/>
                        <div class="ml-dir-header-category-sort"><tiles:insertAttribute name="sort" /></div>
                    </c:if>
                <div id="bodyFacetedNavPlaceholder" class="ml-body-faceted-nav-placeholder"></div>
            </div>
        </div><%-- ml-dir-header-category-wrapper --%>
    </c:if>
    <c:choose>
        <c:when test="${isProductsExist}">
            <div class="ml-grid-view">
                <div class="ml-dir-header-page-wrapper">
                    <div class="ml-grid-view-toggle-container btn-group">
                        <button type="button" id="gridView" class="btn btn-default"><span class="ml-icon-lib ml-icon-th-grid"></span></button>
                        <button type="button" id="listView" class="btn btn-default"><span class="ml-icon-lib ml-icon-th-list"></span></button>
                    </div>
                    <div class="ml-paging-container">
                            <%--  Product Set Paging --%>
                        <c:set var="pagingAction" value="${ProductDirectoryModel.pagingURL}" scope="request"/>
                        <c:set var="basePagingAction" value="${ProductDirectoryModel.basePagingURL}" scope="request"/>
                        <c:if test="${ProductDirectoryModel.pagingAtTopShown}">
                            <tiles:insertAttribute name="productsetpaging"/>
                        </c:if>
                    </div>
                </div>
                
				<%-- Affirm Monthly Payment Price --%>
				<c:if test="${!borderFreeEnabledAndNotUSSelected}">
					<tiles:insertAttribute name="affirmMonthlyPaymentPrice"/>
				</c:if>


                    <%--  Product Thumbs --%>
                <div id="ml-grid-view-items" class="ml-grid-view-items ml-grid-view-multi-column">
                        <%-- toggle ml-grid-view-multi-column or ml-grid-view-single-column class to ml-grid-view-items id selector --%>
                    <c:forEach var="thumb" varStatus="status" items="${ProductDirectoryModel.products}">
                        <div class="ml-grid-view-item">
                            <tiles:insertAttribute name="categoryItemthumbnail">
                                <tiles:putAttribute name="categoryItem" value="${thumb}" type="org.marketlive.entity.product.ICategoryItem"/>
                                <tiles:putAttribute name="itemIndex" value="${status.index}" />
                            </tiles:insertAttribute>
                        </div>
                    </c:forEach>
                </div>

                <div class="ml-grid-view-footer">
                    <div class="ml-grid-view-toggle-container"></div>
                    <div class="ml-paging-container">
                        <c:if test="${ProductDirectoryModel.pagingAtBottomShown}">
                            <%--  Product Set Paging --%>
                            <c:set var="pagingAction" value="${ProductDirectoryModel.pagingURL}" scope="request"/>
                            <c:set var="basePagingAction" value="${ProductDirectoryModel.basePagingURL}" scope="request"/>
                            <c:if test="${ProductDirectoryModel.pagingAtBottomShown}">
                                <tiles:insertAttribute name="productsetpaging"/>
                            </c:if>
                        </c:if>
                    </div>
                </div>

                <%-- Back to Top --%>
                <div class="ml-back-to-top">
                    <a href="#top">
                        <span class="ml-icon-lib ml-icon-up"></span>
                        <fmt:message key="lnk.backToTop"/>
                    </a>
            </div>
            </div>
        </c:when>
        <c:otherwise>
            <c:set var="noProductsMessage"><fmt:message key="msg.product.directory.noProducts"/></c:set>
            <c:if test="${searchState.zeroResultSelectionBase}"><c:set var="noProductsMessage"><fmt:message key="msg.facetedNav.noProductsMatchingCriteria"/></c:set></c:if>
            <div class="ml-horizontal-separator"></div>
            <div class="ml-dir-no-products-msg">${noProductsMessage}</div>
            <tiles:insertAttribute name="secondaryCategoryNav"/>
        </c:otherwise>
    </c:choose>

</div><%-- ml-directory --%>