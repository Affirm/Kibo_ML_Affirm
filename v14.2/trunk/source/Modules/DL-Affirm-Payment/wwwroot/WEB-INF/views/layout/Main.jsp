<%@ include file="/WEB-INF/views/common/TagLibs.jsp" %>
<%@ include file="/templates/inc-displayresult.jsp" %>

<%--@elvariable id="useLeftNav" type="java.lang.String"--%>
<%--@elvariable id="forcedLeftNav" type="java.lang.boolean"--%>
<%--@elvariable id="localeModel" type="com.marketlive.app.b2c.LocaleModel"--%>
<%--@elvariable id="siteImages" type="com.marketlive.app.b2c.images.SiteImages"--%>
<%--@elvariable id="TopNavModel" type="com.marketlive.app.b2c.nav.TopNavModel"--%>
<%--@elvariable id="site" type="com.marketlive.app.b2c.multisite.SiteScopeBeanMap"--%>

<tiles:importAttribute name="layoutSection" scope="request" />
<c:set var="layoutSectionCSSClass" value="${layoutSection}" />
<c:if test="${empty layoutSectionCSSClass}"><c:set var="layoutSectionCSSClass" value="default" /></c:if>
<c:set var="layoutSectionCSSClass" value="ml-layout-section-${fn:toLowerCase(layoutSectionCSSClass)}" />
<tilesx:useAttribute name="affirmPaymentsJavascriptSDK" id="affirmPaymentsJavascriptSDK" ignore="true"/>



<tiles:importAttribute name="useLeftNav" />
<c:if test="${useLeftNav == 'true'}"><c:set var="showLeftNav" value="true" scope="request" /></c:if>
<c:if test="${useLeftNav == 'never'}"><c:set var="showLeftNav" value="false" scope="request" /></c:if>

<%-- Left Nav Controller --%>
<c:if test="${!forcedLeftNav && layoutSection != 'ACCOUNT' && layoutSection != 'ANCILLARY'}">
    <tiles:insertAttribute name="leftNav">
        <tiles:putAttribute name="feature" value="LEFTNAV" />
    </tiles:insertAttribute>
</c:if>
<%-- display left nav background image --%>
<c:set var="leftNavBG" value="" />
<c:if test="${showLeftNav}">
    <c:set var="leftNavBG" value="ml-navleft-bg" />
</c:if>

<fmt:setLocale value="${localeModel.locale}" scope="session" />
<tiles:insertAttribute name="resourceBundle"><tiles:putAttribute name="feature" value="RESOURCEBUNDLE"/></tiles:insertAttribute>

<!DOCTYPE html>
<html lang="en-US" xmlns:fb="http://www.facebook.com/2008/fbml">

<head>
    <tiles:insertAttribute name="htmlhead"/>
    <link rel="shortcut icon" href="${siteImages.imagePath}favicon.ico" type="image/vnd.microsoft.icon">
    <c:if test="${!empty affirmPaymentsJavascriptSDK}">
    	<tiles:insertDefinition name="${affirmPaymentsJavascriptSDK}" />
	</c:if>
</head>

<body class="${layoutSectionCSSClass}">


<a name="top"></a>
<div id="ml-modal-placement"></div>

<%-- Header ******************************************************************************************************* --%>
<header role="banner" >
    <nav role="navigation">
        <tiles:insertAttribute name="header"/>
    </nav>
    <c:if test="${TopNavModel.topCategoryNavShown}"><div><tiles:insertAttribute name="categorynav"/></div></c:if>
</header>

<%-- Main Content ************************************************************************************************* --%>
<main role="main">
    <div class="${leftNavBG}">
        <div class="container">
            <c:choose>
                <%-- Left Nav, Breadcrumb & Body Content --%>
                <c:when test="${showLeftNav}">
                    <div class="ml-leftNav-wrapper">
                        <div class="ml-navleft-body ml-body-wrapper">
                            <%-- Bread Crumb --%>
                            <c:if test="${site.layout.breadcrumbShown}"><div class="ml-breadcrumb-wrapper"><tiles:insertAttribute name="breadcrumb"/></div></c:if>
                            <tiles:insertAttribute name="body"/>
                            <tiles:insertAttribute name="secondaryCategoryNav"/>
                        </div>
                        <div id="leftNavContainer" class="ml-leftNav-container">
                            <tiles:insertAttribute name="leftNav">
                                <tiles:putAttribute name="feature" value="LEFTNAV" />
                            </tiles:insertAttribute>
                        </div>
                    </div>
                </c:when>

                <%-- Breadcrumb and Body Content --%>
                <c:otherwise>
                    <div class="ml-body-wrapper">
                        <%-- Bread Crumb --%>
                        <c:if test="${site.layout.breadcrumbShown}"><div class="ml-breadcrumb-wrapper"><tiles:insertAttribute name="breadcrumb"/></div></c:if>
                        <tiles:insertAttribute name="body"/>
                        <tiles:insertAttribute name="secondaryCategoryNav"/>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</main>

<%-- Footer ******************************************************************************************************* --%>
<footer role="contentinfo">
    <tiles:insertAttribute name="footer"/>
</footer>

<%-- Browser Compatibility Check --%>
<!--  tiles:insertDefinition name=".browser.compatibility" />-->
<%--  Reporting Tile --%>
<c:if test="${site.reportingModel.omnitureEnabled || site.reportingModel.googleanalyticsEnabled}">
    <tiles:insertAttribute name="reporting"/>
</c:if>

<%-- Global Script Groups --%>
<mlperf:script type="text/javascript" group="FOOTER" />
<mlperf:script type="text/javascript" group="P2P" />

<%-- Checkout Scripts --%>
<c:if test="${layoutSection == 'CHECKOUT'}">
    <mlperf:script type="text/javascript" group="CHECKOUT"></mlperf:script>
</c:if>

<%-- Deferred scripts by mlperf taglib --%>
<mlperf:printDeferredScripts />

<c:if test="${sessionScope.showLiteRegistrationPopUp}">
    <%-- Remove session variable --%>
    <c:remove var="showLiteRegistrationPopUp" scope="session" />

    <c:set var="accountSectionForwardURL" value="${sessionScope.accountSectionForwardURLKey}" />

    <%-- Remove session variable --%>
    <c:remove var="accountSectionForwardURLKey" scope="session" />

    <%-- Show lite registration popup --%>
    <script type="text/javascript">
        MarketLive.Base.setSessionStorageAttribute("accountSectionURL", '<c:out value="${accountSectionForwardURL}"/>');
        MarketLive.Base.literegistration('/checkout/literegistration.do?liteRegistrationType=identityLogin&accessAccountSection=true', 'IdentityGuest');

    </script>
</c:if>

</body>
</html>