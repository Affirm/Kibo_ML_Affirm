<%@ page import="com.marketlive.app.b2c.common.constants.RequestParams"%>
<%@ include file="/WEB-INF/views/common/TagLibs.jsp" %>

<style>
.ml-topnav-identity-guest .ml-topnav-identity-guest {
	z-index: 200;
}
</style>

<%-- for client side validation --%>
<fmt:message key="msg.form.search" var="msgSearch"/>
<fmt:message key="err.common.noSearchTerm" var="msgNoSearchTerm"/>
<fmt:message key="err.common.shortSearchTerm" var="msgShortSearchTerm"><fmt:param value="${TopNavModel.minKeywordLength}" /></fmt:message>
<mlperf:script defer="false">MarketLive.Nav.TopNav.initialize({minKeywordLength:${TopNavModel.minKeywordLength}, maxKeywordLength:${TopNavModel.maxKeywordLength}, msgSearch:"${msgSearch}", msgNoSearchTerm:"${msgNoSearchTerm}", msgShortSearchTerm:"${msgShortSearchTerm}"});</mlperf:script>

<%-- Pre-load Icon Fix (do not remove)--%>
<span class="ml-icon-lib ml-icon-home ml-icon-pre-load-fix"></span>

<%-- Lite Registration Dialog --%>
<div id="mlLiteLoginApp">
    <div class="modal fade" data-ng-controller="mlLiteLoginCtrl" id="mlLiteRegistrationDialog" tabindex="-1" role="dialog" aria-hidden="true">
        <div class="modal-dialog">
            <div class="ml-lite-registration-dialog">
                <tiles:insertAttribute name="login"/>
            </div>
        </div>
    </div>
</div>

<%-- Global Header Include --%>
<tiles:insertAttribute name="globalHeaderInclude"/>

<c:set var="searchEnabled" value="${TopNavModel.isSearchEnabled()}"></c:set>
<c:set var="displayToggleSearch" value="${TopNavModel.displayToggleSearch}"></c:set>

<c:if test="${searchEnabled && displayToggleSearch}">
    <c:set var="toggleSearchClass" value=" ml-header-toggle-search"></c:set>
</c:if>

<div class="ml-header-wrapper${toggleSearchClass}">
    <div class="container">
        <div class="ml-header">
            <div class="ml-header-content-wrapper${toggleSearchClass}">
                <div class="ml-header-content">
                    <%-- Logo --%>
                    <div class="ml-header-logo-wrapper">
                        <div class="ml-header-logo">
                            <fmt:message key='companyName' var="companyName" />
                            <mlabtest:element uniqueIdentifier="logoImage" description="Top Nav Logo Image (Global)" uriSpecific="false">
                            <a href="${mlnav:urlWrapper('/home.do', pageContext)}">
                                <ml:picture alt="${companyName}" srcXs="${siteImages.imagePath}global/globalnav/logo_xs.png" srcMd="${siteImages.imagePath}global/globalnav/logo01.png" />
                            </a>
                            </mlabtest:element>
                        </div>
                    </div>

                    <c:if test="${searchEnabled}">
                        <c:if test="${TopNavModel.getUseAutoComplete()}">
                            <tiles:insertDefinition name=".tile.nav.AutoComplete"/>
                        </c:if>
                        <c:set var="searchContextParam" value="<%= RequestParams.SEARCH_CONTEXT %>" />
                        <c:set var="catalogContextParamValue" value="<%= RequestParams.SEARCH_CONTEXT_ID_CATALOG %>" />
                        <c:set var="contentContextParamValue" value="<%= RequestParams.SEARCH_CONTEXT_ID_CONTENT %>" />
                        <div class="ml-header-search-wrapper">
                        <div class="ml-header-search">
                            <form name="searchForm" method="get" action="/search.do"
                                  onsubmit="document.searchForm.pageName.value=MarketLive.Base.getVariableValue('pageName');return MarketLive.Nav.TopNav.checkKeyword(this.keyword);">
                                <%-- hidden field that will be set to the value of the pageName
                                variable, for Omniture reporting, when the "Search" button
                                is clicked --%>
                                <input type="hidden" name="pageName" value="" />
                                <div id="searchDiv">
                                    <label for="navsearchbox" class="ml-navsearchbox-label">Search</label>
                                    <input type="text" id="navsearchbox" tabindex="1" value="${TopNavModel.getDefaultSearchText()}" name="keyword" class="ml-header-search-field form-control" placeholder="<fmt:message key='msg.form.search' />" maxlength="${TopNavModel.getMaxKeywordLength()}" data-toggle="dropdown">
                                    <!-- here is where autocomplete content is appended to -->
                                </div>
                            <div class="ml-header-search-btn-wrapper">
                                <mlabtest:element uniqueIdentifier="topNavSearchBtn" description="Top Nav Search Button (Global)" uriSpecific="false"><button type="submit" class="ml-icon ml-icon-search" tabindex="2"></button></mlabtest:element>
                            </div>
                                <c:if test="${displayToggleSearch}">
                                    <c:set var="defaultToProductContext" value="checked" />
                                    <c:set var="defaultToContentContext" value="" />
                                    <c:if test="${!empty searchState && searchState.currentContext == contentContextParamValue}">
                                        <c:set var="defaultToProductContext" value="" />
                                        <c:set var="defaultToContentContext" value="checked" />
                                    </c:if>
                                    <div class="ml-header-toggle-search-input">
                                        <label>
                                            <input type="radio" name="${searchContextParam}" value="${catalogContextParamValue}" ${defaultToProductContext}/>
                                            <fmt:message key="lbl.form.search.product" />
                                        </label>
                                        <label>
                                            <input type="radio" name="${searchContextParam}" value="${contentContextParamValue}" ${defaultToContentContext}/>
                                            <fmt:message key="lbl.form.search.content" />
                                        </label>
                                    </div>
                                </c:if>
                            </form>
                        </div>
                    </div>
                    </c:if>
                        <div class="ml-header-links-wrapper">
                        <div class="ml-header-links">
                            <%-- Products/Category Menu  --%>
                            <c:set var="menuText"><fmt:message key="lnk.header.categoryMenu"/></c:set>
                            <div class="ml-header-link ml-header-shop">
                                <c:choose>
                                    <c:when test="${!empty menuText}">
                                        <div class="ml-header-link-item" data-toggle="collapse" data-target="#ml-navbar-collapse">
                                            <span><fmt:message key="lnk.header.categoryMenu"/></span>
                                            <span class="ml-icon-lib ml-icon-down"></span>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="ml-header-link-item ml-header-link-no-padding" data-toggle="collapse" data-target="#ml-navbar-collapse">
                                            <span class="ml-icon-stack">
                                              <span class="ml-icon-lib ml-icon-square-o ml-icon-stack-2x"></span>
                                              <span class="ml-icon-lib ml-icon-bars ml-icon-stack-1x"></span>
                                            </span>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                            <%-- User --%>
                            <div class="ml-header-link ml-header-account">
                                <div class="ml-header-link-item">
                                    <tiles:insertAttribute name="topNavIdentity" />
                                </div>
                            </div>

                            <%-- Stores --%>
                            <div class="ml-header-link ml-header-stores">
                                <div class="ml-header-link-item">
                                    <span><a href="/store-locator.do"><fmt:message key="lnk.header.storeLocator" /></a></span>
                                    <span class="ml-icon-lib ml-icon-map-marker"></span>
                                </div>
                            </div>

                            <%-- Phone --%>
                            <div class="ml-header-link ml-header-phone">
                                <div class="ml-header-link-item">
                                    <span><fmt:message key="lnk.header.phone" /></span>
                                    <span class="ml-icon-lib ml-icon-phone"></span>
                                </div>
                            </div>

                            <%-- Language --%>
                            <tiles:insertAttribute name="siteLanguageSelector"/>

                            <c:set var="borderfreeCountryLookupModalInserted" value="false" scope="request"/>
                            <%-- Ship to: borderFree --%>
                            <c:if test="${TopNavModel.borderFreeShippingEnabled}">
                                <div class="ml-header-link">
                                    <%-- BorderFree International Shipping Tile --%>
                                    <tiles:insertDefinition name=".tile.intlshipping.processor.borderfree.ipCountryLookup"/>
                                </div>
                            </c:if>
                        </div>
                    </div>

                    <%-- Shopping Bag --%>
                    <div class="ml-header-global-cart-wrapper">
                        <tiles:insertAttribute name="globalcart"/>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>